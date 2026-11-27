package io.quarkiverse.opensearch.it;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch.core.GetResponse;
import org.opensearch.client.opensearch.core.search.Hit;

/**
 * Exercises the sync and async OpenSearch Java clients end to end (JVM and native).
 */
@Path("/fruits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OpenSearchResource {

    private static final String INDEX = "fruits";

    @Inject
    OpenSearchClient client;

    @Inject
    OpenSearchAsyncClient asyncClient;

    @POST
    public Fruit index(Fruit fruit) throws IOException {
        client.index(i -> i.index(INDEX).id(fruit.id).document(fruit).refresh(Refresh.True));
        return fruit;
    }

    @GET
    @Path("/{id}")
    public Fruit get(@PathParam("id") String id) throws IOException {
        GetResponse<Fruit> response = client.get(g -> g.index(INDEX).id(id), Fruit.class);
        if (!response.found()) {
            throw new NotFoundException();
        }
        return response.source();
    }

    @GET
    @Path("/search")
    public List<Fruit> search(@QueryParam("color") String color)
            throws IOException, InterruptedException, ExecutionException {
        return asyncClient.search(s -> s.index(INDEX)
                .query(q -> q.match(m -> m.field("color").query(v -> v.stringValue(color)))), Fruit.class)
                .get()
                .hits().hits().stream()
                .map(Hit::source)
                .toList();
    }

    @GET
    @Path("/cluster-health")
    @Produces(MediaType.TEXT_PLAIN)
    public String clusterHealth() throws IOException {
        return client.cluster().health().status().jsonValue();
    }
}
