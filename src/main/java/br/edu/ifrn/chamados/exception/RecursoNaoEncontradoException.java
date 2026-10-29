package br.edu.ifrn.chamados.exception;

/**
 * Lancada quando um recurso (setor, categoria, usuario, chamado...) nao existe.
 * Traduzida para <b>HTTP 404</b> pelo {@link GlobalExceptionHandler}.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    /**
     * Monta a mensagem padrao "Recurso com id X não encontrado.".
     *
     * @param recurso nome do recurso, ex.: "Setor", "Categoria", "Usuário", "Chamado"
     * @param id      identificador procurado
     */
    public static RecursoNaoEncontradoException de(String recurso, Object id) {
        return new RecursoNaoEncontradoException("%s com id %s não encontrado.".formatted(recurso, id));
    }
}
