package com.maystrovoy.service;

import com.maystrovoy.dao.PersonDAO;
import com.maystrovoy.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class PersonService {

    @Autowired
    PersonDAO personDAO;

    @Inject
    private MessageSource messageSource;

    public String checkPersonAuthentication(HttpServletRequest request) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String authenticationError = null;
        Person person = personDAO.getPersonByLogin(request.getParameter("loginName"));
        if (person == null || !request.getParameter("password").equals(getHashedPassword(person.getPassword(),person.getCreationDay()))) {
            authenticationError = messageSource.getMessage("authorizationerr", null, null, null);
        } else {
            definePersonInSession(person, request.getSession());
        }
        return authenticationError;
    }

    public void definePersonInSession(Person person, HttpSession httpSession) {
        httpSession.setAttribute("person", person);
    }

    public void registerPerson(Person person) {
        personDAO.addPerson(person);
    }

    public String getHashedPassword(String passwordToHash, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        String generatedPassword = null;

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        messageDigest.update(salt.getBytes("UTF-8"));
        byte[] bytes = messageDigest.digest(passwordToHash.getBytes("UTF-8"));
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            stringBuilder.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        generatedPassword = stringBuilder.toString();
        return generatedPassword;
    }

}