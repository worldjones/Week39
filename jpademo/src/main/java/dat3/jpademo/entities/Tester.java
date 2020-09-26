package dat3.jpademo.entities;

import dto.PersonStyleDTO;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;


public class Tester {
    
    public static void main(String[] args) {
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pu");
        EntityManager em = emf.createEntityManager();
        
        Person p1 = new Person("Jønke", 1963);
        Person p2 = new Person("Blondie", 1959);
        
        Address a1 = new Address("Store Torv 1", 2323, "Nr. Snede");
        Address a2 = new Address("Langgade 34", 1212, "Valby");
        
        p1.setAddress(a1);
        p2.setAddress(a2);
        
        Fee f1 = new Fee(100);
        Fee f2 = new Fee(200);
        Fee f3 = new Fee(300);
        
        p1.addFee(f1);
        p1.addFee(f3);
        p2.addFee(f2);
        
        SwimStyle s1 = new SwimStyle("Crawl");
        SwimStyle s2 = new SwimStyle("Butterfly");
        SwimStyle s3 = new SwimStyle("Breast stroke");
        
        p1.addSwimStyle(s1);
        p1.addSwimStyle(s3);
        p2.addSwimStyle(s2);
        
        em.getTransaction().begin();
            em.persist(p1);
            em.persist(p2);        
        em.getTransaction().commit();
        
        em.getTransaction().begin();
            p1.removeSwimStyle(s3);
        em.getTransaction().commit();
            
        System.out.println("p1: " + p1.getP_id() + ", " + p1.getName());
        System.out.println("p2: " + p2.getP_id() + ", " + p2.getName());
        
        System.out.println("Jønkes gade: " + p1.getAddress().getStreet());
        
        System.out.println("Lad os se om to-vejs virker: " + a1.getPerson().getName());
        
        System.out.println("Hvem har betalt f2? Det har: " + f2.getPerson().getName());
        
        System.out.println("Hvad er der blevet betalt i alt?");
        
        TypedQuery<Fee> q1 = em.createQuery("SELECT f FROM Fee f", Fee.class);
        List<Fee> fees = q1.getResultList();
        
        for (Fee f : fees) {
            System.out.println(f.getPerson().getName() + ": " + f.getAmount() + 
                    "kr. Den: " + f.getPayDate() + " Adr: " + f.getPerson().getAddress().getCity());
        }
        
        TypedQuery<Person> q2 = em.createQuery("SELECT p FROM Person p", Person.class);
        List<Person> persons = q2.getResultList();
        
        for (Person p : persons) {
            System.out.println("Navn: " + p.getName());
            System.out.println("--Fees:");
            for (Fee f : p.getFees()) {
                System.out.println("---- Beløb: " + f.getAmount() + ", " + f.getPayDate().toString());
            }
            System.out.println("--Styles");
            for (SwimStyle ss : p.styles) {
                System.out.println("---- Style: " + ss.getStyleName());
            }
        }
        
        //Ekstra ting
        System.out.println("***** JPQL joins *****");
        
        Query q3 = em.createQuery("SELECT new dto.PersonStyleDTO(p.name, p.year, s.styleName) FROM Person p JOIN p.styles s");
        
        List<PersonStyleDTO> personDetails = q3.getResultList();
        
        for (PersonStyleDTO ps : personDetails) {
            System.out.println("Navn: " + ps.getName() + ", " + ps.getYear() + ", " + ps.getSwimStyle());
        }
        
    }
    
}
