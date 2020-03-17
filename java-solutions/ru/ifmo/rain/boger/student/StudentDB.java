package ru.ifmo.rain.boger.student;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import info.kgeorgiy.java.advanced.student.AdvancedStudentGroupQuery;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.Group;

public class StudentDB implements AdvancedStudentGroupQuery {
    private static final Comparator<Student> FULL_NAME_COMPARATOR = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName).thenComparing(Student::getId);
    private static final String EMPTY_ANSWER = "";

    // Utility functions

    private <T> List<T> mapToList(final Collection<Student> students, final Function<Student, T> mapping) {
        return mapStudentsToCollection(students, mapping, ArrayList::new);
    }

    private <T, C extends Collection<T>> C mapStudentsToCollection(final Collection<Student> students,
                                                                   final Function<Student, T> mapping,
                                                                   final Supplier<C> collection) {
        return students.stream().map(mapping).collect(Collectors.toCollection(collection));
    }

    private List<Student> sortStudents(final Collection<Student> students, final Comparator<Student> comparator) {
        return students.stream().sorted(comparator).collect(Collectors.toList());
    }

    private List<Student> searchStudents(final Collection<Student> students,
                                         final Function<Student, String> mapping, final String query) {
        return students.stream().sorted(FULL_NAME_COMPARATOR).filter(s -> query.equals(mapping.apply(s))).
                collect(Collectors.toList());
    }

    private Stream<Map.Entry<String, List<Student>>> sortedByGroupStudentsStream(final Collection<Student> students) {
        // Returns stream of map entries <group, list<student>> where group_i <= group_{i+1}
        return students.stream().collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList()))
                .entrySet().stream();
    }

    private List<Group> sortGroupsAndStudents(final Collection<Student> students, final UnaryOperator<List<Student>> studentSorter) {
        return sortedByGroupStudentsStream(students).
                map(groupPair -> new Group(groupPair.getKey(),
                        studentSorter.apply(groupPair.getValue()))).collect(Collectors.toList());
    }

    private String maxSearchInGroups(final Collection<Student> students, final ToIntFunction<List<Student>> criteria) {
        return sortedByGroupStudentsStream(students).map
                (groupPair -> Map.entry(groupPair.getKey(), criteria.applyAsInt(groupPair.getValue())))
                .max(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey).orElse(EMPTY_ANSWER);
    }

    private String getFullName(final Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    private List<String> getByIndices(final List<String> students, final int[] indices) {
        return Arrays.stream(indices).mapToObj(students::get).collect(Collectors.toList());
    }

    // Methods

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapToList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapToList(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return mapToList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapToList(students, this::getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return mapStudentsToCollection(students, Student::getFirstName, TreeSet::new);
        //return students.stream().map(Student::getFirstName).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(Student::compareTo).map(Student::getFirstName).orElse(EMPTY_ANSWER);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudents(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudents(students, FULL_NAME_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return searchStudents(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return searchStudents(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return searchStudents(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return findStudentsByGroup(students, group).stream().collect(
                Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return sortGroupsAndStudents(students, this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return sortGroupsAndStudents(students, this::sortStudentsById);
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return maxSearchInGroups(students, List::size);
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return maxSearchInGroups(students, (studentList) -> getDistinctFirstNames(studentList).size());
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return students.stream().collect(Collectors.groupingBy(this::getFullName, HashMap::new,
                Collectors.collectingAndThen(Collectors.toList(),
                        list -> list.stream().map(Student::getGroup).distinct().count()))).entrySet().stream().max(
                Comparator.comparingLong(Map.Entry<String, Long>::getValue).thenComparing(
                        Map.Entry::getKey)).map(Map.Entry::getKey).orElse(EMPTY_ANSWER);
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getByIndices(mapToList(students, Student::getFirstName), indices);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getByIndices(mapToList(students, Student::getLastName), indices);
    }

    @Override
    public List<String> getGroups(Collection<Student> students, int[] indices) {
        return getByIndices(mapToList(students, Student::getGroup), indices);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getByIndices(mapToList(students, this::getFullName), indices);
    }

}
