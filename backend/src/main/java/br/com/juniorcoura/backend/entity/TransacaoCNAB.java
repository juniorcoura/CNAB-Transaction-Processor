package br.com.juniorcoura.backend.entity;

import java.math.BigDecimal;

/*
 * Representa uma entidade imutável.
 */
public record TransacaoCNAB(
    Integer tipo,
    String data,
    BigDecimal valor,
    Long cpf,
    String cartao,
    String hora,
    String donoDaLoja,
    String nomeDaLoja
){

}