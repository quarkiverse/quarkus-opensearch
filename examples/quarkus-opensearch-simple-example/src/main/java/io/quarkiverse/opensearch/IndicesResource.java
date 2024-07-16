package io.quarkiverse.opensearch;

import java.io.IOException;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.opensearch.client.opensearch.OpenSearchClient;

@Path("/indices")
public class IndicesResource {

    @Inject
    OpenSearchClient client;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws IOException {
        return client.cat().indices().valueBody().stream().map(e -> e.index()).collect(Collectors.joining("\n"));
    }
}
