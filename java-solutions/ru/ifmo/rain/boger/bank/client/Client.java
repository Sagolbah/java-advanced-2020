package ru.ifmo.rain.boger.bank.client;

import ru.ifmo.rain.boger.bank.common.Account;
import ru.ifmo.rain.boger.bank.common.Bank;
import ru.ifmo.rain.boger.bank.common.Person;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
    public static void main(final String... args) throws RemoteException {
        if (args == null || args.length != 5) {
            System.out.println("Usage: Client <first name> <last name> <passport> <account ID> <money change>");
            return;
        }
        for (String s : args) {
            if (s == null) {
                System.out.println("All arguments must not be null");
            }
        }
        String firstName = args[0];
        String lastName = args[1];
        String passport = args[2];
        String accountId = args[3];
        int moneyChange;
        try {
            moneyChange = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.out.println("Money change must be a number");
            return;
        }
        final Bank bank = getBank();
        System.out.println("Getting account with passport " + passport);
        Person person = bank.getRemotePerson(passport);
        if (person == null) {
            bank.createPerson(firstName, lastName, passport);
            person = bank.getRemotePerson(passport);
        } else {
            if (!person.getFirstName().equals(firstName) || !person.getLastName().equals(lastName) ||
                    !person.getPassport().equals(passport)) {
                System.out.println("Given person data is incorrect");
                return;
            }
        }
        System.out.println("Getting account with subID" + accountId);
        Account account = bank.getPersonAccounts(person).get(accountId);
        if (account == null) {
            System.out.println("Account not found - creating new");
            account = bank.createAccount(person, accountId);
        }
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        account.setAmount(account.getAmount() + moneyChange);
        System.out.println("New amount: " + account.getAmount());
    }

    private static Bank getBank() throws RemoteException {
        try {
            return (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
        }
        return null;
    }
}
