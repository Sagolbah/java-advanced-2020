#!/bin/bash

testingPath="ru.ifmo.rain.boger.bank.testing"

# cd to java-solutions
cd "../java-solutions" || exit
java -cp "../../java-advanced-2020/lib/junit-4.11.jar":"../../java-advanced-2020/lib/hamcrest-core-1.3.jar":"." org.junit.runner.JUnitCore "$testingPath.TestRemoteBank" "$testingPath.TestClient"

exit "$?"
