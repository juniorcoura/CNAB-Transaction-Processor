package br.com.juniorcoura.backend.entity;

import java.math.BigDecimal;

/**
 * Enum que representa os tipos de transações financeiras possíveis
 * provenientes de um arquivo CNAB. Cada tipo é associado a um valor numérico
 * que o identifica.
 */
public enum TipoTransacao {
    DEBITO(1), 
    BOLETO(2), 
    FINANCIAMENTO(3), 
    CREDITO(4),
    RECEBIMENTO_EMPRESTIMO(5),
    VENDAS(6), 
    RECEBIMENTO_TED(7), 
    RECEBIMENTO_DOC(8), 
    ALUGUEL(9);

    private int tipo;

    private TipoTransacao(int tipo){
        this.tipo = tipo;
    }


    /*
     * Determina o sinal matemática da transação para calcular os valores adicionados no banco de dados.
     * 
     */
    public BigDecimal getSinal(){
        return switch(tipo){
            // Casos de ENTRADA de dinheiro para a loja
            case 1, 4, 5, 6, 7, 8 -> new BigDecimal(1);

            // Casos de SAÍDA de dinheiro da loja
            case 2, 3, 9 -> new BigDecimal(-1);

            default -> new BigDecimal(0);
        };
    }

    public static TipoTransacao findByTipo(int tipo){
        for(TipoTransacao tipoTransacao :values()){
            if(tipoTransacao.tipo == tipo){
                return tipoTransacao;
            }
        }
        throw new IllegalArgumentException("Invilid tipo:"+tipo);
    }
}
