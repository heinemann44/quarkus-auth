package br.com.autentication.service;

import java.time.LocalDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.autentication.entity.RefreshTokenEntity;
import br.com.autentication.exception.AutenticationInvalidRequestException;
import br.com.autentication.json.TokenResponse;
import br.com.exception.ApplicationException;
import br.com.session.service.AuthorizationService;
import br.com.user.entity.UserEntity;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AutenticationService {

    @Inject
    private AuthorizationService authorizationService;

    @Inject
    private JWTParser jwtParser;

    @ConfigProperty(name = "quarkus.smallrye-jwt.new-token.lifespan")
    private Long accessTokenLifespan;

    @ConfigProperty(name = "quarkus.smallrye-jwt.new-token.refresh-token.lifespan")
    private Long refreshTokenLifespan;

    @Transactional
    public TokenResponse token(String grantType, String username, String password) throws ApplicationException {
        this.validate(grantType, username, password);
        TokenResponse tokenResponse = null;

        switch (grantType) {
            case "password":
                tokenResponse = this.generateTokenGrantTypePassword(username);
                this.saveTokenResponse(tokenResponse, username);
                break;
            default:
                throw new AutenticationInvalidRequestException("grant_type invalid");
        }

        return tokenResponse;
    }

    private void validate(String grantType, String username, String password) throws ApplicationException {
        if (StringUtil.isNullOrEmpty(grantType)) {
            throw new AutenticationInvalidRequestException("grant_type is required");
        }

        switch (grantType) {
            case "password":
                this.validateGrandTypePassword(username, password);
                break;
            default:
                throw new AutenticationInvalidRequestException("grant_type invalid");
        }
    }

    private void validateGrandTypePassword(String username, String password) throws ApplicationException {
        if (StringUtil.isNullOrEmpty(username)) {
            throw new AutenticationInvalidRequestException("username is required");
        }

        if (StringUtil.isNullOrEmpty(password)) {
            throw new AutenticationInvalidRequestException("password is required");
        }

        this.authorizationService.getAuthenticatedUser(username, password);
    }

    private TokenResponse generateTokenGrantTypePassword(String username) {
        TokenResponse tokenResponse = new TokenResponse();

        String accessToken = Jwt.subject(username).claim("email", username).expiresIn(accessTokenLifespan).sign();
        String refreshToken = Jwt.subject(username).claim("email", username).claim("token_type", "refresh")
                .expiresIn(refreshTokenLifespan)
                .sign();

        tokenResponse.setAccessToken(accessToken);
        tokenResponse.setRefreshToken(refreshToken);
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(accessTokenLifespan);

        return tokenResponse;
    }

    private void saveTokenResponse(TokenResponse tokenResponse, String username) {
        UserEntity userEntity = UserEntity.find("email", username).firstResult();
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();

        refreshTokenEntity.token = tokenResponse.getRefreshToken();
        refreshTokenEntity.user = userEntity;
        refreshTokenEntity.expiresAt = LocalDateTime.now().plusSeconds(this.refreshTokenLifespan);

        refreshTokenEntity.persist();
    }

    public TokenResponse refresh(String refreshToken) throws ApplicationException {
        if (StringUtil.isNullOrEmpty(refreshToken)) {
            throw new AutenticationInvalidRequestException("refresh token is required");
        }
        try {
            JsonWebToken jwt = this.jwtParser.parse(refreshToken);
            String email = jwt.getClaim("email");

            TokenResponse tokenResponse = this.generateTokenGrantTypePassword(email);
            tokenResponse.setRefreshToken(refreshToken);

            return tokenResponse;
        } catch (Exception e) {
            throw new AutenticationInvalidRequestException("refresh token is invalid");
        }
    }

}
