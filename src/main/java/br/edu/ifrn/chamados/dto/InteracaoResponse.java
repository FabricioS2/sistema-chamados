package br.edu.ifrn.chamados.dto;

import br.edu.ifrn.chamados.model.Interacao;

import java.time.LocalDateTime;

/**
 * Interacao (andamento) registrada no historico do chamado.
 */
public record InteracaoResponse(
        Long id,
        String texto,
        LocalDateTime dataHora,
        UsuarioResponse autor
) {

    public static InteracaoResponse de(Interacao interacao) {
        return new InteracaoResponse(
                interacao.getId(),
                interacao.getTexto(),
                interacao.getDataHora(),
                UsuarioResponse.de(interacao.getAutor()));
    }
}
