package br.com.ajasoftware.clinica.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

    private final String field;

    public BusinessException(String message) {
        super(message);
        this.field = "global";
    }

    public BusinessException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}