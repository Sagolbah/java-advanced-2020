package ru.ifmo.rain.boger.bank.server;

import ru.ifmo.rain.boger.bank.common.Person;

public abstract class AbstractPerson implements Person {
    private final String firstName;
    private final String lastName;
    private final String passport;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassport() {
        return passport;
    }

    public AbstractPerson(String firstName, String lastName, String passport) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passport = passport;
    }
}
