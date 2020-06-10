package ru.ifmo.rain.boger.bank.server;


import ru.ifmo.rain.boger.bank.common.Person;

import java.rmi.Remote;

public class RemotePerson extends AbstractPerson implements Remote {

    @Override
    public String getFirstName() {
        return super.getFirstName();
    }

    @Override
    public String getLastName() {
        return super.getLastName();
    }

    @Override
    public String getPassport() {
        return super.getPassport();
    }

    public RemotePerson(String firstName, String lastName, String passport) {
        super(firstName, lastName, passport);
    }

}
