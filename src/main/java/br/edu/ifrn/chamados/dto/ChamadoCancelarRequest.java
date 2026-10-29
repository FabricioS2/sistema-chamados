package br.edu.ifrn.chamados.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corpo de {@code PATCH /chamados/{id}/cancelar}: somente o proprio solicitante pode cancelar,
 * e apenas enquanto o chamado estiver ABERTO (regra 5).
 */
public record ChamadoCancelarRequest(

        @NotNull(message = "O solicitanteId é obrigatório.")
        Long solicitanteId
) {
}
