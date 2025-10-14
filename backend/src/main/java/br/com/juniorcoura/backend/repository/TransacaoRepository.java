package br.com.juniorcoura.backend.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import br.com.juniorcoura.backend.entity.Transacao;

public interface TransacaoRepository extends CrudRepository<Transacao, Long> {
    
    // select * from transacao order by nome_loja asc and id desc
    List<Transacao> findAllByOrderByNomeDaLojaAscIdDesc();
}
