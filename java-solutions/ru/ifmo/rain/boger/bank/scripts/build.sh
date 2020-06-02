#!/bin/bash

# cd to java-solutions
cd "../../../../../.." || exit

fullPath="ru/ifmo/rain/boger/bank"

javac -cp "../../java-advanced-2020/lib/junit-4.11.jar":"../../java-advanced-2020/lib/hamcrest-core-1.3.jar":"." $(find "$fullPath/" -name "*.java")
