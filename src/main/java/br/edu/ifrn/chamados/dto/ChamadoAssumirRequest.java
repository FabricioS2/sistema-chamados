package br.edu.ifrn.chamados.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corpo de {@code PATCH /chamados/{id}/assumir}: o tecnico que assume o chamado (regra 3).
 */
public record ChamadoAssumirRequest(

        @NotNull(message = "O tecnicoId é obrigatório.")
        Long tecnicoId
) {
}
