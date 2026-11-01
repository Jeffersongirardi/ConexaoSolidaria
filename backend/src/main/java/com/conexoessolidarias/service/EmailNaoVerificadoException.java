package com.conexoessolidarias.service;

/** Lançada quando a ação exige e-mail verificado e a conta ainda não confirmou. */
public class EmailNaoVerificadoException extends RuntimeException {
    public EmailNaoVerificadoException(String message) {
        super(message);
    }
}
