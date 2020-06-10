package ru.ifmo.rain.boger.bank.server;


import ru.ifmo.rain.boger.bank.common.Account;
import ru.ifmo.rain.boger.bank.common.LocalPerson;

import java.util.Map;

public class LocalPersonImpl extends AbstractPerson implements LocalPerson {
    private final Map<String, Account> accounts;

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

    public LocalPersonImpl(String firstName, String lastName, String passport, Map<String, Account> accounts) {
        super(firstName, lastName, passport);
        this.accounts = accounts;
    }

    public Map<String, Account> getAccounts() {
        return accounts;
    }
}

