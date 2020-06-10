package ru.ifmo.rain.boger.bank.common;

import java.rmi.*;

public interface Account extends Remote {
    /**
     * Returns account identifier.
     */
    String getId() throws RemoteException;

    /**
     * Returns amount of money at the account.
     */
    int getAmount() throws RemoteException;

    /**
     * Sets amount of money at the account.
     */
    void setAmount(int amount) throws RemoteException;

    /**
     * Changes amount of money with provided {@code delta} parameter
     **/
    void changeAmount(int delta) throws RemoteException;
}