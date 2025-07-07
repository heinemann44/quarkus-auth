package br.com.resource.controller;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/api/v1/resources")
public class ResourceController {

    @POST
    public String test() {
        return "Sucesso";
    }

}
