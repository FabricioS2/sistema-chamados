package br.edu.ifrn.chamados.repository;

import br.edu.ifrn.chamados.model.TipoUsuario;
import br.edu.ifrn.chamados.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsBySetorId(Long setorId);

    /**
     * Lista usuarios com filtros opcionais e combinaveis por nome, tipo e setor.
     *
     * <p>O {@code left join} garante que solicitantes (que nao tem setor) continuem
     * aparecendo quando o filtro {@code setorId} nao e informado.</p>
     */
    @Query("""
            select u from Usuario u
             left join u.setor s
            where (cast(:nome as string) is null
                   or lower(u.nome) like lower(concat('%', cast(:nome as string), '%')))
              and (:tipo is null or u.tipo = :tipo)
              and (:setorId is null or s.id = :setorId)
            """)
    Page<Usuario> buscarPorFiltro(@Param("nome") String nome,
                                  @Param("tipo") TipoUsuario tipo,
                                  @Param("setorId") Long setorId,
                                  Pageable pageable);
}
