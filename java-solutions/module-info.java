/**
 * Homework solutions for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 * @author Daniil Boger (github.com/Sagolbah)
 */

module java.solutions {
    requires info.kgeorgiy.java.advanced.implementor;
    requires java.compiler;

    exports ru.ifmo.rain.boger.implementor;

    opens ru.ifmo.rain.boger.implementor;

}
