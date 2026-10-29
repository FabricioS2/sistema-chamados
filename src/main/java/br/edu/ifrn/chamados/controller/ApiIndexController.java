package br.edu.ifrn.chamados.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Indice da API em {@code GET /}: conveniencia extra (fora do escopo minimo do enunciado)
 * para descobrir as rotas disponiveis sem precisar abrir o codigo ou o README.
 */
@RestController
public class ApiIndexController {

    @GetMapping(path = "/", produces = "application/json")
    public Map<String, Object> index() {
        // LinkedHashMap para o JSON sair sempre na mesma ordem de leitura
        Map<String, Object> indice = new LinkedHashMap<>();
        indice.put("api", "Sistema de Chamados de Suporte do IFRN");
        indice.put("descricao", "API REST para abertura, atendimento e histórico de chamados de suporte.");
        indice.put("stack", "Java 21 + Spring Boot 4 + Spring Data JPA + PostgreSQL + Flyway");
        indice.put("documentacao", "Veja o README.md e o arquivo requisicoes.http na raiz do repositório.");
        indice.put("paginacao", "Use ?page=0&size=10&sort=dataAbertura,desc (tamanho padrão 10, máximo 50).");
        indice.put("erros", "Toda falha devolve JSON com timestamp, status, mensagem e caminho.");

        Map<String, Object> recursos = new LinkedHashMap<>();
        recursos.put("setores", List.of(
                rota("POST", "/setores", "cria um setor"),
                rota("GET", "/setores", "lista paginada (filtro: nome)"),
                rota("GET", "/setores/{id}", "detalha um setor"),
                rota("PUT", "/setores/{id}", "atualiza um setor"),
                rota("DELETE", "/setores/{id}", "exclui um setor (409 se houver chamados vinculados)"),
                rota("GET", "/setores/{id}/categorias", "categorias do setor, paginadas")));
        recursos.put("categorias", List.of(
                rota("POST", "/categorias", "cria uma categoria em um setor"),
                rota("GET", "/categorias", "lista paginada (filtros: nome, setorId)"),
                rota("GET", "/categorias/{id}", "detalha uma categoria"),
                rota("PUT", "/categorias/{id}", "atualiza uma categoria"),
                rota("DELETE", "/categorias/{id}", "exclui uma categoria (409 se houver chamados)")));
        recursos.put("usuarios", List.of(
                rota("POST", "/usuarios", "cria um usuário (409 se o e-mail já existir)"),
                rota("GET", "/usuarios", "lista paginada (filtros: nome, tipo, setorId)"),
                rota("GET", "/usuarios/{id}", "detalha um usuário"),
                rota("PUT", "/usuarios/{id}", "atualiza um usuário"),
                rota("GET", "/usuarios/{id}/chamados", "chamados abertos pelo usuário ou atribuídos a ele (filtro: status)")));
        recursos.put("chamados", List.of(
                rota("POST", "/chamados", "abre um chamado"),
                rota("GET", "/chamados", "lista paginada (filtros: status, prioridade, setorId, categoriaId, "
                        + "solicitanteId, tecnicoId, dataInicio, dataFim)"),
                rota("GET", "/chamados/{id}", "detalha o chamado com o histórico de interações"),
                rota("PATCH", "/chamados/{id}/assumir", "técnico assume o chamado (ABERTO -> EM_ATENDIMENTO)"),
                rota("PATCH", "/chamados/{id}/resolver", "técnico atribuído resolve o chamado (-> RESOLVIDO)"),
                rota("PATCH", "/chamados/{id}/cancelar", "solicitante cancela o chamado (ABERTO -> CANCELADO)"),
                rota("POST", "/chamados/{id}/interacoes", "registra uma interação no chamado")));
        indice.put("recursos", recursos);

        return indice;
    }

    private Map<String, String> rota(String metodo, String caminho, String descricao) {
        Map<String, String> rota = new LinkedHashMap<>();
        rota.put("metodo", metodo);
        rota.put("caminho", caminho);
        rota.put("descricao", descricao);
        return rota;
    }
}
