package br.com.autentication.controller;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.com.autentication.json.TokenResponse;
import br.com.autentication.service.AutenticationService;
import br.com.environment.ApplicationEnvironment;
import br.com.exception.ApplicationException;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.NewCookie.SameSite;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/oauth")
public class AutenticationController {

    @Inject
    private AutenticationService service;

    @ConfigProperty(name = "quarkus.smallrye-jwt.new-token.refresh-token.lifespan")
    private int refreshTokenLifespan;

    @POST
    @Path("/token")
    public Response token(@FormParam("grant_type") String grantType, @FormParam("username") String username,
            @FormParam("password") String password) throws ApplicationException {
        TokenResponse token = this.service.token(grantType, username, password);
        NewCookie cookie = this.createRefreshTokenCookie(token.getRefreshToken());

        return Response.ok().cookie(cookie).entity(token).build();
    }

    @POST
    @Path("/refresh")
    public TokenResponse refresh(@CookieParam("refresh_token") String token) throws ApplicationException {
        return this.service.refresh(token);
    }

    private NewCookie createRefreshTokenCookie(String tokenValue) {
        NewCookie cookie = new NewCookie.Builder("refresh_token")
                .value(tokenValue)
                .path("/api/v1/oauth/refresh")
                .secure(ApplicationEnvironment.isProduction())
                .httpOnly(true)
                .sameSite(SameSite.STRICT)
                .maxAge(this.refreshTokenLifespan)
                .build();

        return cookie;
    }

}
