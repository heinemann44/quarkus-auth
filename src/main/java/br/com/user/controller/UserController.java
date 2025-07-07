package br.com.user.controller;

import br.com.user.json.UserCreate;
import br.com.user.json.UserResponse;
import br.com.user.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/api/v1/users")
public class UserController {

    @Inject
    private UserService service;

    @POST
    public UserResponse create(UserCreate userCreate) {
        return this.service.create(userCreate);
    }

}
