package io.quarkiverse.opensearch;

import java.io.IOException;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;

import io.smallrye.mutiny.Uni;

@Path("/")
public class IndicesResource {

    @Inject
    OpenSearchClient client;

    @Inject
    OpenSearchAsyncClient asyncClient;

    @GET
    @Path("/indices")
    @Produces(MediaType.TEXT_PLAIN)
    public String indices() throws IOException {
        return client.cat().indices().valueBody().stream().map(e -> e.index()).collect(Collectors.joining("\n"));
    }

    @GET
    @Path("/indices-async")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> indicesAsync() throws IOException {
        return Uni.createFrom().completionStage(asyncClient.cat().indices())
                .chain(r -> Uni.createFrom()
                        .item(r.valueBody().stream().map(t -> t.index()).collect(Collectors.joining("\n"))));
    }
}
