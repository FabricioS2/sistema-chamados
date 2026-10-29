package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.CategoriaRequest;
import br.edu.ifrn.chamados.dto.CategoriaResponse;
import br.edu.ifrn.chamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.chamados.exception.RegraNegocioException;
import br.edu.ifrn.chamados.model.Categoria;
import br.edu.ifrn.chamados.model.Setor;
import br.edu.ifrn.chamados.repository.CategoriaRepository;
import br.edu.ifrn.chamados.repository.ChamadoRepository;
import br.edu.ifrn.chamados.repository.SetorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio do cadastro de categorias. Toda categoria pertence a um setor,
 * que passa a ser o setor responsavel pelos chamados abertos nela.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final SetorRepository setorRepository;
    private final ChamadoRepository chamadoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            SetorRepository setorRepository,
                            ChamadoRepository chamadoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.setorRepository = setorRepository;
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        Setor setor = carregarSetor(request.setorId());
        Categoria categoria = categoriaRepository.save(
                new Categoria(normalizar(request.nome()), normalizar(request.descricao()), setor));
        return CategoriaResponse.de(categoria);
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(String nome, Long setorId, Pageable pageable) {
        if (setorId != null && !setorRepository.existsById(setorId)) {
            throw RecursoNaoEncontradoException.de("Setor", setorId);
        }
        return categoriaRepository
                .buscarPorFiltro(filtroOpcional(nome), setorId, pageable)
                .map(CategoriaResponse::de);
    }

    /** GET /setores/{id}/categorias: categorias de um setor. */
    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listarPorSetor(Long setorId, Pageable pageable) {
        carregarSetor(setorId);
        return categoriaRepository.findBySetorId(setorId, pageable).map(CategoriaResponse::de);
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        return CategoriaResponse.de(carregar(id));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = carregar(id);
        Setor setor = carregarSetor(request.setorId());
        categoria.setNome(normalizar(request.nome()));
        categoria.setDescricao(normalizar(request.descricao()));
        categoria.setSetor(setor);
        return CategoriaResponse.de(categoriaRepository.save(categoria));
    }

    /** Regra 8: nao e permitido excluir categoria que tenha chamados vinculados. */
    @Transactional
    public void excluir(Long id) {
        Categoria categoria = carregar(id);
        if (chamadoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException(
                    "Não é permitido excluir uma categoria com chamados vinculados (regra 8).");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria carregar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Categoria", id));
    }

    private Setor carregarSetor(Long id) {
        return setorRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Setor", id));
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String filtroOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
