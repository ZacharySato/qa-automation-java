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
        testCountryId = given(testCountryCreateRequest)
                .when()
                .post(countries)
                .then()
                .extract()
                .path("id");

        testCountryName = "10";
        testCountryCreateRequest = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBody("{\"countryName\": \"" + testCountryName + "\"}")
                .build();

        testCountryName = "20";
        testCountryUpdateRequest = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBody("{\"countryName\": \"" + testCountryName + "\",\"id\": " + testCountryId + "}")
                .build();
    }

    @AfterEach
    public void cleanUp() {
        delete(country, testCountryId);
    }

    @Test
    @DisplayName("Get country if exists")
    public void checkCountryGotSuccessfully() {
        when().get(country, testCountryId).then().statusCode(200).body("countryName", is(testCountryName));
    }

    @Test
    @DisplayName("Get country if not exists")
    public void checkCountryGotUnsuccessfully() {
        cleanUp();
        when().get(country, testCountryId).then().statusCode(404);
    }

    @Test
    @DisplayName("Update country if exists")
    public void checkCountryUpdatedSuccessfully() {
        given(testCountryUpdateRequest)
                .when()
                .put(country, testCountryId)
                .then()
                .statusCode(200)
                .body("countryName", is(testCountryName));
    }

    @Test
    @DisplayName("Update country if not exists")
    public void checkCountryUpdatedUnsuccessfully() {
        cleanUp();
        given(testCountryUpdateRequest)
                .when()
                .put(country, testCountryId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Create country if not exists")
    public void checkCountryPostedSuccessfully() {
        cleanUp();
        testCountryName = "30";
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
    @DisplayName("Create country if exists")
    public void checkCountryPostedUnsuccessfully() {
        given(testCountryCreateRequest)
                .when()
                .post(countries)
                .then()
                .statusCode(500);
    }

    @Test
    @DisplayName("Delete country if exists")
    public void checkCountryDeletedSuccessfully() {
        when().delete(country, testCountryId).then().statusCode(204);
        when().get(country, testCountryId).then().statusCode(404);
    }
}

