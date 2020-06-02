package ru.ifmo.rain.boger.bank.testing;

import ru.ifmo.rain.boger.bank.common.Bank;
import ru.ifmo.rain.boger.bank.server.RemoteBank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public final class TestingUtils {
    public static Bank startBank(final int bankPort) {
        Bank bank = new RemoteBank(bankPort);
        try {
            UnicastRemoteObject.exportObject(bank, bankPort);
            Naming.rebind("//localhost/bank", bank);
        } catch (RemoteException e) {
            System.err.println("Couldn't export object: " + e.getMessage());
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL");
        }
        return bank;
    }

    public static Registry startRmiRegistry(final int registryPort) throws RemoteException {
        return LocateRegistry.createRegistry(registryPort);
    }


}
