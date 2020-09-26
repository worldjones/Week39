package facades;

import dto.PersonDTO;
import dto.PersonsDTO;
import entities.Person;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;


public class PersonFacade implements IPersonFacade {

    private static PersonFacade instance;
    private static EntityManagerFactory emf;

    //Private Constructor to ensure Singleton
    private PersonFacade() {
    }

    /**
     *
     * @param _emf
     * @return an instance of this facade class.
     */
    public static PersonFacade getPersonFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PersonFacade();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public long getPersonCount() {
        EntityManager em = emf.createEntityManager();
        try {
            long personCount = (long) em.createQuery("SELECT COUNT(p) FROM Person p").getSingleResult();
            return personCount;
        } finally {
            em.close();
        }
    }

    @Override
    public PersonDTO addPerson(String fName, String lName, String phone) throws MissingInputException {
        if (fName.length() == 0 || lName.length() == 0) {
            throw new MissingInputException("First name and/or last name is missing");
        }
        EntityManager em = emf.createEntityManager();
        Person person = new Person(fName, lName, phone);
        try {
            em.getTransaction().begin();
            em.persist(person);
            em.getTransaction().commit();

            return new PersonDTO(person);
        } finally {
            em.close();
        }
    }

    @Override
    public PersonDTO deletePerson(int id) throws PersonNotFoundException {
        EntityManager em = emf.createEntityManager();
        try {
            Person person = em.find(Person.class, id);
            if (person == null) {
                throw new PersonNotFoundException("Could not delete, provided id does not exist");
            } else {
                em.getTransaction().begin();
                em.remove(person);
                em.getTransaction().commit();

                return new PersonDTO(person);
            }

        } finally {
            em.close();
        }
    }

    @Override
    public PersonDTO getPerson(int id) throws PersonNotFoundException {
        EntityManager em = emf.createEntityManager();
        try {
            Person person = em.find(Person.class, id);
            if (person == null) {
                throw new PersonNotFoundException("No person with provided id found");
            } else {
                return new PersonDTO(person);
            }
        } finally {
            em.close();
        }
    }

    @Override
    public PersonsDTO getAllPersons() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Person> query
                    = em.createQuery("SELECT p FROM Person p", Person.class);
            List<Person> personEntities = query.getResultList();
            PersonsDTO all = new PersonsDTO(personEntities);
            return all;
        } finally {
            em.close();
        }
    }

    @Override
    public PersonDTO editPerson(PersonDTO p) throws PersonNotFoundException, MissingInputException {
        if (p.getfName().length() == 0 || p.getlName().length() == 0) {
            throw new MissingInputException("First name and/or last name is missing");
        }
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Person person = em.find(Person.class, p.getId());
            if (person == null) {
                throw new PersonNotFoundException("No person with provided id found");
            } else {
                person.setFirstName(p.getfName());
                person.setLastName(p.getlName());
                person.setPhone(p.getPhone());

                em.getTransaction().commit();

                return new PersonDTO(person);
            }
        } finally {
            em.close();
        }
    }

}
