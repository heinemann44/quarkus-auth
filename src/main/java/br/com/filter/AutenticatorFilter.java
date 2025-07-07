package br.com.filter;

import java.time.LocalDateTime;

import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import br.com.session.controller.SessionController;
import br.com.session.entity.SessionEntity;
import br.com.session.service.SessionService;
import io.netty.util.internal.StringUtil;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AutenticatorFilter {

    @Context
    HttpHeaders httpHeaders;

    @Inject
    private SessionService sessionService;

    @ServerRequestFilter
    public Uni<Response> filter() {
        Cookie cookie = this.httpHeaders.getCookies().get(SessionController.SESSION_COOKIE_NAME);

        if (cookie == null || StringUtil.isNullOrEmpty(cookie.getValue())) {
            return Uni.createFrom().item(Response.status(Response.Status.FORBIDDEN).build());
        }

        SessionEntity sessionEntity = this.sessionService.getSession(cookie.getValue());
        if (sessionEntity == null || sessionEntity.expiresAt.isBefore(LocalDateTime.now())) {
            return Uni.createFrom().item(Response.status(Response.Status.FORBIDDEN).build());
        }

        return Uni.createFrom().nullItem();
    }

}
