package br.com.juniorcoura.backend.job;

import java.math.BigDecimal;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import br.com.juniorcoura.backend.entity.TipoTransacao;
import br.com.juniorcoura.backend.entity.Transacao;
import br.com.juniorcoura.backend.entity.TransacaoCNAB;

@Configuration
public class BatchConfig {
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;

    public BatchConfig(PlatformTransactionManager transactionManager, JobRepository jobRepository){
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
    }

    @Bean
    Job job(Step step){
        return new JobBuilder("job", jobRepository)
        .start(step)
        .incrementer(new RunIdIncrementer())
        .build();
    }

    @Bean
    Step step(ItemReader<TransacaoCNAB> reader,
     ItemProcessor<TransacaoCNAB, Transacao> processor, ItemWriter writer){
        return new StepBuilder("step", jobRepository)
            .<TransacaoCNAB, Transacao>chunk(1000, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }

    @StepScope
    @Bean
    FlatFileItemReader<TransacaoCNAB> reader(@Value("#{jobParameters['cnabFile']}") Resource resource){
        return new FlatFileItemReaderBuilder<TransacaoCNAB>()
        .name("reader")
        .resource(resource)
        .fixedLength()
        .columns(
            new org.springframework.batch.item.file.transform.Range(1,1), 
            new org.springframework.batch.item.file.transform.Range(2,9),
            new org.springframework.batch.item.file.transform.Range(10,19), 
            new org.springframework.batch.item.file.transform.Range(20,30),
            new org.springframework.batch.item.file.transform.Range(31,42),
            new org.springframework.batch.item.file.transform.Range(43,48),
            new org.springframework.batch.item.file.transform.Range(49,62),
            new org.springframework.batch.item.file.transform.Range(63,80)
            )
        .names(
            "tipo","data","valor","cpf","cartao","hora","donoDaLoja","nomeDaLoja"
            )
            .targetType(TransacaoCNAB.class)
            .build();
    }

    @Bean 
    ItemProcessor<TransacaoCNAB, Transacao> processor(){
        return item ->{

            var tipoTransacao = TipoTransacao.findByTipo(item.tipo());
            var valorNormalizado = item.valor().divide(new BigDecimal((100))).multiply(tipoTransacao.getSinal());
            
            //Wither pattern
            var transaction = new Transacao(
                null, item.tipo(), null, valorNormalizado, item.cpf(), item.cartao(),
                null, item.donoDaLoja().trim(), item.nomeDaLoja().trim()
            )
            .withData(item.data())
            .withHora(item.hora());
            return transaction;
       };
    }

    @Bean
    JdbcBatchItemWriter<Transacao> writer( DataSource dataSource){
        return new JdbcBatchItemWriterBuilder<Transacao>()
           .dataSource(dataSource)
           .sql(
            """
                    INSERT INTO transacoes (
                       tipo, data, valor, cpf, cartao,
                        hora, dono_loja, nome_loja
                        ) VALUES (
                            :tipo, :data, :valor, :cpf, :cartao,
                            :hora, :donoDaLoja, :nomeDaLoja
                            )
                    """
            )
            .beanMapped()
            .build();     
        }

    @Bean
    JobLauncher jobLauncherAsync(JobRepository jobRepository) throws Exception{
        var jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
}

