package br.edu.ifrn.chamados.dto;

import br.edu.ifrn.chamados.model.Setor;

/**
 * Representacao de um setor na resposta da API.
 */
public record SetorResponse(Long id, String nome, String sigla) {

    public static SetorResponse de(Setor setor) {
        if (setor == null) {
            return null;
        }
        return new SetorResponse(setor.getId(), setor.getNome(), setor.getSigla());
    }
}
