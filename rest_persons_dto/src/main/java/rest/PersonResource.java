package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.PersonDTO;
import dto.PersonsDTO;
import entities.Person;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import facades.PersonFacade;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import utils.EMF_Creator;


@Path("person")
public class PersonResource {

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();

    //An alternative way to get the EntityManagerFactory, whithout having to type the details all over the code
    //EMF = EMF_Creator.createEntityManagerFactory(DbSelector.DEV, Strategy.CREATE);
    private static final PersonFacade FACADE = PersonFacade.getPersonFacade(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String demo() {
        return "{\"msg\":\"Hello World\"}";
    }

    @Path("count")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getCount() {
        long count = FACADE.getPersonCount();
        return "{\"count\":" + count + "}";
    }

    @Path("all")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getAllPersons() {
        PersonsDTO psDTO = FACADE.getAllPersons();
        return GSON.toJson(psDTO);
    }

    @Path("{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getPerson(@PathParam("id") int id) throws PersonNotFoundException {
        PersonDTO pDTO = FACADE.getPerson(id);
        return GSON.toJson(pDTO);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String addPerson(String person) throws MissingInputException {
        PersonDTO pDTO = GSON.fromJson(person, PersonDTO.class);
        PersonDTO pAdded = FACADE.addPerson(pDTO.getfName(), pDTO.getlName(), pDTO.getPhone());
        return GSON.toJson(pAdded);
    }

    @Path("{id}")
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String editPerson(@PathParam("id") int id, String person) throws PersonNotFoundException, MissingInputException {
        PersonDTO pDTO = GSON.fromJson(person, PersonDTO.class);
        pDTO.setId(id);
        PersonDTO pEdited = FACADE.editPerson(pDTO);
        return GSON.toJson(pEdited);
    }

    @Path("{id}")
    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    public String deletePerson(@PathParam("id") int id) throws PersonNotFoundException {
        PersonDTO pDeleted = FACADE.deletePerson(id);
        return GSON.toJson(pDeleted);

    }
}
