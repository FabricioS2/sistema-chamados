package br.edu.ifrn.chamados.exception;

/**
 * Lancada quando uma regra de negocio do dominio e violada (transicao de status invalida,
 * tecnico de outro setor, e-mail duplicado, exclusao de setor/categoria com chamados vinculados...).
 * Traduzida para <b>HTTP 409</b> pelo {@link GlobalExceptionHandler}.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
