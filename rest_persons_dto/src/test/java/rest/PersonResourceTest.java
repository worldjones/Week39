package rest;

import dto.PersonDTO;
import entities.Person;
import exceptions.PersonNotFoundException;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.parsing.Parser;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.EMF_Creator;

/**
 *
 * @author Acer
 */
public class PersonResourceTest {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";
    private static Person p1;
    private static Person p2;
    private static Person p3;

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        //This method must be called before you request the EntityManagerFactory
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactoryForTest();

        httpServer = startServer();
        httpServer.start();
        while (!httpServer.isStarted()) {
        }
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void closeTestServer() {
        //System.in.read();
        //Don't forget this, if you called its counterpart in @BeforeAll
        EMF_Creator.endREST_TestWithDB();
        httpServer.shutdownNow();
    }

    // Setup the DataBase (used by the test-server and this test) in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the EntityClass used below to use YOUR OWN (renamed) Entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        p1 = new Person("Per", "Larsen", "46765647");
        p2 = new Person("Kurt", "Hansen", "76847462");
        p3 = new Person("Gurli", "Svendsen", "76856412");
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Test
    public void testServerIsUp() {
        System.out.println("Testing is server UP");
        given().when().get("/person").then().statusCode(200);
    }

    //This test assumes the database contains two rows
    @Test
    public void testDummyMsg() throws Exception {
        given()
                .contentType("application/json")
                .get("/person/").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("msg", equalTo("Hello World"));
    }

    @Test
    public void testCount() throws Exception {
        given()
                .contentType("application/json")
                .get("/person/count").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("count", equalTo(3));
    }

    @Test
    public void testGetAllPersons() throws Exception {
        List<PersonDTO> personsDTO;
        personsDTO = given()
                .contentType("application/json")
                .when()
                .get("/person/all").then()
                .extract().body().jsonPath().getList("all", PersonDTO.class);

        assertThat(personsDTO, iterableWithSize(3));

    }

    @Test
    public void testGetPerson() throws Exception {
        given()
                .contentType("application/json")
                .get("/person/" + p2.getId()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("fName", equalTo(p2.getFirstName()));
    }
    
    @Test
    public void testGetPersonException() {
        given()
                .contentType("application/json")
                .get("/person/" + 999).then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND_404.getStatusCode())
                .body("message", equalTo("No person with provided id found"));
    }

    @Test
    public void testAddPerson() throws Exception {
        given()
                .contentType("application/json")
                .body(new PersonDTO("Klaus", "Guttermand", "75634251"))
                .when()
                .post("person")
                .then()
                .body("fName", equalTo("Klaus"))
                .body("lName", equalTo("Guttermand"))
                .body("phone", equalTo("75634251"))
                .body("id", notNullValue());
    }
    
    @Test
    public void testAddPersonException() {
        given()
                .contentType("application/json")
                .body(new PersonDTO("Klaus", "", "75634251"))
                .when()
                .post("person")
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST_400.getStatusCode())
                .body("message", equalTo("First name and/or last name is missing"));
    }

    @Test
    public void testEditPerson() throws Exception {
        PersonDTO p3DTO = new PersonDTO(p3);
        p3DTO.setfName("Bente");

        given()
                .contentType("application/json")
                .body(p3DTO)
                .when()
                .put("person/" + p3DTO.getId())
                .then()
                .body("fName", equalTo("Bente"))
                .body("lName", equalTo("Svendsen"))
                .body("phone", equalTo("76856412"))
                .body("id", equalTo(p3DTO.getId()));
    }
    
    @Test
    public void testEditPersonExceptionNotFound() {
        PersonDTO p3DTO = new PersonDTO(p3);
        p3DTO.setfName("Bente");

        given()
                .contentType("application/json")
                .body(p3DTO)
                .when()
                .put("person/" + 999)
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND_404.getStatusCode())
                .body("message", equalTo("No person with provided id found"));
    }
    
    @Test
    public void testEditPersonExceptionMissingInput() {
        PersonDTO p3DTO = new PersonDTO(p3);
        p3DTO.setfName("");

        given()
                .contentType("application/json")
                .body(p3DTO)
                .when()
                .put("person/" + p3DTO.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST_400.getStatusCode())
                .body("message", equalTo("First name and/or last name is missing"));
    }

    @Test
    public void testDeletePerson() throws Exception {
        PersonDTO p1DTO = new PersonDTO(p1);

        given()
                .contentType("application/json")
                .delete("person/" + p1DTO.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode());

        List<PersonDTO> personsDTO;
        personsDTO = given()
                .contentType("application/json")
                .when()
                .get("/person/all").then()
                .extract().body().jsonPath().getList("all", PersonDTO.class
                );

        assertThat(personsDTO, iterableWithSize(2));
    }
    
    @Test
    public void testDeletePersonException() {
        PersonDTO p1DTO = new PersonDTO(p1);

        given()
                .contentType("application/json")
                .delete("person/" + 999)
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND_404.getStatusCode())
                .body("message", equalTo("Could not delete, provided id does not exist"));
    }

}
