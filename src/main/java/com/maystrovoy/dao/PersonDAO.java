package com.maystrovoy.dao;

import com.maystrovoy.model.Person;
import com.maystrovoy.service.PersonService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Repository
@Transactional
public class PersonDAO {

    private static final Logger LOGGER = LogManager.getLogger(PersonDAO.class);

    @PersistenceContext
    private EntityManager entityManager;

    public void addPerson(Person person) {
        if (person != null) {
            person.setRole("read");
            entityManager.persist(person);
        }
    }

    public void mergePerson(Person person) {
        entityManager.merge(person);
    }

    public void removePersonByLoginName(String adminLogin, String personLoginName, HttpServletRequest request) {
        Person person = getPersonByLogin(personLoginName);
        if (person != null) {
            entityManager.remove(person);
            if (adminLogin.equals(personLoginName)) {
                request.getSession().invalidate();
            }
            LOGGER.info("admin : " + adminLogin + " removed person : " + personLoginName);
        } else {
            LOGGER.error("failed to remove person, person is null");
        }
    }

    public void changePersonPassword(String loginName, String newPassword) throws UnsupportedEncodingException,
            NoSuchAlgorithmException {
        Person person = getPersonByLogin(loginName);
        String password = PersonService.getHashedPassword(newPassword, person.getCreationDay());
        person.setPassword(password);
        entityManager.persist(person);
        LOGGER.info("person : " + person.getLoginName() + " successfully changed password");
    }

    public void resetPersonPassword(String login, String resetPersonLoginName, HttpServletRequest request) {
        Person person = getPersonByLogin(resetPersonLoginName);
        if (person != null) {
            String password = null;
            try {
                password = PersonService.getHashedPassword(person.getLoginName(), person.getCreationDay());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            person.setPassword(password);
            entityManager.persist(person);
            LOGGER.info("admin : " + login + " reset password for person : " + resetPersonLoginName);
            if (login.equals(resetPersonLoginName)) {
                request.getSession().invalidate();
            }
        } else {
            LOGGER.info("failed to reset person password, person is null");
        }

    }

    public void updatePersonRole(String login, String editedPersonLoginName, String editedPersonRole) {
        Person editedPerson = getPersonByLogin(editedPersonLoginName);
        editedPerson.setRole(editedPersonRole);
        LOGGER.info("Admin: " + login + " changed user " + editedPersonLoginName + " priviliges to " +
                editedPersonRole);
        entityManager.persist(editedPerson);
    }

    public List<Person> getAllPersons() {
        Query getAllPersonsQuery = entityManager.createQuery("select q from Person q");
        List<Person> allPersonsList = getAllPersonsQuery.getResultList();
        return allPersonsList;
    }

    public Person getPersonByLogin(String loginName) {
        Person labor = null;
        Query getPersonQuery = entityManager.createQuery("select p from Person p where p.loginName = :loginParameter");
        getPersonQuery.setParameter("loginParameter", loginName);
        List<Person> laborList = getPersonQuery.getResultList();
        if (laborList.size() > 0) {
            labor = laborList.get(0);
        }
        return labor;
    }

    public boolean isLoginExist(String loginName) {
        boolean isLoginExist = false;
        Query getPersonQuery = entityManager.createQuery("select p from Person p where p.loginName = :loginParameter");
        getPersonQuery.setParameter("loginParameter", loginName);
        List<Person> laborList = getPersonQuery.getResultList();
        if (laborList.size() > 0) {
            isLoginExist = true;
        }
        return isLoginExist;
    }

}
