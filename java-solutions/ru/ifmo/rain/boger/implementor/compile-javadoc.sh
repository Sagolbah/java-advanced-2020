#!/bin/bash

fullPath="java-solutions/ru/ifmo/rain/boger/implementor"
output="$fullPath/_javadoc"
module="java-solutions"
supportDirectory="../java-advanced-2020/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor"
linkAPI="https://docs.oracle.com/en/java/javase/11/docs/api/"


# cd to repository root
cd ../../../../../.. || exit
javadoc -d "$output" --module-path "../java-advanced-2020/lib":"../java-advanced-2020/artifacts" -link "$linkAPI" -private -version -author "$fullPath/BaseImplementor.java" "$fullPath/Implementor.java" "$fullPath/JarImplementor.java" "$fullPath/CleaningVisitor.java" "$supportDirectory/Impler.java" "$supportDirectory/JarImpler.java" "$supportDirectory/ImplerException.java"
