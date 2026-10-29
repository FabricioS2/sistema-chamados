package br.edu.ifrn.chamados.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Status de um chamado e maquina de estados do ciclo de vida.
 *
 * <p>Transicoes permitidas:</p>
 * <pre>
 *   ABERTO          -&gt; EM_ATENDIMENTO  (tecnico assume)
 *   ABERTO          -&gt; CANCELADO       (solicitante cancela)
 *   EM_ATENDIMENTO  -&gt; RESOLVIDO       (tecnico atribuido resolve)
 * </pre>
 * <p>Nenhuma outra transicao e permitida.</p>
 */
public enum StatusChamado {

    ABERTO,
    EM_ATENDIMENTO,
    RESOLVIDO,
    CANCELADO;

    /** Chamados encerrados nao aceitam alteracoes nem novas interacoes (regra 6). */
    public boolean isEncerrado() {
        return this == RESOLVIDO || this == CANCELADO;
    }

    /**
     * Verifica se a transicao deste status para {@code destino} faz parte do ciclo de vida.
     *
     * @param destino status pretendido
     * @return {@code true} se a transicao for permitida
     */
    public boolean permiteTransicaoPara(StatusChamado destino) {
        return transicoesPermitidas().contains(destino);
    }

    private Set<StatusChamado> transicoesPermitidas() {
        return switch (this) {
            case ABERTO -> EnumSet.of(EM_ATENDIMENTO, CANCELADO);
            case EM_ATENDIMENTO -> EnumSet.of(RESOLVIDO);
            case RESOLVIDO, CANCELADO -> EnumSet.noneOf(StatusChamado.class);
        };
    }
}
