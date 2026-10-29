package br.edu.ifrn.chamados.repository;

import br.edu.ifrn.chamados.model.Setor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SetorRepository extends JpaRepository<Setor, Long> {

    boolean existsBySiglaIgnoreCase(String sigla);

    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);

    /**
     * Lista setores com filtro opcional por nome (contem, sem diferenciar maiusculas).
     * Filtro nao informado ({@code null}) e ignorado; a ordenacao vem do {@link Pageable}.
     *
     * <p>O {@code cast(:nome as string)} da tipo ao parametro nulo - sem ele o PostgreSQL
     * nao consegue inferir o tipo do bind e a consulta falha.</p>
     */
    @Query("""
            select s from Setor s
            where (cast(:nome as string) is null
                   or lower(s.nome) like lower(concat('%', cast(:nome as string), '%')))
            """)
    Page<Setor> buscarPorFiltro(@Param("nome") String nome, Pageable pageable);
}
