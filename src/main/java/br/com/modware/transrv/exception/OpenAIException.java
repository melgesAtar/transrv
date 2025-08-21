package br.com.modware.transrv.exception;

public class OpenAIException extends RuntimeException {
    public OpenAIException(String message) {
        super(message);
    }
}
