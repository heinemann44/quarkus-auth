package br.com.resource.controller;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/api/v1")
public class ResourceController {

    @POST
    @Path("/resources")
    public String test() {
        return "Sucesso";
    }

    @POST
    @Path("/resources2")
    public String test2() {
        return "Sucesso2";
    }

}
