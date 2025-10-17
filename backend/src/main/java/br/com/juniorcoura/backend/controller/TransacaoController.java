package br.com.juniorcoura.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.juniorcoura.backend.entity.Transacao;
import br.com.juniorcoura.backend.entity.TransacaoReport;
import br.com.juniorcoura.backend.service.TransacaoService;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("transacoes")
public class TransacaoController {
    
    
    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService){
        this.transacaoService = transacaoService;
    }

    /*
     * A anotação @CrossOrigin (CORS) é utilizada para permitir que o frontend da aplicação,
     * rodando em um domínio diferente, possa fazer requisições
     * para este endpoint.
     */
    @GetMapping("")
    @CrossOrigin(origins = {"http://localhost:9090"}) 
        List<TransacaoReport> listAll (){
        return transacaoService.listTotaisTransacoesPorNomeDaLoja();
    }
}
