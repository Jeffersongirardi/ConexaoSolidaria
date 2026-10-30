package com.conexoessolidarias.service;

/** Conta temporariamente bloqueada por excesso de tentativas de login. */
public class ContaBloqueadaException extends RuntimeException {
    public ContaBloqueadaException(String message) {
        super(message);
    }
}
