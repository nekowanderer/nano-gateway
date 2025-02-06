package idv.clu.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@QuarkusTestResource(TestPropertyResource.class)
class RoutingResourceTest {

    @Test
    void testRouteSimpleApiEndpoint() {
        String requestPayload = "{\"game\":\"Mobile Legends\",\"gamerID\":\"GYUTDTE\",\"points\":20}";

        given()
                .contentType("application/json")
                .body(requestPayload)
                .when()
                .post("/route/simple_api")
                .then()
                .statusCode(200)
                .body("route_to", is("http://simple_api_1:8080"));

        given()
                .contentType("application/json")
                .body(requestPayload)
                .when()
                .post("/route/simple_api")
                .then()
                .statusCode(200)
                .body("route_to", is("http://simple_api_2:8080"));

        given()
                .contentType("application/json")
                .body(requestPayload)
                .when()
                .post("/route/simple_api")
                .then()
                .statusCode(200)
                .body("route_to", is("http://simple_api_3:8080"));
    }

}