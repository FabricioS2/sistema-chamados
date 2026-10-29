package br.edu.ifrn.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corpo das requisicoes de criacao (POST) e atualizacao (PUT) de categoria.
 *
 * @param setorId setor responsavel pelos chamados desta categoria
 */
public record CategoriaRequest(

        @NotBlank(message = "O nome da categoria é obrigatório.")
        @Size(max = 120, message = "O nome da categoria deve ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "A descrição da categoria é obrigatória.")
        @Size(max = 500, message = "A descrição da categoria deve ter no máximo 500 caracteres.")
        String descricao,

        @NotNull(message = "O setorId é obrigatório: toda categoria pertence a um setor.")
        Long setorId
) {
}
