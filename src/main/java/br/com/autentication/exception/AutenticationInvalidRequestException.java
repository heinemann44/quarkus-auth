package br.com.autentication.exception;

import org.apache.http.HttpStatus;

import br.com.exception.ApplicationException;
import br.com.exception.annotation.ResponseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@ResponseStatus(HttpStatus.SC_BAD_REQUEST)
public class AutenticationInvalidRequestException extends ApplicationException {

    public AutenticationInvalidRequestException(String message) {
        super(message);
    }

}
