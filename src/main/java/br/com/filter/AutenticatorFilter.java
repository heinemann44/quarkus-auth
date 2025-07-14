package br.com.filter;

import java.time.LocalDateTime;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import br.com.session.controller.SessionController;
import br.com.session.entity.SessionEntity;
import br.com.session.service.SessionService;
import br.com.user.service.UserService;
import io.netty.util.internal.StringUtil;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AutenticatorFilter {

    @Context
    HttpHeaders httpHeaders;

    @Context
    UriInfo uriInfo;

    @Inject
    JWTParser jwtParser;

    @Inject
    private SessionService sessionService;

    @Inject
    private UserService userService;

    private static final String[] IGNORED_PATHS = { "/api/v1/migrations", "/api/v1/users", "/api/v1/sessions",
            "/api/v1/oauth" };

    @ServerRequestFilter
    public Uni<Response> filter() {
        if (this.isIgnoredPath()) {
            return Uni.createFrom().nullItem();
        }

        boolean isValidSession = this.validateSession();
        boolean isValidBearerToken = this.validateBearerToken();

        if (!isValidSession && !isValidBearerToken) {
            return Uni.createFrom().item(Response.status(Response.Status.FORBIDDEN).build());
        }

        return Uni.createFrom().nullItem();
    }

    private boolean validateBearerToken() {
        String authHeader = this.httpHeaders.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring("Bearer ".length()).trim();

        try {
            JsonWebToken jwt = this.jwtParser.parse(token);

            String email = jwt.getClaim("email");
            this.userService.get(email);

            return true;
        } catch (Exception e) {
            // Token expirado ou usuário não encontrado
            return false;
        }
    }

    private boolean validateSession() {
        Cookie sessionCookie = this.httpHeaders.getCookies().get(SessionController.SESSION_COOKIE_NAME);

        if (sessionCookie == null || StringUtil.isNullOrEmpty(sessionCookie.getValue())) {
            return false;
        }

        SessionEntity sessionEntity = this.sessionService.getSession(sessionCookie.getValue());
        if (sessionEntity == null || sessionEntity.expiresAt.isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    private boolean isIgnoredPath() {
        String path = this.uriInfo.getPath();
        for (String ignoredPath : IGNORED_PATHS) {
            if (path.startsWith(ignoredPath)) {
                return true;
            }
        }
        return false;
    }

}
