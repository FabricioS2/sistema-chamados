package br.edu.ifrn.chamados.controller;

import br.edu.ifrn.chamados.dto.ChamadoFiltro;
import br.edu.ifrn.chamados.dto.InteracaoResponse;
import br.edu.ifrn.chamados.dto.ChamadoResponse;
import br.edu.ifrn.chamados.dto.CategoriaResponse;
import br.edu.ifrn.chamados.dto.SetorResponse;
import br.edu.ifrn.chamados.dto.UsuarioResponse;
import br.edu.ifrn.chamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.chamados.exception.RegraNegocioException;
import br.edu.ifrn.chamados.model.Prioridade;
import br.edu.ifrn.chamados.model.StatusChamado;
import br.edu.ifrn.chamados.model.TipoUsuario;
import br.edu.ifrn.chamados.service.ChamadoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada web de chamados: status HTTP, header Location, validacao do corpo,
 * envelope de paginacao, formato padrao de erro e repasse dos filtros para o service.
 */
@WebMvcTest(ChamadoController.class)
class ChamadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChamadoService chamadoService;

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 1, 8, 30, 0);

    private ChamadoResponse chamadoExemplo() {
        SetorResponse cotic = new SetorResponse(1L, "Coordenação de Tecnologia da Informação e Comunicação", "COTIC");
        CategoriaResponse categoria = new CategoriaResponse(1L, "Rede e Wi-Fi", "Falhas de conectividade", cotic);
        UsuarioResponse maria = new UsuarioResponse(1L, "Maria Silva", "maria.silva@ifrn.edu.br",
                "2019123456", TipoUsuario.SOLICITANTE, null);
        return new ChamadoResponse(10L, "Wi-Fi do laboratório caiu", "Sem internet", "Lab 2",
                Prioridade.MEDIA, StatusChamado.ABERTO, AGORA, null, null, maria, null, categoria, List.of());
    }

    @Test
    @DisplayName("POST /chamados valido retorna 201 com Location")
    void abreChamado() throws Exception {
        when(chamadoService.abrir(any())).thenReturn(chamadoExemplo());

        mockMvc.perform(post("/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Wi-Fi do laboratório caiu","descricao":"Sem internet","local":"Lab 2",
                                 "categoriaId":1,"solicitanteId":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/chamados/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.prioridade").value("MEDIA"))
                .andExpect(jsonPath("$.categoria.setor.sigla").value("COTIC"))
                .andExpect(jsonPath("$.solicitante.nome").value("Maria Silva"));
    }

    @Test
    @DisplayName("POST /chamados sem campos obrigatorios retorna 400 com os campos invalidos")
    void corpoInvalido() throws Exception {
        mockMvc.perform(post("/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"","descricao":"","local":"","categoriaId":null,"solicitanteId":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.caminho").value("/chamados"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("validação")))
                .andExpect(jsonPath("$.erros[*].campo",
                        org.hamcrest.Matchers.hasItems("titulo", "descricao", "local", "categoriaId")));
    }

    @Test
    @DisplayName("violacao de regra de negocio vira 409 com o corpo padronizado")
    void regraDeNegocioVira409() throws Exception {
        when(chamadoService.abrir(any())).thenThrow(
                new RegraNegocioException("Apenas usuários do tipo SOLICITANTE podem abrir chamados (regra 2)."));

        mockMvc.perform(post("/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"T","descricao":"D","local":"L","categoriaId":1,"solicitanteId":4}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        org.hamcrest.Matchers.containsString("SOLICITANTE")))
                .andExpect(jsonPath("$.caminho").value("/chamados"));
    }

    @Test
    @DisplayName("GET /chamados devolve os itens e os metadados da pagina")
    void listagemPaginada() throws Exception {
        when(chamadoService.listar(any(), any()))
                .thenReturn(new PageImpl<>(List.of(chamadoExemplo()), PageRequest.of(0, 10), 42));

        mockMvc.perform(get("/chamados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(42))
                .andExpect(jsonPath("$.totalPages").value(5))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(10));
    }

    @Test
    @DisplayName("GET /chamados repassa os filtros da query string para o service")
    void repassaFiltros() throws Exception {
        when(chamadoService.listar(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/chamados")
                        .param("status", "ABERTO")
                        .param("prioridade", "ALTA")
                        .param("setorId", "1")
                        .param("categoriaId", "2")
                        .param("solicitanteId", "3")
                        .param("tecnicoId", "4")
                        .param("dataInicio", "2026-01-01")
                        .param("dataFim", "2026-01-31")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "dataAbertura,desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<ChamadoFiltro> filtro = ArgumentCaptor.forClass(ChamadoFiltro.class);
        ArgumentCaptor<Pageable> paginacao = ArgumentCaptor.forClass(Pageable.class);
        verify(chamadoService).listar(filtro.capture(), paginacao.capture());

        assertThat(filtro.getValue().status()).isEqualTo(StatusChamado.ABERTO);
        assertThat(filtro.getValue().prioridade()).isEqualTo(Prioridade.ALTA);
        assertThat(filtro.getValue().setorId()).isEqualTo(1L);
        assertThat(filtro.getValue().categoriaId()).isEqualTo(2L);
        assertThat(filtro.getValue().solicitanteId()).isEqualTo(3L);
        assertThat(filtro.getValue().tecnicoId()).isEqualTo(4L);
        assertThat(filtro.getValue().dataInicio()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(filtro.getValue().dataFim()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(paginacao.getValue().getPageSize()).isEqualTo(10);
        assertThat(paginacao.getValue().getSort().isSorted()).isTrue();
    }

    @Test
    @DisplayName("filtro de status invalido retorna 400 listando os valores aceitos")
    void filtroInvalido() throws Exception {
        mockMvc.perform(get("/chamados").param("status", "URGENTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(
                        org.hamcrest.Matchers.containsString("ABERTO, EM_ATENDIMENTO, RESOLVIDO, CANCELADO")));
    }

    @Test
    @DisplayName("GET /chamados/{id} inexistente retorna 404 padronizado")
    void chamadoNaoEncontrado() throws Exception {
        when(chamadoService.buscarPorId(999L)).thenThrow(RecursoNaoEncontradoException.de("Chamado", 999L));

        mockMvc.perform(get("/chamados/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Chamado com id 999 não encontrado."))
                .andExpect(jsonPath("$.caminho").value("/chamados/999"));
    }

    @Test
    @DisplayName("PATCH /chamados/{id}/assumir retorna 200 com o chamado em atendimento")
    void assumeChamado() throws Exception {
        ChamadoResponse emAtendimento = new ChamadoResponse(10L, "Wi-Fi caiu", "Sem internet", "Lab 2",
                Prioridade.MEDIA, StatusChamado.EM_ATENDIMENTO, AGORA, null, null,
                chamadoExemplo().solicitante(),
                new UsuarioResponse(4L, "Carlos Pereira", "carlos@ifrn.edu.br", "2015001122",
                        TipoUsuario.TECNICO, new SetorResponse(1L, "Coordenação de Tecnologia da Informação e Comunicação", "COTIC")),
                chamadoExemplo().categoria(), List.of());
        when(chamadoService.assumir(eq(10L), any())).thenReturn(emAtendimento);

        mockMvc.perform(patch("/chamados/10/assumir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tecnicoId\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"))
                .andExpect(jsonPath("$.tecnico.nome").value("Carlos Pereira"));
    }

    @Test
    @DisplayName("PATCH /chamados/{id}/resolver sem solucao retorna 400")
    void resolucaoObrigatoria() throws Exception {
        mockMvc.perform(patch("/chamados/10/resolver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tecnicoId\":4,\"solucao\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("solucao"));
    }

    @Test
    @DisplayName("PATCH em chamado encerrado retorna 409 (regra 6)")
    void chamadoEncerradoNaoAceitaAlteracao() throws Exception {
        when(chamadoService.cancelar(eq(10L), any())).thenThrow(new RegraNegocioException(
                "Chamados RESOLVIDO ou CANCELADO não aceitam alterações (regra 6)."));

        mockMvc.perform(patch("/chamados/10/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"solicitanteId\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("regra 6")));
    }

    @Test
    @DisplayName("POST /chamados/{id}/interacoes retorna 201 com Location")
    void registraInteracao() throws Exception {
        when(chamadoService.registrarInteracao(eq(10L), any())).thenReturn(
                new InteracaoResponse(100L, "A caminho do laboratório.", AGORA, chamadoExemplo().solicitante()));

        mockMvc.perform(post("/chamados/10/interacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"A caminho do laboratório.\",\"autorId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/chamados/10"))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.autor.nome").value("Maria Silva"));
    }
}
