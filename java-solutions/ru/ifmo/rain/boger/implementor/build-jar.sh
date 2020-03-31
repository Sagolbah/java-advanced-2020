#!/bin/bash

fullPath="ru/ifmo/rain/boger/implementor"
output="$fullPath/_build"
package="ru.ifmo.rain.boger.implementor"
jarOutput="$../_implementor.jar"
executingClass="$package.Implementor"

mkdir "$output" 2>/dev/null

# cd to java-solutions
cd ../../../../.. || exit

# Already compiled modules: at lib
javac --module-path "../../java-advanced-2020/lib":"../../java-advanced-2020/artifacts" "module-info.java" -d "$output" "$fullPath/Implementor.java" "$fullPath/JarImplementor.java" "$fullPath/BaseImplementor.java" "$fullPath/CleaningVisitor.java"

cd $fullPath/_build

jar -c --file "_implementor.jar" -e "$executingClass" .
mv _implementor.jar ..
