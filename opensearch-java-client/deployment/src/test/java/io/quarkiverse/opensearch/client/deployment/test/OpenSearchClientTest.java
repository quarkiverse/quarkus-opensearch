package io.quarkiverse.opensearch.client.deployment.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.GetRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;

import io.quarkus.test.QuarkusUnitTest;

class OpenSearchClientTest {

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset(
                            // Dummy config triggers analytics client but lets Dev Services start it
                            "quarkus.opensearch.username=dummy"), "application.properties"));

    @Inject
    OpenSearchClient client;

    @Inject
    OpenSearchAsyncClient asyncClient;

    private final String index = "sample-index";
    private final String id = "1";

    private final int wait = 10;

    private final TimeUnit seconds = TimeUnit.SECONDS;

    @Test
    void testOpenSearchClient() throws Exception {
        assertNotNull(client._transport());
        CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(index).build();
        assertEquals(client.indices().create(createIndexRequest).index(), index);

        IndexData indexData = new IndexData("John", "Joe");
        IndexRequest<IndexData> indexRequest = new IndexRequest.Builder<IndexData>().index(index).id(id).document(indexData)
                .build();
        assertEquals(client.index(indexRequest).id(), id);

        GetRequest getRequest = new GetRequest.Builder().index(index).id(id).build();
        assertTrue(client.get(getRequest, IndexData.class).found());

        // force refresh for search
        client.indices().refresh();

        SearchRequest searchRequest = new SearchRequest.Builder().query(q -> q.match(m -> m.field("firstName")
                .query(FieldValue.of("John"))))
                .build();
        SearchResponse<IndexData> searchResponse = client.search(searchRequest, IndexData.class);
        assertTrue(searchResponse.hits().hits().size() > 0);

        // delete index
        assertTrue(client.indices().delete(new DeleteIndexRequest.Builder().index(index).build()).acknowledged());
    }

    @Test
    void testOpenSearchAsyncClient() throws Exception {
        assertNotNull(asyncClient._transport());
        CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(index).build();
        assertEquals(asyncClient.indices().create(createIndexRequest).get(wait, seconds).index(), index);

        IndexData indexData = new IndexData("John", "Joe");
        IndexRequest<IndexData> indexRequest = new IndexRequest.Builder<IndexData>().index(index).id(id).document(indexData)
                .build();
        assertEquals(asyncClient.index(indexRequest).get(wait, seconds).id(), id);

        GetRequest getRequest = new GetRequest.Builder().index(index).id(id).build();
        assertTrue(asyncClient.get(getRequest, IndexData.class).get(wait, seconds).found());

        // force refresh for search
        asyncClient.indices().refresh().get(wait, seconds);

        SearchRequest searchRequest = new SearchRequest.Builder().query(q -> q.match(m -> m.field("firstName")
                .query(FieldValue.of("John"))))
                .build();
        SearchResponse<IndexData> searchResponse = asyncClient.search(searchRequest, IndexData.class).get(wait, seconds);
        assertTrue(searchResponse.hits().hits().size() > 0);

        // delete index
        assertTrue(asyncClient.indices().delete(new DeleteIndexRequest.Builder().index(index).build()).get(wait, seconds)
                .acknowledged());

    }

    public static class IndexData {
        private String firstName;
        private String lastName;

        public IndexData() {
            // empty constructor for jackson-databind
        }

        public IndexData(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

    }
}
