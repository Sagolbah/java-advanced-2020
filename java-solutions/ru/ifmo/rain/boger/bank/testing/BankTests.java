package ru.ifmo.rain.boger.bank.testing;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class BankTests {
    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        Result result = junit.run(TestRemoteBank.class, TestClient.class);
        System.exit(result.getFailureCount() == 0 ? 0 : 1);
    }
}
