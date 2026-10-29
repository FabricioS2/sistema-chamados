package br.edu.ifrn.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corpo de {@code PATCH /chamados/{id}/resolver}: somente o tecnico atribuido pode resolver
 * e a solucao e obrigatoria (regra 4).
 */
public record ChamadoResolverRequest(

        @NotNull(message = "O tecnicoId é obrigatório.")
        Long tecnicoId,

        @NotBlank(message = "A solução é obrigatória ao resolver o chamado.")
        @Size(max = 2000, message = "A solução deve ter no máximo 2000 caracteres.")
        String solucao
) {
}
