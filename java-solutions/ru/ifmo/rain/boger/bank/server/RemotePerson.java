package ru.ifmo.rain.boger.bank.server;


import ru.ifmo.rain.boger.bank.common.Person;

import java.rmi.Remote;

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
