package ru.ifmo.rain.boger.bank;


import java.util.Map;

public class LocalPersonImpl implements LocalPerson {
    private final String firstName;
    private final String lastName;
    private final String passport;
    private final Map<String, Account> accounts;

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

    public LocalPersonImpl(String firstName, String lastName, String passport, Map<String, Account> accounts) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passport = passport;
        this.accounts = accounts;
    }

    public Map<String, Account> getAccounts() {
        return accounts;
    }
}

