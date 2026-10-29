package br.edu.ifrn.chamados.dto;

import br.edu.ifrn.chamados.model.Categoria;

/**
 * Representacao de uma categoria na resposta da API, ja com o setor responsavel.
 */
public record CategoriaResponse(Long id, String nome, String descricao, SetorResponse setor) {

    public static CategoriaResponse de(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                SetorResponse.de(categoria.getSetor()));
    }
}
