package br.com.juniorcoura.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import br.com.juniorcoura.backend.service.TransacaoService;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.juniorcoura.backend.entity.Transacao;
import br.com.juniorcoura.backend.repository.TransacaoRepository;

@ExtendWith(MockitoExtension.class)
public class TransacaoServiceTest {
    
    //AAA test ( Arrange, Act, Assert)

    @InjectMocks
    private TransacaoService transacaoService;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Test
    public void testlistGetTotaisTransacoesByNomeDaLoja(){
        //Arrange
        final String lojaA = "Loja A", lojaB = "Loja B";
        var transacao1 = new Transacao(1L, 1, new Date(System.currentTimeMillis()),
        BigDecimal.valueOf(100), 123456789L, "1234-1234-1234-1234", new Time(System.currentTimeMillis()), "DonoLojaB", lojaB);

        var transacao2 = new Transacao(1L, 1, new Date(System.currentTimeMillis()),
        BigDecimal.valueOf(50), 123442789L, "5678-5678-5678-5678", new Time(System.currentTimeMillis()), "DonoLojaB", lojaB);

        var transacao3 = new Transacao(2L, 1, new Date(System.currentTimeMillis()),
        BigDecimal.valueOf(75), 123123489L, "4321-4321-4321-4321", new Time(System.currentTimeMillis()), "DonoLojaA", lojaA);

        var mockTransacoes = List.of(transacao1, transacao2, transacao3);

        when(transacaoRepository.findAllByOrderByNomeDaLojaAscIdDesc())
        .thenReturn(mockTransacoes);

        //Act
        var reports = transacaoService.listTotaisTransacoesPorNomeDaLoja();

        //Assert

        //Verifica se o agrupamento por loja está sendo realizado
        assertEquals(2, reports.size());

        reports.forEach(report -> {
            if(report.nomeDaLoja().equals(lojaA)){
                assertEquals(1, report.transacoes().size());
                assertEquals(BigDecimal.valueOf(75), report.total());
                assertTrue(report.transacoes().contains(transacao3));
            }else if(report.nomeDaLoja().equals(lojaB)){
                assertEquals(2, report.transacoes().size());
                assertEquals(BigDecimal.valueOf(150), report.total());
                assertTrue(report.transacoes().contains(transacao1));
                assertTrue(report.transacoes().contains(transacao2));
            }
        });        
    }
}
