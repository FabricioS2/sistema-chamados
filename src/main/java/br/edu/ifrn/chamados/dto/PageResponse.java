package br.edu.ifrn.chamados.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envelope padrao das respostas de listagem: os itens da pagina e seus metadados.
 *
 * <pre>
 * {
 *   "content": [ ... ],
 *   "page": 0,
 *   "size": 10,
 *   "totalElements": 42,
 *   "totalPages": 5
 * }
 * </pre>
 *
 * @param content       itens da pagina atual
 * @param page          numero da pagina atual (comeca em 0)
 * @param size          tamanho da pagina
 * @param totalElements total de elementos considerando o filtro aplicado
 * @param totalPages    total de paginas
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResponse<T> de(Page<T> pagina) {
        return new PageResponse<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }
}
