package br.edu.ifrn.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corpo de {@code POST /chamados/{id}/interacoes}. Sem autenticacao, o autor da interacao
 * vai no corpo e precisa ser o solicitante ou o tecnico atribuido (regra 7).
 */
public record InteracaoRequest(

        @NotBlank(message = "O texto da interação é obrigatório.")
        @Size(max = 2000, message = "O texto da interação deve ter no máximo 2000 caracteres.")
        String texto,

        @NotNull(message = "O autorId é obrigatório.")
        Long autorId
) {
}
