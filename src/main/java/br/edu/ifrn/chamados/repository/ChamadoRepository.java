package br.edu.ifrn.chamados.repository;

import br.edu.ifrn.chamados.model.Chamado;
import br.edu.ifrn.chamados.model.Prioridade;
import br.edu.ifrn.chamados.model.StatusChamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    /** Regra 8: categoria com chamados vinculados nao pode ser excluida. */
    boolean existsByCategoriaId(Long categoriaId);

    /** Regra 8: setor com chamados vinculados (atraves de suas categorias) nao pode ser excluido. */
    boolean existsByCategoriaSetorId(Long setorId);

    /**
     * Listagem paginada de chamados com filtros opcionais, combinaveis entre si.
     * Filtro nao informado ({@code null}) e ignorado; a ordenacao vem do {@link Pageable}.
     *
     * <p>Os {@code left join} evitam que chamados sem tecnico (ou sem filtro informado)
     * desaparecam do resultado. O periodo de abertura usa duas flags booleanas porque o
     * PostgreSQL nao infere o tipo de um parametro {@code timestamp} nulo.</p>
     *
     * @param status          ABERTO, EM_ATENDIMENTO, RESOLVIDO ou CANCELADO
     * @param prioridade      BAIXA, MEDIA ou ALTA
     * @param setorId         setor responsavel (setor da categoria)
     * @param categoriaId     categoria do chamado
     * @param solicitanteId   quem abriu o chamado
     * @param tecnicoId       tecnico atribuido
     * @param filtrarInicio   informa se o filtro de abertura a partir de {@code dataInicio} foi pedido
     * @param dataInicio      abertura a partir desta data/hora (inclusivo)
     * @param filtrarFim      informa se o filtro de abertura ate {@code dataFim} foi pedido
     * @param dataFim         abertura ate esta data/hora (inclusivo)
     */
    @Query("""
            select c from Chamado c
             left join c.categoria cat
             left join cat.setor st
             left join c.solicitante sol
             left join c.tecnico tec
            where (:status is null or c.status = :status)
              and (:prioridade is null or c.prioridade = :prioridade)
              and (:setorId is null or st.id = :setorId)
              and (:categoriaId is null or cat.id = :categoriaId)
              and (:solicitanteId is null or sol.id = :solicitanteId)
              and (:tecnicoId is null or tec.id = :tecnicoId)
              and (:filtrarInicio = false or c.dataAbertura >= :dataInicio)
              and (:filtrarFim = false or c.dataAbertura <= :dataFim)
            """)
    Page<Chamado> buscarPorFiltro(@Param("status") StatusChamado status,
                                  @Param("prioridade") Prioridade prioridade,
                                  @Param("setorId") Long setorId,
                                  @Param("categoriaId") Long categoriaId,
                                  @Param("solicitanteId") Long solicitanteId,
                                  @Param("tecnicoId") Long tecnicoId,
                                  @Param("filtrarInicio") boolean filtrarInicio,
                                  @Param("dataInicio") LocalDateTime dataInicio,
                                  @Param("filtrarFim") boolean filtrarFim,
                                  @Param("dataFim") LocalDateTime dataFim,
                                  Pageable pageable);

    /**
     * Chamados abertos por um solicitante ou atribuidos a um tecnico, com filtro opcional por status.
     */
    @Query("""
            select c from Chamado c
             left join c.solicitante sol
             left join c.tecnico tec
            where (sol.id = :usuarioId or tec.id = :usuarioId)
              and (:status is null or c.status = :status)
            """)
    Page<Chamado> buscarPorUsuario(@Param("usuarioId") Long usuarioId,
                                   @Param("status") StatusChamado status,
                                   Pageable pageable);
}
