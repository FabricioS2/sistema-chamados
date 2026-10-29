package br.edu.ifrn.chamados.repository;

import br.edu.ifrn.chamados.model.Chamado;
import br.edu.ifrn.chamados.model.Interacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteracaoRepository extends JpaRepository<Interacao, Long> {

    /** Historico do chamado, ordenado do mais antigo para o mais recente. */
    List<Interacao> findByChamadoIdOrderByDataHoraAsc(Long chamadoId);
}
