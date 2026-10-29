package br.edu.ifrn.chamados.dto;

import br.edu.ifrn.chamados.model.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corpo da requisicao de abertura de chamado (POST /chamados).
 *
 * <p>Sem autenticacao, o autor da acao vai no corpo: {@code solicitanteId}.
 * Se {@code prioridade} nao for informada, o sistema aplica {@code MEDIA} (regra 1).</p>
 */
public record ChamadoRequest(

        @NotBlank(message = "O título do chamado é obrigatório.")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
        String titulo,

        @NotBlank(message = "A descrição do problema é obrigatória.")
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
        String descricao,

        @NotBlank(message = "O local do atendimento é obrigatório.")
        @Size(max = 120, message = "O local deve ter no máximo 120 caracteres.")
        String local,

        Prioridade prioridade,

        @NotNull(message = "O categoriaId é obrigatório.")
        Long categoriaId,

        @NotNull(message = "O solicitanteId é obrigatório.")
        Long solicitanteId
) {
}
