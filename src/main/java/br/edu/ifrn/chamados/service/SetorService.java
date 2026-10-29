package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.SetorRequest;
import br.edu.ifrn.chamados.dto.SetorResponse;
import br.edu.ifrn.chamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.chamados.exception.RegraNegocioException;
import br.edu.ifrn.chamados.model.Setor;
import br.edu.ifrn.chamados.repository.CategoriaRepository;
import br.edu.ifrn.chamados.repository.ChamadoRepository;
import br.edu.ifrn.chamados.repository.SetorRepository;
import br.edu.ifrn.chamados.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio do cadastro de setores.
 *
 * <p>Nenhuma regra fica no controller: ele apenas recebe a requisicao e devolve o que este
 * service produz. Injecao de dependencia por construtor (sem {@code @Autowired} em atributo).</p>
 */
@Service
public class SetorService {

    private final SetorRepository setorRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChamadoRepository chamadoRepository;

    public SetorService(SetorRepository setorRepository,
                        CategoriaRepository categoriaRepository,
                        UsuarioRepository usuarioRepository,
                        ChamadoRepository chamadoRepository) {
        this.setorRepository = setorRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional
    public SetorResponse criar(SetorRequest request) {
        String sigla = normalizar(request.sigla());
        if (setorRepository.existsBySiglaIgnoreCase(sigla)) {
            throw new RegraNegocioException("Já existe um setor cadastrado com a sigla '%s'.".formatted(sigla));
        }
        Setor setor = setorRepository.save(new Setor(normalizar(request.nome()), sigla));
        return SetorResponse.de(setor);
    }

    @Transactional(readOnly = true)
    public Page<SetorResponse> listar(String nome, Pageable pageable) {
        return setorRepository.buscarPorFiltro(filtroOpcional(nome), pageable).map(SetorResponse::de);
    }

    @Transactional(readOnly = true)
    public SetorResponse buscarPorId(Long id) {
        return SetorResponse.de(carregar(id));
    }

    @Transactional
    public SetorResponse atualizar(Long id, SetorRequest request) {
        Setor setor = carregar(id);
        String sigla = normalizar(request.sigla());
        if (setorRepository.existsBySiglaIgnoreCaseAndIdNot(sigla, id)) {
            throw new RegraNegocioException("A sigla '%s' já pertence a outro setor.".formatted(sigla));
        }
        setor.setNome(normalizar(request.nome()));
        setor.setSigla(sigla);
        return SetorResponse.de(setorRepository.save(setor));
    }

    /**
     * Regra 8: nao e permitido excluir setor que tenha chamados vinculados.
     * Tambem sao bloqueadas exclusoes que violariam as chaves estrangeiras
     * (setor com categorias cadastradas ou com tecnicos lotados).
     */
    @Transactional
    public void excluir(Long id) {
        Setor setor = carregar(id);
        if (chamadoRepository.existsByCategoriaSetorId(id)) {
            throw new RegraNegocioException(
                    "Não é permitido excluir um setor com chamados vinculados (regra 8).");
        }
        if (categoriaRepository.existsBySetorId(id)) {
            throw new RegraNegocioException(
                    "Não é permitido excluir um setor que ainda possui categorias cadastradas.");
        }
        if (usuarioRepository.existsBySetorId(id)) {
            throw new RegraNegocioException(
                    "Não é permitido excluir um setor que ainda possui técnicos vinculados.");
        }
        setorRepository.delete(setor);
    }

    private Setor carregar(Long id) {
        return setorRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Setor", id));
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    /** Filtro em branco e tratado como "nao informado" e, portanto, ignorado. */
    private String filtroOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
