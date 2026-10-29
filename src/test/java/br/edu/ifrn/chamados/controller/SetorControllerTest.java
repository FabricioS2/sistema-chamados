package br.edu.ifrn.chamados.controller;

import br.edu.ifrn.chamados.dto.SetorResponse;
import br.edu.ifrn.chamados.exception.RegraNegocioException;
import br.edu.ifrn.chamados.service.CategoriaService;
import br.edu.ifrn.chamados.service.SetorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada web de setores: criacao (201), atualizacao (200) e exclusao (204/409).
 */
@WebMvcTest(SetorController.class)
class SetorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SetorService setorService;

    @MockitoBean
    private CategoriaService categoriaService;

    @Test
    @DisplayName("POST /setores retorna 201 com Location")
    void criaSetor() throws Exception {
        when(setorService.criar(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new SetorResponse(4L, "Setor Novo", "NOVO"));

        mockMvc.perform(post("/setores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Setor Novo\",\"sigla\":\"NOVO\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/setores/4"))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.sigla").value("NOVO"));
    }

    @Test
    @DisplayName("POST /setores com sigla em branco retorna 400")
    void validacaoDoCorpo() throws Exception {
        mockMvc.perform(post("/setores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"sigla\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.caminho").value("/setores"))
                .andExpect(jsonPath("$.erros.length()").value(2));
    }

    @Test
    @DisplayName("PUT /setores/{id} retorna 200")
    void atualizaSetor() throws Exception {
        when(setorService.atualizar(eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new SetorResponse(1L, "COTIC - atualizado", "COTIC"));

        mockMvc.perform(put("/setores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"COTIC - atualizado\",\"sigla\":\"COTIC\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("COTIC - atualizado"));
    }

    @Test
    @DisplayName("DELETE /setores/{id} sem vinculos retorna 204 sem corpo")
    void excluiSetor() throws Exception {
        doNothing().when(setorService).excluir(4L);

        mockMvc.perform(delete("/setores/4"))
                .andExpect(status().isNoContent())
                .andExpect(header().doesNotExist("Content-Type"));
    }

    @Test
    @DisplayName("DELETE /setores/{id} com chamados vinculados retorna 409 (regra 8)")
    void naoExcluiSetorComChamados() throws Exception {
        doThrow(new RegraNegocioException("Não é permitido excluir um setor com chamados vinculados (regra 8)."))
                .when(setorService).excluir(1L);

        mockMvc.perform(delete("/setores/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("regra 8")))
                .andExpect(jsonPath("$.caminho").value("/setores/1"));
    }

    @Test
    @DisplayName("requisicao sem Content-Type JSON retorna 415")
    void contentTypeInvalido() throws Exception {
        mockMvc.perform(post("/setores")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("nome=Setor"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("application/json")));
    }
}
