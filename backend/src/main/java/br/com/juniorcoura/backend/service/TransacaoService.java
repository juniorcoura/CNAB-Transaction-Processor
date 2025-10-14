package br.com.juniorcoura.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.juniorcoura.backend.entity.TipoTransacao;
import br.com.juniorcoura.backend.entity.Transacao;
import br.com.juniorcoura.backend.entity.TransacaoReport;
import br.com.juniorcoura.backend.repository.TransacaoRepository;

@Service
public class TransacaoService {
    
    private final TransacaoRepository transacaoRepository;
    
    public TransacaoService(TransacaoRepository transacaoRepository){
        this.transacaoRepository = transacaoRepository;
    }

    public List<TransacaoReport> listTotaisTransacoesPorNomeDaLoja(){
        var transacoes = transacaoRepository.findAllByOrderByNomeDaLojaAscIdDesc();
        
        var reportMap = new LinkedHashMap<String, TransacaoReport>();

        transacoes.forEach(transacao -> {
            String nomeDaLoja = transacao.nomeDaLoja();
           // var tipoTransacao = TipoTransacao.findByTipo(transacao.tipo());
            BigDecimal valor = transacao.valor();

            reportMap.compute(nomeDaLoja, (key, existingReport) ->{
                var report = (existingReport != null) ? existingReport 
                : new TransacaoReport(key, BigDecimal.ZERO, new ArrayList<>());

                return report.addTotal(valor).addTransacao(transacao.withValor(valor));
            });
        });
        return new ArrayList<>(reportMap.values());
    }
}
