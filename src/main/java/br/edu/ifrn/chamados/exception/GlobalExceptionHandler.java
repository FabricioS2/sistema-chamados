package br.edu.ifrn.chamados.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.List;

/**
 * Tratamento centralizado de erros da API.
 *
 * <p>Todo erro devolve o mesmo JSON padrao ({@link ErroResponse}), com
 * {@code timestamp}, {@code status}, {@code mensagem} e {@code caminho}:</p>
 *
 * <ul>
 *   <li><b>400</b> dados invalidos (campo obrigatorio vazio, e-mail mal formatado, JSON malformado);</li>
 *   <li><b>404</b> recurso inexistente;</li>
 *   <li><b>405</b> metodo HTTP nao suportado pela rota;</li>
 *   <li><b>409</b> violacao de regra de negocio (transicao de status invalida, tecnico de outro setor...);</li>
 *   <li><b>415</b> Content-Type nao suportado;</li>
 *   <li><b>500</b> erro inesperado.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Regra de negocio violada. */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        log.debug("Regra de negocio violada em {}: {}", request.getRequestURI(), ex.getMessage());
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /** Recurso inexistente. */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                            HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** Dados invalidos nao cobertos pelo Bean Validation (validacao entre campos). */
    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResponse> tratarDadosInvalidos(DadosInvalidosException ex,
                                                             HttpServletRequest request) {
        List<ErroResponse.ErroCampo> erros = ex.getCampo() == null
                ? List.of()
                : List.of(new ErroResponse.ErroCampo(ex.getCampo(), ex.getMessage()));
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), request, erros);
    }

    /** Falha do Bean Validation nos corpos de requisicao (@Valid). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
        List<ErroResponse.ErroCampo> erros = ex.getBindingResult().getAllErrors().stream()
                .map(erro -> new ErroResponse.ErroCampo(
                        erro instanceof FieldError fieldError ? fieldError.getField() : erro.getObjectName(),
                        erro.getDefaultMessage()))
                .toList();
        String mensagem = "Falha de validação: %d campo(s) inválido(s) na requisição.".formatted(erros.size());
        return responder(HttpStatus.BAD_REQUEST, mensagem, request, erros);
    }

    /** JSON malformado, campo ausente obrigatorio ou valor nao aceito no corpo. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoIlegivel(HttpMessageNotReadableException ex,
                                                            HttpServletRequest request) {
        log.debug("Corpo ilegivel em {}: {}", request.getRequestURI(), ex.getMessage());
        return responder(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido: verifique o JSON enviado (campo obrigatório ausente, "
                        + "tipo incorreto ou valor não aceito).",
                request);
    }

    /** Parametro de rota ou de query string com tipo/valor invalido (ex.: status inexistente). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                           HttpServletRequest request) {
        StringBuilder mensagem = new StringBuilder(
                "Valor inválido para o parâmetro '%s': '%s'.".formatted(ex.getName(), ex.getValue()));
        Class<?> tipo = ex.getRequiredType();
        if (tipo != null && tipo.isEnum()) {
            mensagem.append(" Valores aceitos: ").append(Arrays.toString(tipo.getEnumConstants())).append('.');
        }
        return responder(HttpStatus.BAD_REQUEST, mensagem.toString(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> tratarParametroAusente(MissingServletRequestParameterException ex,
                                                               HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "O parâmetro obrigatório '%s' não foi informado.".formatted(ex.getParameterName()), request);
    }

    /** Rota inexistente. */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErroResponse> tratarRotaInexistente(Exception ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND,
                "O recurso '%s' não existe nesta API.".formatted(request.getRequestURI()), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest request) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "O método '%s' não é suportado pelo recurso '%s'.".formatted(ex.getMethod(), request.getRequestURI()),
                request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> tratarMediaType(HttpMediaTypeNotSupportedException ex,
                                                        HttpServletRequest request) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content-Type não suportado. Use application/json.", request);
    }

    /**
     * Ultimo recurso do banco: valor unico duplicado ou registro referenciado por outra tabela.
     * As checagens amigaveis ja sao feitas nos services; isto garante que nada vaze como 500.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> tratarIntegridade(DataIntegrityViolationException ex,
                                                          HttpServletRequest request) {
        log.debug("Violação de integridade em {}: {}", request.getRequestURI(), ex.getMessage());
        return responder(HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade dos dados: valor único já cadastrado "
                        + "ou registro referenciado por outros dados.",
                request);
    }

    /** Erro inesperado. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInterno(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado ao processar {} {}", request.getMethod(), request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno inesperado ao processar a requisição.", request);
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem, HttpServletRequest request) {
        return responder(status, mensagem, request, List.of());
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem,
                                                   HttpServletRequest request,
                                                   List<ErroResponse.ErroCampo> erros) {
        ErroResponse corpo = ErroResponse.de(status.value(), mensagem, request.getRequestURI(), erros);
        return ResponseEntity.status(status).body(corpo);
    }
}
