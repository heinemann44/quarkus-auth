package br.com.environment;

import io.quarkus.runtime.configuration.ConfigUtils;
import lombok.Getter;

@Getter
public enum ApplicationEnvironment {

    TEST("test"),

    PRODUCTION("prod"),

    DEVELOPMENT("dev"),

    HOMOLOG("hml");

    private final String profile;

    ApplicationEnvironment(String profile) {
        this.profile = profile;
    }

    public static ApplicationEnvironment current() {
        String currentProfile = ConfigUtils.getProfiles().getFirst();

        for (ApplicationEnvironment environment : ApplicationEnvironment.values()) {
            if (environment.getProfile().equals(currentProfile)) {
                return environment;
            }
        }

        return null;
    }

    public static boolean isProduction() {
        return ApplicationEnvironment.current() == ApplicationEnvironment.PRODUCTION;
    }

}
