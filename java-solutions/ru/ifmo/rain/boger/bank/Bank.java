package ru.ifmo.rain.boger.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

public interface Bank extends Remote {

    Account createAccount(Person person, String subId) throws RemoteException;

    Account getAccount(String id) throws RemoteException;

    void createPerson(String firstName, String lastName, String passport) throws RemoteException;

    Person getRemotePerson(String passport) throws RemoteException;

    Person getLocalPerson(String passport) throws RemoteException;

    Map<String, Account> getPersonAccounts(Person person) throws RemoteException;
}
