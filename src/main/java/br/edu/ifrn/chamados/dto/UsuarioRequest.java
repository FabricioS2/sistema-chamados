package br.edu.ifrn.chamados.dto;

import br.edu.ifrn.chamados.model.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corpo das requisicoes de criacao (POST) e atualizacao (PUT) de usuario.
 *
 * <p>{@code setorId} e obrigatorio quando {@code tipo} for {@code TECNICO}
 * (regra validada na camada de service, pois depende de outro campo).</p>
 */
public record UsuarioRequest(

        @NotBlank(message = "O nome do usuário é obrigatório.")
        @Size(max = 120, message = "O nome do usuário deve ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado é inválido.")
        @Size(max = 160, message = "O e-mail deve ter no máximo 160 caracteres.")
        String email,

        @NotBlank(message = "A matrícula é obrigatória.")
        @Size(max = 30, message = "A matrícula deve ter no máximo 30 caracteres.")
        String matricula,

        @NotNull(message = "O tipo do usuário é obrigatório (SOLICITANTE ou TECNICO).")
        TipoUsuario tipo,

        Long setorId
) {
}
