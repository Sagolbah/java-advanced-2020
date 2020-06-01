package ru.ifmo.rain.boger.bank;

import java.io.Serializable;
import java.rmi.RemoteException;

public class LocalAccount implements Account, Serializable {
    private final String id;
    private int amount;

    public LocalAccount(String id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        System.out.println("Getting amount of money for local account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(int amount) {
        System.out.println("Setting amount of money for local account " + id);
        this.amount = amount;
    }

}
