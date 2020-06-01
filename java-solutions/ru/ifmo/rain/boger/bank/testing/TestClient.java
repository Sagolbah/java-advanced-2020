package ru.ifmo.rain.boger.bank.testing;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ru.ifmo.rain.boger.bank.Account;
import ru.ifmo.rain.boger.bank.Bank;
import ru.ifmo.rain.boger.bank.Client;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestClient {
    public static final int DEFAULT_REGISTRY_PORT = 1099;
    private static final int BANK_PORT = 8888;
    private static Registry registry;
    private static Client client;
    private static Bank bank = null;

    @BeforeClass
    public static void init() {
        System.out.println("Running client tests");
        try {
            registry = TestingUtils.startRmiRegistry(DEFAULT_REGISTRY_PORT);
        } catch (RemoteException e) {
            System.err.println("ERROR: Can't start RMI Registry" + e.getMessage());
            e.printStackTrace();
        }
        bank = TestingUtils.startBank(BANK_PORT);
    }

    @AfterClass
    public static void finish() {
        try {
            UnicastRemoteObject.unexportObject(bank, true);
        } catch (NoSuchObjectException e) {
            System.err.println("Could not unexport bank " + e.getMessage());
        }
        TestingUtils.unexportRegistry(registry);
    }

    @Test
    public void test01_increments() throws RemoteException {
        for (int i = 0; i < 100; i++) {
            Client.main("Georgiy", "Korneev", "666:666", "Sub", "1");
        }
        Account account = bank.getAccount("666:666:Sub");
        assertEquals(100, account.getAmount());
    }

    @Test
    public void test02_twoAccounts() throws RemoteException {
        for (int i = 0; i < 100; i++) {
            Client.main("Georgiy", "Korneev", "666:666", "Sub2", "10");
            Client.main("Georgiy", "Korneev", "666:666", "Sub", "-1");
        }
        Account first = bank.getAccount("666:666:Sub");
        Account second = bank.getAccount("666:666:Sub2");
        assertEquals(0, first.getAmount());
        assertEquals(1000, second.getAmount());
    }


}
