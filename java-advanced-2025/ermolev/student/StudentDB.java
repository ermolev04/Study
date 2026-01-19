package info.kgeorgiy.ja.ermolev.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {

    private static final Comparator<Student> compForName = Comparator.comparing(Student::firstName)
            .thenComparing(Student::lastName)
            .thenComparing(Student::id);

    private <T> List<T> readData(List<Student> students, Function<Student, T> f) {
        return students.stream().map(f).toList();
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return readData(students, Student::firstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return readData(students, Student::lastName);
    }

    @Override
    public List<GroupName> getGroupNames(List<Student> students) {
        return readData(students, Student::groupName);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return readData(students, s -> s.firstName() + " " + s.lastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(getFirstNames(students));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Student::compareTo)
                .map(Student::firstName)
                .orElse("");
    }


    private static <T> Stream<T> sort(final Stream<T> students, final Comparator<T> comparator) {
        return students.sorted(comparator);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sort(students.stream(), Student::compareTo).toList();
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sort(students.stream(), compForName).toList();
    }

    private List<Student> find(Collection<Student> students, Predicate<Student> pred) {
        return sortStudentsByName(students.stream()
                .filter(pred)
                .toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return find(students, s -> s.firstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String surName) {
        return find(students, s -> s.lastName().equals(surName));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return find(students, s -> s.groupName().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group).stream()
                .collect(Collectors.toMap(
                        Student::lastName,
                        Student::firstName,
                        (oldName, newName) -> oldName
                ));
    }
}
