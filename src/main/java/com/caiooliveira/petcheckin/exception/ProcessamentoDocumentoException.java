package com.caiooliveira.petcheckin.exception;

public class ProcessamentoDocumentoException extends RuntimeException {

    public ProcessamentoDocumentoException(String mensagem) {
        super(mensagem);
    }

    public ProcessamentoDocumentoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}