package br.com.migration.controller;

import org.flywaydb.core.api.output.MigrateResult;

import br.com.migration.service.MigrationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/api/v1/migrations")
public class MigrationController {

    @Inject
    private MigrationService service;

    @POST
    public MigrateResult migrate() {
        return this.service.runPendingMigrations();
    }

}
