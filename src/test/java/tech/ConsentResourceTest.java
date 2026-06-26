package tech;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ConsentResourceTest {

    private static final String BASE_PATH = "/open-banking/consents/";

    @Test
    void shouldCreateConsentSuccessfully() {
        var payload = """
            {
              "cpf": "12345678901",
              "permissions": ["ACCOUNTS_READ", "BALANCES_READ", "TRANSACTIONS_READ"],
              "transactionFrom": "%s",
              "transactionTo": "%s"
            }
            """.formatted(
                OffsetDateTime.now().minusMonths(6),
                OffsetDateTime.now()
        );

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .body("consentId", startsWith("urn:ofb:consent:"))
                .body("status", is("AWAITING_AUTHORISATION"))
                .body("permissions", hasItems("ACCOUNTS_READ", "BALANCES_READ"));
    }

    @Test
    void shouldReturn404WhenConsentDoesNotExist() {
        given()
                .pathParam("id", "urn:ofb:consent:nao-existe")
                .when()
                .get(BASE_PATH + "/{id}")
                .then()
                .statusCode(404)
                .body("code", is("CONSENT_NOT_FOUND"));
    }

    @Test
    void shouldTransitionStatusCorrectly() {
        var consentId = given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "cpf": "98765432100",
              "permissions": ["ACCOUNTS_READ"]
            }
            """)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().jsonPath().getString("consentId");

        given()
                .post(BASE_PATH + "/{id}/authorise", consentId)
                .then()
                .statusCode(200)
                .body("status", is("AUTHORISED"));

        given()
                .contentType(ContentType.JSON)
                .body("{\"reason\": \"usuário solicitou\"}")
                .delete(BASE_PATH + "/{id}", consentId)
                .then()
                .statusCode(200)
                .body("status", is("REVOKED"));
    }

    @Test
    void shouldNotAuthorizeAlreadyAuthorizedConsent() {
        var consentId = given()
                .contentType(ContentType.JSON)
                .body("{\"cpf\": \"11111111111\", \"permissions\": [\"ACCOUNTS_READ\"]}")                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().jsonPath().getString("consentId");

        given()
                .post(BASE_PATH + "/{id}/authorise", consentId)
                .then()
                .statusCode(200);

        // Attempting second authorization
        given()
                .post(BASE_PATH + "/{id}/authorise", consentId)
                .then()
                .statusCode(422)
                .body("code", is("INVALID_STATE_TRANSITION"));
    }
}