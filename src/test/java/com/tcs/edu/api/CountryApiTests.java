package com.tcs.edu.api;

import io.restassured.RestAssured;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class CountryApiTests {
    private static final String country = "/api/countries/{id}";
    private static final String countries = "/api/countries/";
    private RequestSpecification testCountryCreateRequest;
    private RequestSpecification testCountryUpdateRequest;
    private String testCountryName;
    private String testCountryNameUpdated;
    private Integer testCountryId;

    @BeforeAll
    public static void setUpAuth() {
        PreemptiveBasicAuthScheme authScheme = new PreemptiveBasicAuthScheme();
        authScheme.setUserName("admin");
        authScheme.setPassword("admin");
        RestAssured.authentication = authScheme;
    }

    @BeforeAll
    public static void setUpErrorLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    public void setUp() {
        testCountryName = "10";
        testCountryCreateRequest = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBody("{\"countryName\": \"" + testCountryName + "\"}")
                .build();

        testCountryId = given(testCountryCreateRequest)
                .when()
                .post(countries)
                .then()
                .extract()
                .path("id");

        testCountryNameUpdated = "20";
        testCountryUpdateRequest = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBody("{\"countryName\": \"" + testCountryNameUpdated + "\",\"id\": " + testCountryId + "}")
                .build();
    }

    @AfterEach
    public void cleanUp() {
        delete(country, testCountryId);
    }

    @Test
    @DisplayName("Get existed country")
    public void checkCountryGotSuccessfully() {
        when().get(country, testCountryId).then().statusCode(200).body("countryName", is(testCountryName));
    }

    @Test
    @DisplayName("Get country not found")
    public void checkCountryGotUnsuccessfully() {
        cleanUp();
        when().get(country, testCountryId).then().statusCode(404);
    }

    @Test
    @DisplayName("Update existed country")
    public void checkCountryUpdatedSuccessfully() {
        given(testCountryUpdateRequest)
                .when()
                .put(country, testCountryId)
                .then()
                .statusCode(200)
                .body("countryName", is(testCountryNameUpdated));
    }

    @Test
    @DisplayName("Update country not found")
    public void checkCountryUpdatedUnsuccessfully() {
        cleanUp();
        given(testCountryUpdateRequest)
                .when()
                .put(country, testCountryId)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Create country not found")
    public void checkCountryPostedSuccessfully() {
        cleanUp();
        testCountryId = given()
                .contentType(ContentType.JSON)
                .body("{\"countryName\": \"" + testCountryName + "\"}")
                .when()
                .post(countries)
                .then()
                .statusCode(201)
                .body("id", not(empty()))
                .body("countryName", is(testCountryName))
                .extract()
                .path("id");
    }

    @Test
    @DisplayName("Create existed country")
    public void checkCountryPostedUnsuccessfully() {
        given(testCountryCreateRequest)
                .when()
                .post(countries)
                .then()
                .statusCode(500);
    }

    @Test
    @DisplayName("Delete existed country")
    public void checkCountryDeletedSuccessfully() {
        when().delete(country, testCountryId).then().statusCode(204);
        when().get(country, testCountryId).then().statusCode(404);
    }
}

