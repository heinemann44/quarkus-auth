package br.com.exception;

import org.apache.http.HttpStatus;

import br.com.exception.annotation.ResponseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@ResponseStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)
public class ApplicationException extends ApplicationInterfaceException {

    public int status;

    public String subType;

    public ApplicationException(String message) {
        super(message);
    }

}