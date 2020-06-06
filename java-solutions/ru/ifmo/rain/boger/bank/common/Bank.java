package ru.ifmo.rain.boger.bank.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Bank extends Remote {

    /**
     * Creates new account with given subId for given {@link Person}
     *
     * @param person given person
     * @param subId  given subID
     * @return null if person with given passport not found. If account is new - returns new {@link Account},
     * otherwise - returns existing.
     * @throws RemoteException if remote error occurred
     */
    Account createAccount(Person person, String subId) throws RemoteException;

    /**
     * Searches account with given global ID in format {@code passport:subID}
     *
     * @param id global id
     * @return null if per not found. Otherwise - matching {@link Person}
     * @throws RemoteException if remote error occurred
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Creates new {@link Person}. If person with given credentials already exists - does nothing.
     * @param firstName person first name
     * @param lastName person last name
     * @param passport person passport
     * @throws RemoteException if remote error occurred
     */
    void createPerson(String firstName, String lastName, String passport) throws RemoteException;

    /**
     * Returns remote {@link Person} with given passport
     * @param passport given passport
     * @return remote {@link Person} with given passport, null if person not found
     * @throws RemoteException if remote error occurred
     */
    Person getRemotePerson(String passport) throws RemoteException;

    /**
     * Returns local {@link Person} with given passport
     * @param passport given passport
     * @return local {@link Person} with given passport, null if person not found
     * @throws RemoteException if remote error occurred
     */
    Person getLocalPerson(String passport) throws RemoteException;

    /**
     * Returns {@link Map} of all person's accounts. Map keys are subIDs, values - the accounts.
     * If person is local - returns its local snapshot.
     * @param person given {@link Person}
     * @return {@link Map} of person's accounts
     * @throws RemoteException if remote error occurred
     */
    Map<String, Account> getPersonAccounts(Person person) throws RemoteException;
}
