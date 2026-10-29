package br.edu.ifrn.chamados.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Corpo padronizado de todas as respostas de erro da API.
 *
 * <pre>
 * {
 *   "timestamp": "2026-10-29T14:03:11.512",
 *   "status": 409,
 *   "mensagem": "Somente o tecnico atribuido pode resolver o chamado.",
 *   "caminho": "/chamados/3/resolver",
 *   "erros": []
 * }
 * </pre>
 *
 * @param timestamp momento em que o erro foi gerado
 * @param status    codigo HTTP
 * @param mensagem  mensagem legivel do erro
 * @param caminho   path da requisicao que originou o erro
 * @param erros     detalhes por campo (somente para erros de validacao); vazio nos demais casos
 */
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String mensagem,
        String caminho,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<ErroCampo> erros
) {

    public static ErroResponse de(int status, String mensagem, String caminho) {
        return new ErroResponse(LocalDateTime.now(), status, mensagem, caminho, List.of());
    }

    public static ErroResponse de(int status, String mensagem, String caminho, List<ErroCampo> erros) {
        return new ErroResponse(LocalDateTime.now(), status, mensagem, caminho, erros);
    }

    /**
     * Detalhe de um campo invalido.
     *
     * @param campo   nome do campo (ou parametro) com problema
     * @param mensagem motivo da rejeicao
     */
    public record ErroCampo(String campo, String mensagem) {
    }
}
