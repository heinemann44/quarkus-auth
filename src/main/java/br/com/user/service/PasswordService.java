package br.com.user.service;

import br.com.environment.ApplicationEnvironment;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordService {

    public String hash(String password) {
        return BcryptUtil.bcryptHash(password, ApplicationEnvironment.isProduction() ? 12 : 4);
    }

    public boolean compare(String plainTextPassword, String hashedPassword) {
        return BcryptUtil.matches(plainTextPassword, hashedPassword);
    }

}
