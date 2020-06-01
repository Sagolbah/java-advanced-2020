package ru.ifmo.rain.boger.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> personByPassportMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Map<String, Account>> personsAccounts = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    public Account createAccount(final Person person, final String subId) throws RemoteException {
        System.out.println("Creating account with subId " + subId + " for user " + person.getLastName());
        final String personPassport = person.getPassport();
        if (personByPassportMap.get(personPassport) == null) {
            System.out.println("Person with passport " + personPassport + " is not registered");
            return null;
        }
        final String fullId = personPassport + ':' + subId;
        final Account account = new RemoteAccount(personPassport + ':' + subId);
        if (accounts.putIfAbsent(fullId, account) == null) {
            // Only one thread among all threads with same id will enter here.
            personsAccounts.get(personPassport).put(subId, account);
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(fullId);
        }
    }

    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    public void createPerson(final String firstName, final String lastName, final String passport) throws RemoteException {
        System.out.println("Creating new person: " + firstName + " " + lastName);
        RemotePerson person = new RemotePerson(firstName, lastName, passport);
        if (personByPassportMap.putIfAbsent(passport, person) == null) {
            // Same as createAccount - only one thread will enter this
            personsAccounts.put(passport, new ConcurrentHashMap<>());
            UnicastRemoteObject.exportObject(person, port);
            System.out.println("New person created successfully");
        } else {
            System.out.println("Person already exists");
        }
    }

    public Map<String, Account> getPersonAccounts(final Person person) throws RemoteException {
        System.out.format("Getting all accounts for %s %s%n", person.getFirstName(), person.getLastName());
        if (person instanceof LocalPerson) {
            System.out.println("Person is local - returning local storage");
            return ((LocalPerson) person).getAccounts();

        }
        // Remote person
        System.out.println("Remote person - returning actual data");
        return personsAccounts.get(person.getPassport());
    }

    public Person getLocalPerson(final String passport) throws RemoteException {
        Person person = personByPassportMap.get(passport);
        if (person == null) {
            System.out.println("Person with given passport not found. Aborting.");
            return null;
        }
        Map<String, Account> localAccounts = createLocalAccounts(passport);
        return new LocalPersonImpl(person.getFirstName(), person.getLastName(), passport, localAccounts);
    }

    public Person getRemotePerson(final String passport) {
        System.out.println("Getting remote person with passport " + passport);
        return personByPassportMap.get(passport);
    }

    private Map<String, Account> createLocalAccounts(final String passport) {
        Map<String, Account> result = new HashMap<>();
        personsAccounts.get(passport).forEach((k, v) -> {
            try {
                result.put(k, new LocalAccount(v.getId(), v.getAmount()));
            } catch (RemoteException e) {
                System.out.println("Local account creation failed");
            }
        });
        return result;
    }

}
