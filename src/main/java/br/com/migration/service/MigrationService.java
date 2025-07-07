package br.com.migration.service;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MigrationService {

    @Inject
    private Flyway flyway;

    public MigrateResult runPendingMigrations() {
        return this.flyway.migrate();
    }

}
