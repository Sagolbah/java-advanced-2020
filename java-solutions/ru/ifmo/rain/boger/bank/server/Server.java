package ru.ifmo.rain.boger.bank.server;

import ru.ifmo.rain.boger.bank.common.Bank;

import java.rmi.*;
import java.rmi.server.*;
import java.net.*;

public class Server {
    private static final int PORT = 8888;

    public static void main(final String... args) {
        final Bank bank = new RemoteBank(PORT);
        try {
            UnicastRemoteObject.exportObject(bank, PORT);
            Naming.rebind("//localhost/bank", bank);
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        }
        System.out.println("Server started");
    }
}
