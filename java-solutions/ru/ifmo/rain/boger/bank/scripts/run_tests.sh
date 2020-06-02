#!/bin/bash

# cd to java-solutions
cd "../../../../../.." || exit
java -cp "../../java-advanced-2020/lib/junit-4.11.jar":"../../java-advanced-2020/lib/hamcrest-core-1.3.jar":"." ru.ifmo.rain.boger.bank.testing.BankTests

exit "$?"
