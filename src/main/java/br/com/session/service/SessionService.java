package br.com.session.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

import br.com.session.entity.SessionEntity;
import br.com.session.json.SessionCreate;
import br.com.session.json.SessionResponse;
import br.com.user.json.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SessionService {

    @Inject
    private AuthorizationService authorizationService;

    public static final int EXPIRATION_IN_SECONDS = 60 * 60 * 24 * 30; // 30 days

    @Transactional
    public SessionResponse create(SessionCreate sessionCreate) throws Exception {
        UserResponse authenticatedUser = this.authorizationService.getAuthenticatedUser(sessionCreate.getEmail(),
                sessionCreate.getPassword());
        String token = this.generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(EXPIRATION_IN_SECONDS);

        SessionResponse sessionResponse = new SessionResponse();
        sessionResponse.setToken(token);
        sessionResponse.setExpiresAt(expiresAt);
        sessionResponse.setUserId(authenticatedUser.getId());

        this.saveSession(sessionResponse);

        return sessionResponse;
    }

    private void saveSession(SessionResponse sessionResponse) {
        SessionEntity sessionEntity = new SessionEntity();

        sessionEntity.token = sessionResponse.getToken();
        sessionEntity.expiresAt = sessionResponse.getExpiresAt();
        sessionEntity.user = sessionResponse.getUserId();

        sessionEntity.persist();
    }

    private String generateToken() throws NoSuchAlgorithmException {
        byte[] bytes = new byte[48];
        SecureRandom.getInstanceStrong().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public SessionEntity getSession(String token) {
        return SessionEntity.find("token", token).firstResult();
    }
}
