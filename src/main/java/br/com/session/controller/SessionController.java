package br.com.session.controller;

import java.util.List;

import br.com.environment.ApplicationEnvironment;
import br.com.session.entity.SessionEntity;
import br.com.session.json.SessionCreate;
import br.com.session.json.SessionResponse;
import br.com.session.service.SessionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/sessions")
public class SessionController {

    @Inject
    private SessionService service;

    public static String SESSION_COOKIE_NAME = "session_id";

    @POST
    public Response create(SessionCreate sessionCreate) throws Exception {
        SessionResponse sessionResponse = this.service.create(sessionCreate);

        NewCookie cookie = new NewCookie.Builder(SESSION_COOKIE_NAME)
                .value(sessionResponse.getToken())
                .path("/")
                .secure(ApplicationEnvironment.isProduction())
                .httpOnly(true)
                .maxAge(SessionService.EXPIRATION_IN_SECONDS)
                .build();

        return Response.ok().cookie(cookie).entity(sessionResponse).build();
    }

    @GET
    public List<SessionEntity> getAll() {
        return SessionEntity.listAll();
    }

}
