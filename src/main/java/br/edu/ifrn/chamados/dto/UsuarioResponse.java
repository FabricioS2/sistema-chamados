package br.edu.ifrn.chamados.dto;

import br.edu.ifrn.chamados.model.TipoUsuario;
import br.edu.ifrn.chamados.model.Usuario;

/**
 * Representacao de um usuario na resposta da API. O setor vem {@code null} para solicitantes.
 */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String matricula,
        TipoUsuario tipo,
        SetorResponse setor
) {

    public static UsuarioResponse de(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getMatricula(),
                usuario.getTipo(),
                SetorResponse.de(usuario.getSetor()));
    }
}
