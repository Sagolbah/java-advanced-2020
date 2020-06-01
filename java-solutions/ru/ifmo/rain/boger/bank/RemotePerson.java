package ru.ifmo.rain.boger.bank;


import java.rmi.Remote;
import java.util.Map;

public class RemotePerson implements Remote, Person {
    private final String firstName;
    private final String lastName;
    private final String passport;

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassport() {
        return passport;
    }

    public RemotePerson(String firstName, String lastName, String passport) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passport = passport;
    }

}
