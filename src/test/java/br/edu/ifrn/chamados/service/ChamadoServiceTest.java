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
import br.edu.ifrn.chamados.model.TipoUsuario;
import br.edu.ifrn.chamados.model.Usuario;
import br.edu.ifrn.chamados.repository.CategoriaRepository;
import br.edu.ifrn.chamados.repository.ChamadoRepository;
import br.edu.ifrn.chamados.repository.InteracaoRepository;
import br.edu.ifrn.chamados.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes das regras de negocio do chamado (regras 1 a 7 do enunciado), todas na camada de service.
 */
@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private InteracaoRepository interacaoRepository;

    @InjectMocks
    private ChamadoService service;

    private Setor cotic;
    private Setor manutencao;
    private Categoria redeWifi;
    private Categoria projetores;
    private Usuario maria;
    private Usuario carlos;
    private Usuario jose;
    private Chamado chamado;

    @BeforeEach
    void preparar() {
        cotic = new Setor("Coordenação de Tecnologia da Informação e Comunicação", "COTIC");
        cotic.setId(1L);
        manutencao = new Setor("Coordenação de Manutenção e Infraestrutura", "MANUT");
        manutencao.setId(2L);

        redeWifi = new Categoria("Rede e Wi-Fi", "Falhas de conectividade", cotic);
        redeWifi.setId(1L);
        projetores = new Categoria("Projetores e Equipamentos de Sala", "Projetor das salas", manutencao);
        projetores.setId(4L);

        maria = new Usuario("Maria Silva", "maria@ifrn.edu.br", "2019123456", TipoUsuario.SOLICITANTE, null);
        maria.setId(1L);
        carlos = new Usuario("Carlos Pereira", "carlos@ifrn.edu.br", "2015001122", TipoUsuario.TECNICO, cotic);
        carlos.setId(4L);
        jose = new Usuario("José Santos", "jose@ifrn.edu.br", "2016004455", TipoUsuario.TECNICO, manutencao);
        jose.setId(6L);

        chamado = new Chamado("Wi-Fi do laboratório caiu", "Sem internet no laboratório", "Lab 2",
                Prioridade.MEDIA, maria, redeWifi);
        chamado.setId(10L);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now().minusDays(1));
    }

    private void stubChamado() {
        when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamado));
    }

    private void stubSave() {
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Nested
    @DisplayName("Regra 1 e 2 - abertura de chamado")
    class Abertura {

        @Test
        @DisplayName("chamado nasce ABERTO, com dataAbertura preenchida pelo sistema e prioridade MEDIA quando omitida")
        void aplicaPadroesDaRegra1() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(maria));
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(redeWifi));
            stubSave();

            ChamadoResponse resposta = service.abrir(new ChamadoRequest(
                    "Wi-Fi caiu", "Sem internet", "Lab 2", null, 1L, 1L));

            assertThat(resposta.status()).isEqualTo(StatusChamado.ABERTO);
            assertThat(resposta.prioridade()).isEqualTo(Prioridade.MEDIA);
            assertThat(resposta.dataAbertura()).isNotNull();
            assertThat(resposta.dataFechamento()).isNull();
            assertThat(resposta.tecnico()).isNull();
            assertThat(resposta.solicitante().id()).isEqualTo(1L);
            assertThat(resposta.categoria().setor().sigla()).isEqualTo("COTIC");
        }

        @Test
        @DisplayName("prioridade informada e respeitada")
        void usaPrioridadeInformada() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(maria));
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(redeWifi));
            stubSave();

            ChamadoResponse resposta = service.abrir(new ChamadoRequest(
                    "Urgente", "Parou tudo", "Lab 2", Prioridade.ALTA, 1L, 1L));

            assertThat(resposta.prioridade()).isEqualTo(Prioridade.ALTA);
        }

        @Test
        @DisplayName("regra 2: apenas SOLICITANTE pode abrir chamado")
        void tecnicoNaoAbreChamado() {
            when(usuarioRepository.findById(4L)).thenReturn(Optional.of(carlos));

            assertThatThrownBy(() -> service.abrir(new ChamadoRequest(
                    "Título", "Descrição", "Local", null, 1L, 4L)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("SOLICITANTE");

            verify(chamadoRepository, never()).save(any());
        }

        @Test
        @DisplayName("categoria inexistente retorna 404")
        void categoriaInexistente() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(maria));
            when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.abrir(new ChamadoRequest(
                    "Título", "Descrição", "Local", null, 999L, 1L)))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("Categoria");
        }

        @Test
        @DisplayName("solicitante inexistente retorna 404")
        void solicitanteInexistente() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.abrir(new ChamadoRequest(
                    "Título", "Descrição", "Local", null, 1L, 999L)))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("Usuário");
        }
    }

    @Nested
    @DisplayName("Regra 3 - assumir chamado")
    class Assumir {

        @Test
        @DisplayName("tecnico do setor responsavel assume e o status vira EM_ATENDIMENTO")
        void tecnicoDoSetorAssume() {
            stubChamado();
            when(usuarioRepository.findById(4L)).thenReturn(Optional.of(carlos));
            stubSave();

            ChamadoResponse resposta = service.assumir(10L, new ChamadoAssumirRequest(4L));

            assertThat(resposta.status()).isEqualTo(StatusChamado.EM_ATENDIMENTO);
            assertThat(resposta.tecnico().id()).isEqualTo(4L);
            assertThat(resposta.tecnico().setor().sigla()).isEqualTo("COTIC");
        }

        @Test
        @DisplayName("tecnico de outro setor nao pode assumir")
        void tecnicoDeOutroSetor() {
            stubChamado();
            when(usuarioRepository.findById(6L)).thenReturn(Optional.of(jose));

            assertThatThrownBy(() -> service.assumir(10L, new ChamadoAssumirRequest(6L)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("MANUT");

            assertThat(chamado.getStatus()).isEqualTo(StatusChamado.ABERTO);
            assertThat(chamado.getTecnico()).isNull();
        }

        @Test
        @DisplayName("solicitante nao pode assumir chamado")
        void solicitanteNaoAssume() {
            stubChamado();
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(maria));

            assertThatThrownBy(() -> service.assumir(10L, new ChamadoAssumirRequest(1L)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("TECNICO");
        }

        @Test
        @DisplayName("somente chamado ABERTO pode ser assumido")
        void chamadoJaEmAtendimento() {
            chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
            chamado.setTecnico(carlos);
            stubChamado();
            when(usuarioRepository.findById(4L)).thenReturn(Optional.of(carlos));

            assertThatThrownBy(() -> service.assumir(10L, new ChamadoAssumirRequest(4L)))
                    .isInstanceOf(RegraNegocioException.class);
        }

        @Test
        @DisplayName("chamado encerrado nao aceita alteracao (regra 6)")
        void chamadoResolvidoNaoAceitaAssumir() {
            chamado.setStatus(StatusChamado.RESOLVIDO);
            chamado.setTecnico(carlos);
            stubChamado();
            when(usuarioRepository.findById(4L)).thenReturn(Optional.of(carlos));

            assertThatThrownBy(() -> service.assumir(10L, new ChamadoAssumirRequest(4L)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("regra 6");
        }

        @Test
        @DisplayName("chamado inexistente retorna 404")
        void chamadoInexistente() {
            when(chamadoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.assumir(999L, new ChamadoAssumirRequest(4L)))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("Regra 4 - resolver chamado")
    class Resolver {

        private void assumirAntes() {
            chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
            chamado.setTecnico(carlos);
        }

        @Test
        @DisplayName("tecnico atribuido resolve, grava a solucao e preenche dataFechamento")
        void tecnicoAtribuidoResolve() {
            assumirAntes();
            stubChamado();
            when(usuarioRepository.findById(4L)).thenReturn(Optional.of(carlos));
            stubSave();

            ChamadoResponse resposta = service.resolver(10L,
                    new ChamadoResolverRequest(4L, "Roteador reiniciado e configurado"));

            assertThat(resposta.status()).isEqualTo(StatusChamado.RESOLVIDO);
            assertThat(resposta.solucao()).isEqualTo("Roteador reiniciado e configurado");
            assertThat(resposta.dataFechamento()).isNotNull();
        }

        @Test
        @DisplayName("outro tecnico nao pode resolver chamado ja atribuido")
        void outroTecnicoNaoResolve() {
            assumirAntes();
            stubChamado();
            when(usuarioRepository.findById(6L)).thenReturn(Optional.of(jose));

            assertThatThrownBy(() -> service.resolver(10L,
                    new ChamadoResolverRequest(6L, "Tentativa indevida")))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("técnico atribuído");

            assertThat(chamado.getStatus()).isEqualTo(StatusChamado.EM_ATENDIMENTO);
        }

        @Test
        @DisplayName("chamado ABERTO precisa ser assumido antes de ser resolvido")
        void chamadoAbertoNaoResolveDireto() {
            stubChamado();
            when(usuarioRepository.findById(4L)).thenReturn(Optional.of(carlos));

            assertThatThrownBy(() -> service.resolver(10L, new ChamadoResolverRequest(4L, "Solução")))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("ABERTO -> RESOLVIDO");
        }

        @Test
        @DisplayName("chamado RESOLVIDO nao pode ser resolvido de novo (regra 6)")
        void chamadoJaResolvido() {
            assumirAntes();
            chamado.setStatus(StatusChamado.RESOLVIDO);
            stubChamado();
            when(usuarioRepository.findById(4L)).thenReturn(Optional.of(carlos));

            assertThatThrownBy(() -> service.resolver(10L, new ChamadoResolverRequest(4L, "De novo")))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("regra 6");
        }
    }

    @Nested
    @DisplayName("Regra 5 - cancelar chamado")
    class Cancelar {

        @Test
        @DisplayName("solicitante cancela chamado ABERTO e dataFechamento e preenchida")
        void solicitanteCancela() {
            stubChamado();
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(maria));
            stubSave();

            ChamadoResponse resposta = service.cancelar(10L, new ChamadoCancelarRequest(1L));

            assertThat(resposta.status()).isEqualTo(StatusChamado.CANCELADO);
            assertThat(resposta.dataFechamento()).isNotNull();
        }

        @Test
        @DisplayName("quem nao e o solicitante nao pode cancelar")
        void outroUsuarioNaoCancela() {
            stubChamado();
            when(usuarioRepository.findById(3L)).thenReturn(Optional.of(
                    new Usuario("Outra Pessoa", "outra@ifrn.edu.br", "777", TipoUsuario.SOLICITANTE, null)));

            assertThatThrownBy(() -> service.cancelar(10L, new ChamadoCancelarRequest(3L)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("solicitante");
        }

        @Test
        @DisplayName("so e possivel cancelar enquanto o chamado estiver ABERTO")
        void chamadoEmAtendimentoNaoCancela() {
            chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
            chamado.setTecnico(carlos);
            stubChamado();
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(maria));

            assertThatThrownBy(() -> service.cancelar(10L, new ChamadoCancelarRequest(1L)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("EM_ATENDIMENTO -> CANCELADO");
        }
    }

    @Nested
    @DisplayName("Regras 6 e 7 - interacoes")
    class Interacoes {

        @Test
        @DisplayName("solicitante registra interacao em chamado aberto")
        void solicitanteRegistraInteracao() {
            stubChamado();
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(maria));
            when(interacaoRepository.save(any(Interacao.class))).thenAnswer(inv -> {
                Interacao i = inv.getArgument(0);
                i.setId(100L);
                return i;
            });

            InteracaoResponse resposta = service.registrarInteracao(10L,
                    new InteracaoRequest("Já reiniciamos o roteador, sem sucesso.", 1L));

            assertThat(resposta.id()).isEqualTo(100L);
            assertThat(resposta.autor().id()).isEqualTo(1L);
            assertThat(resposta.dataHora()).isNotNull();

            ArgumentCaptor<Interacao> captor = ArgumentCaptor.forClass(Interacao.class);
            verify(interacaoRepository).save(captor.capture());
            assertThat(captor.getValue().getChamado().getId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("tecnico atribuido tambem pode registrar interacao")
        void tecnicoAtribuidoRegistraInteracao() {
            chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
            chamado.setTecnico(carlos);
            stubChamado();
            when(usuarioRepository.findById(4L)).thenReturn(Optional.of(carlos));
            when(interacaoRepository.save(any(Interacao.class))).thenAnswer(inv -> inv.getArgument(0));

            InteracaoResponse resposta = service.registrarInteracao(10L, new InteracaoRequest("A caminho.", 4L));

            assertThat(resposta.texto()).isEqualTo("A caminho.");
        }

        @Test
        @DisplayName("regra 7: quem nao participa do chamado nao registra interacao")
        void estranhoNaoRegistraInteracao() {
            stubChamado();
            when(usuarioRepository.findById(6L)).thenReturn(Optional.of(jose));

            assertThatThrownBy(() -> service.registrarInteracao(10L, new InteracaoRequest("Palpite", 6L)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("regra 7");

            verify(interacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("regra 6: chamado CANCELADO nao aceita novas interacoes")
        void chamadoCanceladoNaoAceitaInteracao() {
            chamado.setStatus(StatusChamado.CANCELADO);
            stubChamado();
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(maria));

            assertThatThrownBy(() -> service.registrarInteracao(10L, new InteracaoRequest("Oi?", 1L)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("regra 6");
        }
    }

    @Nested
    @DisplayName("Consultas e filtros")
    class Consultas {

        @Test
        @DisplayName("filtro por periodo vira os limites do dia e as flags correspondentes")
        void convertePeriodoParaLimitesDoDia() {
            when(chamadoRepository.buscarPorFiltro(any(), any(), any(), any(), any(), any(),
                    anyBoolean(), any(), anyBoolean(), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(chamado)));

            Page<ChamadoResponse> pagina = service.listar(
                    new ChamadoFiltro(StatusChamado.ABERTO, null, 1L, null, null, null,
                            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
                    PageRequest.of(0, 10));

            assertThat(pagina.getTotalElements()).isEqualTo(1);
            assertThat(pagina.getContent().get(0).id()).isEqualTo(10L);

            verify(chamadoRepository).buscarPorFiltro(
                    eq(StatusChamado.ABERTO), isNull(), eq(1L), isNull(), isNull(), isNull(),
                    eq(true), eq(LocalDate.of(2026, 1, 1).atStartOfDay()),
                    eq(true), eq(LocalDate.of(2026, 1, 31).atTime(23, 59, 59, 999_999_000)),
                    any(Pageable.class));
        }

        @Test
        @DisplayName("sem periodo informado, as flags de data vao desligadas")
        void semPeriodoAsFlagsVaoDesligadas() {
            when(chamadoRepository.buscarPorFiltro(any(), any(), any(), any(), any(), any(),
                    anyBoolean(), any(), anyBoolean(), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            service.listar(ChamadoFiltro.vazio(), PageRequest.of(0, 10));

            verify(chamadoRepository).buscarPorFiltro(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    eq(false), isNull(), eq(false), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("usuario inexistente em GET /usuarios/{id}/chamados retorna 404")
        void usuarioInexistenteNaListagemPorUsuario() {
            when(usuarioRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.listarPorUsuario(999L, null, PageRequest.of(0, 10)))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }

        @Test
        @DisplayName("detalhe do chamado traz o historico de interacoes ordenado pelo repository")
        void detalheComInteracoes() {
            stubChamado();
            Interacao interacao = new Interacao("Abertura registrada", LocalDateTime.now(), chamado, maria);
            interacao.setId(1L);
            when(interacaoRepository.findByChamadoIdOrderByDataHoraAsc(10L)).thenReturn(List.of(interacao));

            ChamadoResponse resposta = service.buscarPorId(10L);

            assertThat(resposta.interacoes()).hasSize(1);
            assertThat(resposta.interacoes().get(0).autor().nome()).isEqualTo("Maria Silva");
        }

        @Test
        @DisplayName("listagem sem historico de interacoes (interacoes vazias)")
        void listagemNaoCarregaInteracoes() {
            when(chamadoRepository.buscarPorFiltro(any(), any(), any(), any(), any(), any(),
                    anyBoolean(), any(), anyBoolean(), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(chamado)));

            Page<ChamadoResponse> pagina = service.listar(ChamadoFiltro.vazio(), PageRequest.of(0, 10));

            assertThat(pagina.getContent().get(0).interacoes()).isEmpty();
        }
    }
}
