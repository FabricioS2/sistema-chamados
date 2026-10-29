package br.edu.ifrn.chamados.model;

/**
 * Prioridade de um chamado. Assume apenas BAIXA, MEDIA ou ALTA.
 *
 * <p>Quando o solicitante nao informa a prioridade, o sistema aplica {@link #MEDIA}
 * (regra de negocio 1).</p>
 */
public enum Prioridade {
    BAIXA,
    MEDIA,
    ALTA
}
