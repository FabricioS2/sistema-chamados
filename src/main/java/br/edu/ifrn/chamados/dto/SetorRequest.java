package br.edu.ifrn.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo das requisicoes de criacao (POST) e atualizacao (PUT) de setor.
 */
public record SetorRequest(

        @NotBlank(message = "O nome do setor é obrigatório.")
        @Size(max = 120, message = "O nome do setor deve ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "A sigla do setor é obrigatória.")
        @Size(max = 20, message = "A sigla do setor deve ter no máximo 20 caracteres.")
        String sigla
) {
}
