package ru.ifmo.rain.boger.bank.testing;

import org.junit.*;
import org.junit.runners.MethodSorters;
import ru.ifmo.rain.boger.bank.common.Account;
import ru.ifmo.rain.boger.bank.common.Bank;
import ru.ifmo.rain.boger.bank.common.Person;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestRemoteBank {
    public static final int DEFAULT_REGISTRY_PORT = 1099;
    private static final int BANK_PORT = 8888;
    private static Registry registry;
    private static Bank bank = null;

    @BeforeClass
    public static void initialize() {
        System.out.println("Running remote bank tests");
        try {
            registry = TestingUtils.startRmiRegistry(DEFAULT_REGISTRY_PORT);
        } catch (RemoteException e) {
            System.err.println("ERROR: Can't start RMI Registry" + e.getMessage());
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void finish() {
        try {
            UnicastRemoteObject.unexportObject(bank, true);
        } catch (NoSuchObjectException e) {
            System.err.println("Could not unexport bank " + e.getMessage());
        }
    }

    @Before
    public void createNewBank() {
        if (bank != null) {
            try {
                UnicastRemoteObject.unexportObject(bank, true);
            } catch (NoSuchObjectException e) {
                System.err.println("Unexporting object not found " + e.getMessage());
            }
        }
        bank = TestingUtils.startBank(BANK_PORT);
    }

    private Person createRemotePerson(final String firstName, final String lastName, final String passport) throws RemoteException {
        bank.createPerson(firstName, lastName, passport);
        return bank.getRemotePerson(passport);
    }

    private Account createAccountWithMoney(final Person person, final String subId, final int amount) throws RemoteException {
        Account account = bank.createAccount(person, subId);
        account.setAmount(amount);
        return account;
    }

    @Test
    public void test01_newRemotePerson() throws RemoteException {
        final String passport = "test";
        Person person = createRemotePerson("Alice", "Alice2", passport);
        Account account = createAccountWithMoney(person, "sub", 1);
        assertEquals(1, account.getAmount());
    }

    @Test
    public void test02_transaction() throws RemoteException {
        Person first = createRemotePerson("Gennady", "Korotkevich", "1");
        Person second = createRemotePerson("Niyaz", "Nigmatullin", "2");
        Account firstAccount = createAccountWithMoney(first, "s1", 100);
        Account secondAccount = createAccountWithMoney(second, "s2", 50);
        firstAccount.setAmount(firstAccount.getAmount() - 25);
        secondAccount.setAmount(secondAccount.getAmount() + 25);
        assertEquals(firstAccount.getAmount(), 75);
        assertEquals(secondAccount.getAmount(), 75);
    }

    @Test
    public void test03_selfTransaction() throws RemoteException {
        Person person = createRemotePerson("Vitalii", "Aksenov", "239");
        Account first = createAccountWithMoney(person, "test1", 10);
        Account second = createAccountWithMoney(person, "test2", 10);
        first.setAmount(first.getAmount() - 5);
        second.setAmount(second.getAmount() + 5);
        assertEquals(first.getAmount(), 5);
        assertEquals(second.getAmount(), 15);
    }

    @Test
    public void test04_evilIdSearch() throws RemoteException {
        final String arabicPassport = ":حَرَكِيَّةٌ";
        final String arabicSubId = "نحن";
        Person person = createRemotePerson("Arabic", "Person", arabicPassport);
        Account account = createAccountWithMoney(person, arabicSubId, 100);
        assertEquals(account, bank.getAccount(arabicPassport + ':' + arabicSubId));
    }

    @Test
    public void test05_localSnapshot() throws RemoteException {
        final String passport = "test";
        Person person = createRemotePerson("Joseph", "Joestar", passport);
        Account globalAccount = createAccountWithMoney(person, "test", 100);
        Account snapshot = bank.getPersonAccounts(bank.getLocalPerson(passport)).get("test");
        Account snapshot2 = bank.getPersonAccounts(bank.getLocalPerson(passport)).get("test");
        globalAccount.setAmount(globalAccount.getAmount() + 100);
        assertEquals(snapshot.getAmount(), 100);
        snapshot = bank.getPersonAccounts(bank.getLocalPerson(passport)).get("test");
        globalAccount.setAmount(globalAccount.getAmount() + 200);
        assertEquals(snapshot.getAmount(), 200);
        assertEquals(globalAccount.getAmount(), 400);
        assertEquals(snapshot2.getAmount(), 100);
    }

    @Test
    public void test06_multithreadedCreation() throws RemoteException {
        final int threadCount = 4;
        ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            threadPool.submit(() -> {
                try {
                    multithreadedCreationTask(finalI);
                } catch (RemoteException e) {
                    System.err.println("Parallel executing failed " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            // No operations.
        }
        threadPool.shutdownNow();
        for (int i = 0; i < 5000; i++) {
            Person person = bank.getRemotePerson("test" + i);
            assertEquals(bank.getPersonAccounts(person).get("sub" + i).getAmount(), i);
        }
    }

    private void multithreadedCreationTask(final int threadId) throws RemoteException {
        for (int i = 0; i < 1250; i++) {
            int actualId = threadId * 1250 + i;
            Person person = createRemotePerson("test", "test", "test" + actualId);
            createAccountWithMoney(person, "sub" + actualId, actualId);
        }
    }

}
