package br.edu.ifrn.chamados.repository;

import br.edu.ifrn.chamados.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsBySetorId(Long setorId);

    /**
     * Lista categorias com filtros opcionais e combinaveis por nome e setor.
     * Filtro nao informado ({@code null}) e ignorado.
     */
    @Query("""
            select c from Categoria c
            where (cast(:nome as string) is null
                   or lower(c.nome) like lower(concat('%', cast(:nome as string), '%')))
              and (:setorId is null or c.setor.id = :setorId)
            """)
    Page<Categoria> buscarPorFiltro(@Param("nome") String nome,
                                    @Param("setorId") Long setorId,
                                    Pageable pageable);

    /** Categorias de um setor especifico (GET /setores/{id}/categorias). */
    Page<Categoria> findBySetorId(Long setorId, Pageable pageable);
}
