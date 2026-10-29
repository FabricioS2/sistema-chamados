package br.edu.ifrn.chamados.exception;

/**
 * Lancada para dados invalidos que nao sao cobertos pelas anotacoes do Bean Validation,
 * tipicamente validacoes que dependem de mais de um campo (ex.: tecnico sem setor).
 * Traduzida para <b>HTTP 400</b> pelo {@link GlobalExceptionHandler}.
 */
public class DadosInvalidosException extends RuntimeException {

    private final String campo;

    public DadosInvalidosException(String mensagem) {
        this(null, mensagem);
    }

    public DadosInvalidosException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
