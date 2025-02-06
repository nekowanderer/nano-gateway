package idv.clu.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class RestResourceTest {

    @Test
    void testEchoEndpointWithValidJson() {
        String requestPayload = "{\"game\":\"Mobile Legends\",\"gamerID\":\"GYUTDTE\",\"points\":20}";
        
        given()
                .contentType("application/json")
                .body(requestPayload)
                .when()
                .post("/rest_resource/echo")
                .then()
                .statusCode(200)
                .body("game", is("Mobile Legends"))
                .body("gamerID", is("GYUTDTE"))
                .body("points", is(20));
    }

    @Test
    void testEchoEndpointWithInvalidJson() {
        String requestPayload = "{\"game\":\"Mobile Legends\",\"gamerID\":\"GYUTDTE\",\"points\":20\"}";

        given()
                .contentType("application/json")
                .body(requestPayload)
                .when()
                .post("/rest_resource/echo")
                .then()
                .statusCode(400)
                .body("error", is("Invalid JSON"));
    }

    @Test
    void testDelayEndpointWithValidJson() {
        String requestPayload = "{\"delay\": 1000}";

        given()
                .contentType("application/json")
                .body(requestPayload)
                .when()
                .post("/rest_resource/delay")
                .then()
                .statusCode(200)
                .body("delayUpdateInformation", is("Delay updated to 1000 ms"));
    }

    @Test
    void testDelayEndpointWithValidJsonButNegativeDelay() {
        String requestPayload = "{\"delay\": -1000}";

        given()
                .contentType("application/json")
                .body(requestPayload)
                .when()
                .post("/rest_resource/delay")
                .then()
                .statusCode(200)
                .body("delayUpdateInformation", is("Delay updated to 0 ms"));
    }

    @Test
    void testDelayEndpointWithInvalidJson() {
        String requestPayload = "{\"delay\": 1000\"}";

        given()
                .contentType("application/json")
                .body(requestPayload)
                .when()
                .post("/rest_resource/delay")
                .then()
                .statusCode(400)
                .body("error", is("Invalid JSON"));
    }

}