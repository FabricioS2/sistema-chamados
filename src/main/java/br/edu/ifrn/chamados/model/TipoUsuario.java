package br.edu.ifrn.chamados.model;

/**
 * Tipo de usuario do sistema.
 *
 * <ul>
 *   <li>{@link #SOLICITANTE} - servidor ou aluno que abre chamados;</li>
 *   <li>{@link #TECNICO} - usuario lotado em um setor que assume e resolve chamados.</li>
 * </ul>
 */
public enum TipoUsuario {
    SOLICITANTE,
    TECNICO;

    public boolean isTecnico() {
        return this == TECNICO;
    }

    public boolean isSolicitante() {
        return this == SOLICITANTE;
    }
}
