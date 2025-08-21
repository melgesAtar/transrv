package br.com.modware.transrv.exception;

public class AgentNotFoundException extends RuntimeException {
    public AgentNotFoundException(String message) {
        super(message);
    }
}
