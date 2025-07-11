package br.com.user.service;

import java.util.UUID;

import br.com.exception.NotFoundApplicationException;
import br.com.user.entity.UserEntity;
import br.com.user.json.UserCreate;
import br.com.user.json.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserService {

    @Inject
    private PasswordService passwordService;

    @Transactional
    public UserResponse create(UserCreate userCreate) {
        UserEntity user = new UserEntity();

        user.username = userCreate.getUsername();
        user.email = userCreate.getEmail();
        user.password = this.passwordService.hash(userCreate.getPassword());

        user.persist();

        return this.get(user.id);
    }

    public UserResponse get(UUID id) {
        UserEntity user = UserEntity.findById(id);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.id);
        userResponse.setUsername(user.username);
        userResponse.setEmail(user.email);
        userResponse.setPassword(user.password);
        userResponse.setCreatedAt(user.createdAt);
        userResponse.setUpdatedAt(user.updatedAt);

        return userResponse;
    }

    public UserResponse get(String email) throws NotFoundApplicationException {
        UserEntity user = UserEntity.find("email", email).firstResult();

        if (user == null) {
            throw new NotFoundApplicationException("User not found");
        }

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.id);
        userResponse.setUsername(user.username);
        userResponse.setEmail(user.email);
        userResponse.setPassword(user.password);
        userResponse.setCreatedAt(user.createdAt);
        userResponse.setUpdatedAt(user.updatedAt);

        return userResponse;
    }

}
