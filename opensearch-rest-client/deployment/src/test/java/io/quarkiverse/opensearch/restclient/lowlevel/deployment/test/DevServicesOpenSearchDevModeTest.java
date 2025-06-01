package io.quarkiverse.opensearch.restclient.lowlevel.deployment.test;

import static org.hamcrest.Matchers.equalTo;

import java.net.URL;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensearch.client.RestClient;
import org.wildfly.common.Assert;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.restassured.RestAssured;

public class DevServicesOpenSearchDevModeTest {

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class).addClass(TestResource.class));

    @TestHTTPEndpoint(TestResource.class)
    @TestHTTPResource
    URL url;

    @Inject
    RestClient client;

    @Test
    void testRestClient() throws Exception {
        Assert.assertFalse(client.getNodes().isEmpty());
    }

    @Test
    void testDatasource() throws Exception {
        var fruit = new TestResource.Fruit();
        fruit.id = "1";
        fruit.name = "banana";
        fruit.color = "yellow";

        RestAssured
                .given().body(fruit).contentType("application/json")
                .when().post(url)
                .then().statusCode(204);

        RestAssured.when().get(url.toString() + "/search?term=color&match=yellow")
                .then()
                .statusCode(200)
                .body(equalTo("[{\"id\":\"1\",\"name\":\"banana\",\"color\":\"yellow\"}]"));
    }
}
