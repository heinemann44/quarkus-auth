package br.com.exception;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.reactive.ResponseStatus;

import br.com.exception.domain.ApplicationErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RestExceptionHandler implements ExceptionMapper<ApplicationException> {

    @Override
    public Response toResponse(ApplicationException exception) {
        ApplicationErrorResponse response = this.getApplicationErrorResponse(exception);

        return Response.status(response.getStatus()).entity(response).build();
    }

    private ApplicationErrorResponse getApplicationErrorResponse(ApplicationException exception) {
        ApplicationErrorResponse response = new ApplicationErrorResponse();

        response.setMessage(exception.getMessage());
        response.setStatus(this.getHttpStatus(exception.getClass()));
        response.setSubType(exception.getSubType());

        return response;
    }

    private Integer getHttpStatus(final Class<? extends Exception> classe) {
        final ResponseStatus responseStatus = classe.getAnnotation(ResponseStatus.class);
        if (responseStatus == null) {
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }

        return responseStatus.value();
    }

}
