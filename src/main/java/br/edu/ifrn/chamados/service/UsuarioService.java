package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.UsuarioRequest;
import br.edu.ifrn.chamados.dto.UsuarioResponse;
import br.edu.ifrn.chamados.exception.DadosInvalidosException;
import br.edu.ifrn.chamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.chamados.exception.RegraNegocioException;
import br.edu.ifrn.chamados.model.Setor;
import br.edu.ifrn.chamados.model.TipoUsuario;
import br.edu.ifrn.chamados.model.Usuario;
import br.edu.ifrn.chamados.repository.SetorRepository;
import br.edu.ifrn.chamados.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio do cadastro de usuarios.
 *
 * <p>E-mail duplicado retorna 409. Tecnico precisa estar lotado em um setor;
 * solicitante nao pertence a setor.</p>
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SetorRepository setorRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, SetorRepository setorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.setorRepository = setorRepository;
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        String email = normalizar(request.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com o e-mail '%s'.".formatted(email));
        }
        Usuario usuario = usuarioRepository.save(new Usuario(
                normalizar(request.nome()),
                email,
                normalizar(request.matricula()),
                request.tipo(),
                resolverSetor(request.tipo(), request.setorId())));
        return UsuarioResponse.de(usuario);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(String nome, TipoUsuario tipo, Long setorId, Pageable pageable) {
        if (setorId != null && !setorRepository.existsById(setorId)) {
            throw RecursoNaoEncontradoException.de("Setor", setorId);
        }
        return usuarioRepository
                .buscarPorFiltro(filtroOpcional(nome), tipo, setorId, pageable)
                .map(UsuarioResponse::de);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.de(carregar(id));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = carregar(id);
        String email = normalizar(request.email());
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new RegraNegocioException("O e-mail '%s' já pertence a outro usuário.".formatted(email));
        }
        usuario.setNome(normalizar(request.nome()));
        usuario.setEmail(email);
        usuario.setMatricula(normalizar(request.matricula()));
        usuario.setTipo(request.tipo());
        usuario.setSetor(resolverSetor(request.tipo(), request.setorId()));
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    private Usuario carregar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Usuário", id));
    }

    /**
     * Tecnico pertence a um setor (obrigatorio); solicitante nao pertence a setor.
     */
    private Setor resolverSetor(TipoUsuario tipo, Long setorId) {
        if (tipo == TipoUsuario.TECNICO) {
            if (setorId == null) {
                throw new DadosInvalidosException("setorId",
                        "Um usuário do tipo TECNICO precisa estar vinculado a um setor.");
            }
            return setorRepository.findById(setorId)
                    .orElseThrow(() -> RecursoNaoEncontradoException.de("Setor", setorId));
        }
        if (setorId != null) {
            throw new DadosInvalidosException("setorId",
                    "Somente usuários do tipo TECNICO são vinculados a um setor.");
        }
        return null;
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String filtroOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
