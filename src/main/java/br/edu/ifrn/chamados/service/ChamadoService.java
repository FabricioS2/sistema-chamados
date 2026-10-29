package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.ChamadoAssumirRequest;
import br.edu.ifrn.chamados.dto.ChamadoCancelarRequest;
import br.edu.ifrn.chamados.dto.ChamadoFiltro;
import br.edu.ifrn.chamados.dto.ChamadoRequest;
import br.edu.ifrn.chamados.dto.ChamadoResolverRequest;
import br.edu.ifrn.chamados.dto.ChamadoResponse;
import br.edu.ifrn.chamados.dto.InteracaoRequest;
import br.edu.ifrn.chamados.dto.InteracaoResponse;
import br.edu.ifrn.chamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.chamados.exception.RegraNegocioException;
import br.edu.ifrn.chamados.model.Categoria;
import br.edu.ifrn.chamados.model.Chamado;
import br.edu.ifrn.chamados.model.Interacao;
import br.edu.ifrn.chamados.model.Prioridade;
import br.edu.ifrn.chamados.model.Setor;
import br.edu.ifrn.chamados.model.StatusChamado;
import br.edu.ifrn.chamados.model.Usuario;
import br.edu.ifrn.chamados.repository.CategoriaRepository;
import br.edu.ifrn.chamados.repository.ChamadoRepository;
import br.edu.ifrn.chamados.repository.InteracaoRepository;
import br.edu.ifrn.chamados.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Coracao do sistema: concentra o ciclo de vida do chamado e as regras de negocio 1 a 7
 * do enunciado. Nenhuma regra fica nos controllers.
 *
 * <p>Como o projeto nao usa autenticacao, o autor de cada acao chega no corpo da requisicao
 * ({@code solicitanteId}, {@code tecnicoId} ou {@code autorId}) e e validado aqui.</p>
 */
@Service
public class ChamadoService {

    /** Limite superior do dia para o filtro por periodo de abertura (precision de microssegundos). */
    private static final LocalTime FIM_DO_DIA = LocalTime.of(23, 59, 59, 999_999_000);

    private final ChamadoRepository chamadoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InteracaoRepository interacaoRepository;

    public ChamadoService(ChamadoRepository chamadoRepository,
                          CategoriaRepository categoriaRepository,
                          UsuarioRepository usuarioRepository,
                          InteracaoRepository interacaoRepository) {
        this.chamadoRepository = chamadoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.interacaoRepository = interacaoRepository;
    }

    /**
     * Abre um chamado (regras 1 e 2).
     *
     * <p>Regra 1: nasce com status ABERTO, dataAbertura preenchida pelo sistema e
     * prioridade MEDIA quando nao informada. Regra 2: apenas SOLICITANTE abre chamado.</p>
     */
    @Transactional
    public ChamadoResponse abrir(ChamadoRequest request) {
        Usuario solicitante = carregarUsuario(request.solicitanteId());
        if (!solicitante.isSolicitante()) {
            throw new RegraNegocioException(
                    "Apenas usuários do tipo SOLICITANTE podem abrir chamados (regra 2). O usuário '%s' é %s."
                            .formatted(solicitante.getNome(), solicitante.getTipo()));
        }
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Categoria", request.categoriaId()));

        Prioridade prioridade = request.prioridade() != null ? request.prioridade() : Prioridade.MEDIA;

        Chamado chamado = new Chamado(
                normalizar(request.titulo()),
                normalizar(request.descricao()),
                normalizar(request.local()),
                prioridade,
                solicitante,
                categoria);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now());

