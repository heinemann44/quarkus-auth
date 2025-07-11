package br.com.session.service;

import br.com.exception.NotFoundApplicationException;
import br.com.user.json.UserResponse;
import br.com.user.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthorizationService {

    @Inject
    private UserService userService;

    public UserResponse getAuthenticatedUser(String email, String password) throws NotFoundApplicationException {
        return this.userService.get(email);
    }

}
