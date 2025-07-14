package br.com.autentication.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Random;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.autentication.entity.PreAuthTokenEntity;
import br.com.autentication.entity.RefreshTokenEntity;
import br.com.autentication.exception.AutenticationInvalidRequestException;
import br.com.autentication.json.LoginRequest;
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

        String accessToken = this.generateJwt(username);
        String refreshToken = this.generateJwtRefresh(username);

        tokenResponse.setAccessToken(accessToken);
        tokenResponse.setRefreshToken(refreshToken);
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(accessTokenLifespan);

        return tokenResponse;
    }

    private String generateJwt(String username) {
        return Jwt.subject(username).claim("email", username).expiresIn(accessTokenLifespan).sign();
    }

    private String generateJwtRefresh(String username) {
        return Jwt.subject(username).claim("email", username).claim("token_type", "refresh")
                .expiresIn(refreshTokenLifespan).sign();
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

    @Transactional
    public String login(LoginRequest loginRequest) throws ApplicationException {
        this.validateGrandTypePassword(loginRequest.getLogin(), loginRequest.getPassword());

        UserEntity userEntity = UserEntity.find("email", loginRequest.getLogin()).firstResult();
        String preAuthToken = this.generatePreAuthToken();
        String pin = this.generateRandomPin();

        this.sendPinToEmail(pin, userEntity.email);

        this.savePreAuthToken(preAuthToken, userEntity, pin);

        return preAuthToken;
    }

    private void sendPinToEmail(String pin, String email) {
        // TODO
        System.out.println("PIN " + pin);
    }

    private void savePreAuthToken(String preAuthToken, UserEntity userEntity, String pin) {
        PreAuthTokenEntity preAuthTokenEntity = new PreAuthTokenEntity();
        preAuthTokenEntity.token = preAuthToken;
        preAuthTokenEntity.user = userEntity;
        preAuthTokenEntity.pin = pin;
        preAuthTokenEntity.expiresAt = LocalDateTime.now().plusMinutes(5);
        preAuthTokenEntity.persist();
    }

    private String generateRandomPin() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        return String.format("%06d", number);
    }

    private String generatePreAuthToken() throws ApplicationException {
        try {
            byte[] bytes = new byte[48];
            SecureRandom.getInstanceStrong().nextBytes(bytes);
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new ApplicationException("Error to generate pre auth token");
        }
    }

    @Transactional
    public TokenResponse token(String preAuthToken, String pin) throws AutenticationInvalidRequestException {
        PreAuthTokenEntity preAuthTokenEntity = PreAuthTokenEntity.find("token", preAuthToken).firstResult();

        if (preAuthTokenEntity == null) {
            throw new AutenticationInvalidRequestException("pre_auth_token is invalid");
        }

        if (preAuthTokenEntity.expiresAt.isBefore(LocalDateTime.now())) {
            throw new AutenticationInvalidRequestException("pre_auth_token is expired");
        }

        if (preAuthTokenEntity.used) {
            throw new AutenticationInvalidRequestException("pre_auth_token is used");
        }

        if (preAuthTokenEntity.pin.equals(pin)) {
            preAuthTokenEntity.used = true;
            preAuthTokenEntity.persist();
        } else {
            throw new AutenticationInvalidRequestException("pin is invalid");
        }

        String username = preAuthTokenEntity.user.email;
        TokenResponse tokenResponse = this.generateTokenGrantTypePassword(username);
        this.saveTokenResponse(tokenResponse, username);

        return tokenResponse;
    }

}