        return ChamadoResponse.de(chamadoRepository.save(chamado));
    }

    /** GET /chamados: listagem paginada com filtros combinaveis. */
    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listar(ChamadoFiltro filtro, Pageable pageable) {
        LocalDateTime inicio = filtro.dataInicio() == null ? null : filtro.dataInicio().atStartOfDay();
        LocalDateTime fim = filtro.dataFim() == null ? null : filtro.dataFim().atTime(FIM_DO_DIA);

        return chamadoRepository.buscarPorFiltro(
                        filtro.status(),
                        filtro.prioridade(),
                        filtro.setorId(),
                        filtro.categoriaId(),
                        filtro.solicitanteId(),
                        filtro.tecnicoId(),
                        inicio != null,
                        inicio,
                        fim != null,
                        fim,
                        pageable)
                .map(ChamadoResponse::de);
    }

    /** GET /chamados/{id}: detalha o chamado com o historico de interacoes. */
    @Transactional(readOnly = true)
    public ChamadoResponse buscarPorId(Long id) {
        Chamado chamado = carregar(id);
        List<Interacao> interacoes = interacaoRepository.findByChamadoIdOrderByDataHoraAsc(id);
        return ChamadoResponse.de(chamado, interacoes);
    }

    /** GET /usuarios/{id}/chamados: chamados abertos pelo usuario ou atribuidos a ele. */
    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listarPorUsuario(Long usuarioId, StatusChamado status, Pageable pageable) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw RecursoNaoEncontradoException.de("Usuário", usuarioId);
        }
        return chamadoRepository.buscarPorUsuario(usuarioId, status, pageable).map(ChamadoResponse::de);
    }

    /**
     * PATCH /chamados/{id}/assumir (regra 3): um tecnico so pode assumir um chamado ABERTO
     * cuja categoria pertenga ao seu setor. Ao assumir, o status vira EM_ATENDIMENTO.
     */
    @Transactional
    public ChamadoResponse assumir(Long id, ChamadoAssumirRequest request) {
        Chamado chamado = carregar(id);
        Usuario tecnico = carregarUsuario(request.tecnicoId());

        if (!tecnico.isTecnico()) {
            throw new RegraNegocioException(
                    "Somente usuários do tipo TECNICO podem assumir chamados. O usuário '%s' é %s."
                            .formatted(tecnico.getNome(), tecnico.getTipo()));
        }
        exigirTransicao(chamado, StatusChamado.EM_ATENDIMENTO);

        if (tecnico.getSetor() == null) {
            throw new RegraNegocioException(
                    "O técnico '%s' não está lotado em nenhum setor e não pode assumir chamados."
                            .formatted(tecnico.getNome()));
        }
        Setor setorResponsavel = chamado.getSetorResponsavel();
        if (!Objects.equals(tecnico.getSetor().getId(), setorResponsavel.getId())) {
            throw new RegraNegocioException(
                    "O chamado pertence à categoria '%s', do setor %s. O técnico '%s' é lotado no setor %s (regra 3)."
                            .formatted(chamado.getCategoria().getNome(),
                                    setorResponsavel.getSigla(),
                                    tecnico.getNome(),
                                    tecnico.getSetor().getSigla()));
        }

        chamado.setTecnico(tecnico);
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        return ChamadoResponse.de(chamadoRepository.save(chamado));
    }

    /**
     * PATCH /chamados/{id}/resolver (regras 4 e 6): somente o tecnico atribuido pode resolver,
     * a solucao e obrigatoria e dataFechamento e preenchida.
     */
    @Transactional
    public ChamadoResponse resolver(Long id, ChamadoResolverRequest request) {
        Chamado chamado = carregar(id);
        Usuario tecnico = carregarUsuario(request.tecnicoId());

        if (!tecnico.isTecnico()) {
            throw new RegraNegocioException(
                    "Somente usuários do tipo TECNICO podem resolver chamados. O usuário '%s' é %s."
                            .formatted(tecnico.getNome(), tecnico.getTipo()));
        }
        exigirTransicao(chamado, StatusChamado.RESOLVIDO);

        if (chamado.getTecnico() == null || !Objects.equals(chamado.getTecnico().getId(), tecnico.getId())) {
            throw new RegraNegocioException(
                    "Somente o técnico atribuído ao chamado pode resolvê-lo (regra 4).");
        }

        chamado.setStatus(StatusChamado.RESOLVIDO);
        chamado.setSolucao(normalizar(request.solucao()));
        chamado.setDataFechamento(LocalDateTime.now());
        return ChamadoResponse.de(chamadoRepository.save(chamado));
    }

    /**
     * PATCH /chamados/{id}/cancelar (regras 5 e 6): somente o proprio solicitante pode cancelar,
     * e apenas enquanto o chamado estiver ABERTO.
     */
    @Transactional
    public ChamadoResponse cancelar(Long id, ChamadoCancelarRequest request) {
        Chamado chamado = carregar(id);
        Usuario solicitante = carregarUsuario(request.solicitanteId());

        if (!Objects.equals(chamado.getSolicitante().getId(), solicitante.getId())) {
            throw new RegraNegocioException(
                    "Somente o solicitante que abriu o chamado pode cancelá-lo (regra 5).");
        }
        exigirTransicao(chamado, StatusChamado.CANCELADO);

        chamado.setStatus(StatusChamado.CANCELADO);
        chamado.setDataFechamento(LocalDateTime.now());
        return ChamadoResponse.de(chamadoRepository.save(chamado));
    }

    /**
     * POST /chamados/{id}/interacoes (regras 6 e 7): chamados encerrados nao aceitam novas
     * interacoes e apenas o solicitante ou o tecnico atribuido podem registrar andamento.
     */
    @Transactional
    public InteracaoResponse registrarInteracao(Long chamadoId, InteracaoRequest request) {
        Chamado chamado = carregar(chamadoId);
        Usuario autor = carregarUsuario(request.autorId());

        if (chamado.getStatus().isEncerrado()) {
            throw new RegraNegocioException(
                    "Chamado com status %s não aceita novas interações (regra 6).".formatted(chamado.getStatus()));
        }
        boolean ehSolicitante = Objects.equals(chamado.getSolicitante().getId(), autor.getId());
        boolean ehTecnicoAtribuido = chamado.getTecnico() != null
                && Objects.equals(chamado.getTecnico().getId(), autor.getId());
        if (!ehSolicitante && !ehTecnicoAtribuido) {
            throw new RegraNegocioException(
                    "Apenas o solicitante ou o técnico atribuído podem registrar interações neste chamado (regra 7).");
        }

        Interacao interacao = interacaoRepository.save(
                new Interacao(normalizar(request.texto()), LocalDateTime.now(), chamado, autor));
        return InteracaoResponse.de(interacao);
    }

    /**
     * Aplica a maquina de estados do chamado. Transicoes permitidas:
     * ABERTO -&gt; EM_ATENDIMENTO -&gt; RESOLVIDO e ABERTO -&gt; CANCELADO.
     */
    private void exigirTransicao(Chamado chamado, StatusChamado destino) {
        StatusChamado atual = chamado.getStatus();
        if (atual == destino) {
            throw new RegraNegocioException(atual.isEncerrado()
                    ? "Chamado com status %s não aceita alterações (regra 6).".formatted(atual)
                    : "O chamado já está com status %s.".formatted(atual));
        }
        if (!atual.permiteTransicaoPara(destino)) {
            String detalhe = atual.isEncerrado()
                    ? "Chamados RESOLVIDO ou CANCELADO não aceitam alterações (regra 6)."
                    : "Ciclo de vida permitido: ABERTO -> EM_ATENDIMENTO -> RESOLVIDO, e ABERTO -> CANCELADO.";
            throw new RegraNegocioException(
                    "Transição de status inválida: %s -> %s. %s".formatted(atual, destino, detalhe));
        }
    }

    private Chamado carregar(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Chamado", id));
    }

    private Usuario carregarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Usuário", id));
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
