package com.conexoessolidarias.service;
import org.springframework.security.authentication.BadCredentialsException;
public class LoginCredentialsException extends BadCredentialsException {
    public LoginCredentialsException(String message) { super(message); }
}
