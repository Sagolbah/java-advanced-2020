package ru.ifmo.rain.boger.bank;

import java.io.Serializable;
import java.util.Map;

public interface LocalPerson extends Person, Serializable {
    Map<String, Account> getAccounts();
}
