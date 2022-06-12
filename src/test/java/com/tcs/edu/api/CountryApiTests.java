package com.tcs.edu.api;

import io.restassured.RestAssured;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class CountryApiTests {
    private static final String country = "/api/countries/{id}";
    private static final String countries = "/api/countries/";
    private String testCountryName = "10";
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
        testCountryId = given()
                .contentType(ContentType.JSON)
                .body("{\"countryName\": \"" + testCountryName + "\"}")
                .when()
                .post(countries)
                .then()
                .extract()
                .path("id");
        System.out.println(testCountryId);
    }

    @AfterEach
    public void cleanUp() {
        delete(country, testCountryId);
    }

    @Test
    @DisplayName("Create country")
    public void checkCountryPostedSuccessfully() {
        cleanUp();
        testCountryName = "20";
        testCountryId = given()
                .contentType(ContentType.JSON)
                .body("{\"countryName\": \"" + testCountryName + "\"}")
                .when()
                .post(countries)
                .then()
                .statusCode(201)
                .body("id", not(empty()))
                .extract()
                .path("id");
    }


    @Test
    @DisplayName("Get country")
    public void checkCountryGotSuccessfully() {
        when().get(country, testCountryId)
                .then()
                .statusCode(200)
                .body("countryName", is(testCountryName));
    }


    @Test
    @DisplayName("Update country")
    public void checkCountryUpdatedSuccessfully() {
        testCountryName = "30";
        given()
                .contentType(ContentType.JSON)
                .body("{\"countryName\": \"" + testCountryName + "\",\"id\": " + testCountryId + "}")
                .when()
                .patch(country, testCountryId)
                .then()
                .statusCode(200)
                .body("countryName", is(testCountryName));
    }

    @Test
    @DisplayName("Delete country")
    public void checkCountryDeletedSuccessfully() {
        when().delete(country, testCountryId).then().statusCode(204);
    }
}
