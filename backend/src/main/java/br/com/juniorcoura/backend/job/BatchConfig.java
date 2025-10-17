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

    /*
     * Define o Job principal do spring batch
     */
    @Bean
    Job job(Step step){
        return new JobBuilder("job", jobRepository)
        .start(step)
        .incrementer(new RunIdIncrementer())
        .build();
    }

    /*
     * Cria e configura o Step principal do Job.
     * O Step é uma fase do Job que contém a lógica de leitura, processamento e escrita.
     * O Step é configurado para trabalhar em pedaçõs, otimizando a performance.
     */
    @Bean
    Step step(ItemReader<TransacaoCNAB> reader,
     ItemProcessor<TransacaoCNAB, Transacao> processor, ItemWriter writer){

        return new StepBuilder("step", jobRepository)
        // Define o tipo de estrada(TransacaoCNAB), saída(Transacao) do chunk.
        // Chunk(1000): Lê e processa 1000 itens antes de escreve-los de uma só vez no banco.
            .<TransacaoCNAB, Transacao>chunk(1000, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }

    /*
     * Cria e configura o ItemReader para ler o arquivo CNAB.
     * Retorna um objeto do tipo FlatFileItemReader. Um Flat File é um arquivo de texto simples
     * que armazena  em um formato de duas dimensões (linhas e colunas).
     */
    @StepScope
    @Bean
    FlatFileItemReader<TransacaoCNAB> reader(@Value("#{jobParameters['cnabFile']}") Resource resource){
        return new FlatFileItemReaderBuilder<TransacaoCNAB>()
        // Define um nome para o reader
        .name("reader")
        // Arquivo a ser lido
        .resource(resource)
        // Define a leitura como "tamanho fixo"
        .fixedLength()
        // Define os intervalos de caracteres para cada campo na linha
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
        // Mapeia os nomes dos campos para os intervalos definidos
        .names(
            "tipo","data","valor","cpf","cartao","hora","donoDaLoja","nomeDaLoja"
            )
        // Define a classe destino para onde os dados de cada linha serão mapeados
            .targetType(TransacaoCNAB.class)
        // Constrói o ItemReader configurado
            .build();
    }

    /*
     * Transforma os dados antes de passar para o ItemWriter.
     * Intermediário para as regras de negócio, transformações, validação e para filtrar
     * os itens.
     */
    @Bean 
    ItemProcessor<TransacaoCNAB, Transacao> processor(){
        return item ->{

            // Obtém o tipo da transação através do enum TipoTransacao para obter
            // a lógica de negócio
            var tipoTransacao = TipoTransacao.findByTipo(item.tipo());

            // Normaliza o valor
            var valorNormalizado = item.valor().divide(new BigDecimal((100))).multiply(tipoTransacao.getSinal());
            
            var transaction = new Transacao(
                null, item.tipo(), null, valorNormalizado, item.cpf(), item.cartao(),
                null, item.donoDaLoja().trim(), item.nomeDaLoja().trim()
            )
            .withData(item.data())
            .withHora(item.hora());
            return transaction;
       };
    }


    /*
     * Define o ItemWriter, responsável por inserir os dados no banco
     */
    @Bean
    JdbcBatchItemWriter<Transacao> writer( DataSource dataSource){
        return new JdbcBatchItemWriterBuilder<Transacao>()
            // Define a conexão com o banco de dados.
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

    /*
     * Configura um JobLauncher para executar os Jobs de forma assíncrona.
     * Por padrão, o JobLauncher executa o Job na mesma thread da requisição que o chamou.
     * Em uma aplicação web, isso faria a requisição HTTP esperar todo o Job terminar.
     */
    @Bean
    JobLauncher jobLauncherAsync(JobRepository jobRepository) throws Exception{
        var jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        
        // Faz com que cada Job seja executado em uma nova thread, em segundo plano.
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
}

