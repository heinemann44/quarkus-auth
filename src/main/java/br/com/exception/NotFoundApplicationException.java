package br.com.exception;

import org.apache.http.HttpStatus;

import br.com.exception.annotation.ResponseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@ResponseStatus(HttpStatus.SC_NOT_FOUND)
public class NotFoundApplicationException extends ApplicationException {

    public NotFoundApplicationException(String message) {
        super(message);
    }

}
