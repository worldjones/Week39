package facades;

import dto.PersonDTO;
import entities.Person;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import utils.EMF_Creator;


public class PersonFacadeTest {

    private static EntityManagerFactory emf;
    private static PersonFacade facade;
    Person p1;
    Person p2;
    Person p3;

    public PersonFacadeTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        facade = PersonFacade.getPersonFacade(emf);
    }

    @AfterAll
    public static void tearDownClass() {
//        Clean up database after test is done or use a persistence unit with drop-and-create to start up clean on every test
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // Setup the DataBase in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the script below to use YOUR OWN entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        try {
            p1 = new Person("Bob", "Hansen", "13374200");
            p2 = new Person("Jafar", "Habibti", "69696969");
            p3 = new Person("Allan", "Winther", "11111111");
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.persist(p1);
            em.persist(p2);

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @AfterEach
    public void tearDown() {
//        Remove any data after each test was run
    }

    @Test
    public void testGetCount() {
        assertEquals(2, facade.getPersonCount());
    }

    @Test
    public void testGetAllPersons() {
        assertEquals(2, facade.getAllPersons().size(), "Expect two persons in database");
    }

    @Test
    public void testGetPerson() throws PersonNotFoundException {
        PersonDTO p1DTO = facade.getPerson(p1.getId());
        assertEquals(p1DTO.getfName(), p1.getFirstName(), "Expect the same firstname");
    }

    @Test()
    public void testGetPersonException() {
        try {
            PersonDTO p1DTO = facade.getPerson(999); //Expect the exception here
        } catch (PersonNotFoundException ex) {
            assertEquals("No person with provided id found", ex.getMessage());
        }

    }

    @Test
    public void testAddPerson() throws MissingInputException {
        PersonDTO p3DTO = facade.addPerson(p3.getFirstName(), p3.getLastName(), p3.getPhone());
        assertEquals(3, facade.getAllPersons().size(), "Expect three persons in database now");
        assertEquals(p3DTO.getPhone(), p3.getPhone(), "Expect the same phone");
    }

    @Test
    public void testAddPersonException() {
        try {
            PersonDTO p3DTO = facade.addPerson("", "", p3.getPhone());
        } catch (MissingInputException ex) {
            assertEquals("First name and/or last name is missing", ex.getMessage());
        }
    }

    @Test
    public void testEditPerson() throws PersonNotFoundException, MissingInputException {
        PersonDTO newData = new PersonDTO("Jonas", "Jørgensen", "35363738"); //json String
        newData.setId(p2.getId()); //id from url
        PersonDTO p2New = facade.editPerson(newData);
        assertEquals(newData.getlName(), p2New.getlName(), "Expect the same lastname");
        assertEquals(newData.getfName(), p2New.getfName(), "Expect the same firstname");
        assertEquals(newData.getPhone(), p2New.getPhone(), "Expect the same phone");
    }

    @Test
    public void testEditPersonExceptionNotFound() throws MissingInputException {
        try {
            PersonDTO newData = new PersonDTO("Jonas", "Jørgensen", "35363738"); //json String
            newData.setId(999); //id from url
            PersonDTO p2New = facade.editPerson(newData); //Expect the exceptions here
        } catch (PersonNotFoundException ex) {
            assertEquals("No person with provided id found", ex.getMessage());
        }
    }

    @Test
    public void testEditPersonExceptionMissingInput() throws PersonNotFoundException {
        try {
            PersonDTO newData = new PersonDTO("Jonas", "Jørgensen", "35363738"); //json String
            newData.setId(p2.getId()); //id from url
            PersonDTO p2New = facade.editPerson(newData); //Expect the exceptions here
        } catch (MissingInputException ex) {
            assertEquals("First name and/or last name is missing", ex.getMessage());
        }
    }

    @Test
    public void testDeletePerson() throws PersonNotFoundException {
        PersonDTO p1DTO = facade.deletePerson(p1.getId());
        assertEquals(1, facade.getAllPersons().size(), "Expect one person left in database");
        assertEquals(p1DTO.getId(), p1.getId());
    }

    @Test
    public void testDeletePersonException() {
        try {
            PersonDTO p1DTO = facade.deletePerson(999); //Expect the exception here
        } catch (PersonNotFoundException ex) {
            assertEquals("Could not delete, provided id does not exist", ex.getMessage());
        }

    }

}
