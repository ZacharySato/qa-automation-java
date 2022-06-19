package com.tcs.edu.api;

import io.restassured.RestAssured;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

public class CountryApiTests {
    private static final String COUNTRY_PATH = "/api/countries/{id}";
    private static final String COUNTRIES_PATH = "/api/countries/";
    private static Connection connection;
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

    @BeforeAll
    public static void connect() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost/app-db",
                "app-db-admin",
                "P@ssw0rd"
        );
    }

    @AfterAll
    public static void disconnect() throws SQLException {
        connection.close();
    }

    @BeforeEach
    public void setUp() throws SQLException {
        testCountryName = randomCountryName();
        testCountryNameUpdated = randomCountryName();
        testCountryId = 101;

        testCountryUpdateRequest = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBody("{\"countryName\": \"" + testCountryNameUpdated + "\",\"id\": " + testCountryId + "}")
                .build();

        PreparedStatement newCountry = connection.prepareStatement(
                "INSERT INTO country(id,country_name) VALUES(?,?)"
        );
        newCountry.setInt(1, testCountryId);
        newCountry.setString(2, testCountryName);
        newCountry.executeUpdate();
    }

    @AfterEach
    public void cleanUp() throws SQLException {
        PreparedStatement sql = connection.prepareStatement(
                "DELETE FROM public.country WHERE id = ?"
        );
        sql.setInt(1, testCountryId);
        sql.executeUpdate();
    }

    @Test
    @DisplayName("Get existed country")
    public void checkCountryGotSuccessfully() {
        when()
                .get(COUNTRY_PATH, testCountryId)
                .then()
                .statusCode(200)
                .body("countryName", is(testCountryName));
    }

    @Test
    @DisplayName("Get nonexistent country")
    public void checkCountryGotUnsuccessfully() throws SQLException {
        cleanUp();
        when()
                .get(COUNTRY_PATH, testCountryId)
                .then()
                .statusCode(404);
    }


    @Test
    @DisplayName("Update existed country")
    public void checkCountryUpdatedSuccessfully() {
        given(testCountryUpdateRequest)
                .when()
                .put(COUNTRY_PATH, testCountryId)
                .then()
                .statusCode(200)
                .body("countryName", is(testCountryNameUpdated));
    }

    @Test
    @DisplayName("Update nonexistent country")
    public void checkCountryUpdatedUnsuccessfully() throws SQLException {
        cleanUp();
        given(testCountryUpdateRequest)
                .when()
                .put(COUNTRY_PATH, testCountryId)
                .then()
                .statusCode(400);
    }


    @Test
    @DisplayName("Create nonexistent country")
    public void checkCountryPostedSuccessfully() throws SQLException {
        cleanUp();
        testCountryId = given()
                .contentType(ContentType.JSON)
                .body("{\"countryName\": \"" + testCountryName + "\"}")
                .when()
                .post(COUNTRIES_PATH)
                .then()
                .statusCode(201)
                .body("id", not(empty()))
                .body("countryName", is(testCountryName))
                .extract()
                .path("id");
    }

    @Test
    @DisplayName("Create existed country (w\\ error)")
    public void checkCountryPostedUnsuccessfully() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"countryName\": \"" + testCountryName + "\"}")
                .when()
                .post(COUNTRIES_PATH)
                .then()
                .statusCode(500);
    }

    @Test
    @DisplayName("Delete existed country")
    public void checkCountryDeletedSuccessfully() {
        when()
                .delete(COUNTRY_PATH, testCountryId)
                .then()
                .statusCode(204);
        when()
                .get(COUNTRY_PATH, testCountryId)
                .then()
                .statusCode(404);
    }

    private char randomLetter() {
        return (char) (new Random().nextInt(26) + 'A');
    }

    private String randomCountryName() {
        return String.format("%s%s", randomLetter(), randomLetter());
    }
}

