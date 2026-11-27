package io.quarkiverse.opensearch.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class OpenSearchResourceTest {

    @Test
    void indexGetAndSearch() {
        given().contentType(ContentType.JSON)
                .body("{\"id\":\"1\",\"name\":\"apple\",\"color\":\"red\"}")
                .post("/fruits")
                .then().statusCode(200);

        given().get("/fruits/1")
                .then().statusCode(200)
                .body("name", equalTo("apple"))
                .body("color", equalTo("red"));

        given().queryParam("color", "red")
                .get("/fruits/search")
                .then().statusCode(200)
                .body("name", hasItem("apple"));

        given().get("/fruits/does-not-exist")
                .then().statusCode(404);
    }

    @Test
    void clusterHealth() {
        given().get("/fruits/cluster-health")
                .then().statusCode(200)
                .body(anyOf(equalTo("green"), equalTo("yellow")));
    }

    @Test
    void healthCheck() {
        given().get("/q/health/ready")
                .then().statusCode(200)
                .body("status", equalTo("UP"));
    }
}
