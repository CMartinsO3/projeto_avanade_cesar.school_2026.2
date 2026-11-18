package com.caiooliveira.petcheckin.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.caiooliveira.petcheckin.dto.ErroApiResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 - pet, reserva ou documento não encontrado
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroApiResponse tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return new ErroApiResponse(404, ex.getMessage(), null);
    }

    // 400 - violou uma regra de negócio (ex.: check-out antes do check-in)
    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroApiResponse tratarRegraNegocio(RegraNegocioException ex) {
        return new ErroApiResponse(400, ex.getMessage(), null);
    }

    // 400 - falhou em alguma validação do DTO (@NotBlank, @NotNull, @Past...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroApiResponse tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erroDoCampo : ex.getBindingResult().getFieldErrors()) {
            campos.put(erroDoCampo.getField(), erroDoCampo.getDefaultMessage());
        }
        return new ErroApiResponse(400, "Dados inválidos", campos);
    }

    // 400 - JSON mal escrito, data em formato errado ou espécie inexistente
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroApiResponse tratarJsonInvalido(HttpMessageNotReadableException ex) {
        return new ErroApiResponse(400,
                "Corpo da requisição inválido. Verifique o formato do JSON, "
                        + "as datas (aaaa-mm-dd) e a espécie (CACHORRO ou GATO).",
                null);
    }

    // 400 - parâmetro na URL com tipo errado (ex.: /api/pets/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroApiResponse tratarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return new ErroApiResponse(400,
                "Valor inválido para o parâmetro '" + ex.getName() + "': " + ex.getValue(),
                null);
    }

    // 404 - rota que não existe (ex.: /api/gatos)
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroApiResponse tratarRotaInexistente(HttpServletRequest request) {
        return new ErroApiResponse(404, "Rota não encontrada: " + request.getRequestURI(), null);
    }

    // 405 - método HTTP não aceito na rota (ex.: DELETE em /api/pets/1)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErroApiResponse tratarMetodoNaoPermitido(HttpRequestMethodNotSupportedException ex) {
        return new ErroApiResponse(405,
                "O método " + ex.getMethod() + " não é permitido nesta rota.",
                null);
    }

    // 400 - faltou o arquivo ou o tipo no envio do documento
    @ExceptionHandler({ MissingServletRequestPartException.class, MissingServletRequestParameterException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroApiResponse tratarCampoAusente(Exception ex) {
        return new ErroApiResponse(400,
                "Requisição incompleta. Envie o arquivo no campo 'arquivo' e o tipo no campo 'tipo' "
                        + "(CARTEIRA_VACINACAO ou ATESTADO_VETERINARIO).",
                null);
    }

    // 413 - arquivo maior que o limite configurado
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroApiResponse> tratarArquivoGrande(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(413)
                .body(new ErroApiResponse(413, "Arquivo muito grande. O tamanho máximo é 10MB.", null));
    }

    // Qualquer outro erro
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroApiResponse> tratarErroInesperado(Exception ex) {
        if (ex instanceof ErrorResponse erroDoSpring) {
            int status = erroDoSpring.getStatusCode().value();
            return ResponseEntity.status(status)
                    .body(new ErroApiResponse(status, erroDoSpring.getBody().getDetail(), null));
        }

        log.error("Erro inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroApiResponse(500, "Erro interno inesperado. Tente novamente mais tarde.", null));
    }
}