package com.spring.crud.demo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    private static List<Student> students;

    @BeforeAll
    static void initOnce() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = FileLoader.getFileFromResource("students.json");
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
    }

    @BeforeEach
    void init() {
        studentRepository.deleteAll();

    }

    @Test
    void testGivenNon_WhenFindAll_ThenReturnAllRecord() {
        // Given
        studentRepository.saveAll(students);
        Tuple[] expectedStudents = students.stream()
                .map(student -> AssertionsForClassTypes.tuple(student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()))
                .toArray(Tuple[]::new);

        // When
        List<Student> students = studentRepository.findAll();

        // Then
        Assertions.assertThat(students).isNotNull();
        Assertions.assertThat(students.size()).isGreaterThan(0);
        Assertions.assertThat(students)
                .extracting(Student::getRollNo,
                        Student::getFirstName,
                        Student::getLastName,
                        Student::getDateOfBirth,
                        Student::getMarks)
                .containsExactly(expectedStudents);
    }

    @Test
    void testGivenId_WhenFindById_ThenReturnRecord() {
        // Given
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        Student actualStudent = studentRepository.findById(expectedStudent.getId()).orElseGet(Student::new);

        // Then
        assertStudent(expectedStudent, actualStudent);
    }

    @Test
    void testGivenId_WhenFindByRollNo_ThenReturnRecord() {
        // Given
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        Student actualStudent = studentRepository.findByRollNo(expectedStudent.getRollNo()).orElseGet(Student::new);

        // Then
        assertStudent(expectedStudent, actualStudent);
    }

    @Test
    void testGivenId_WhenFindByFirstName_ThenReturnRecord() {
        // Given
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        Student actualStudent = studentRepository.findByFirstName(expectedStudent.getFirstName()).orElseGet(Student::new);

        // Then
        assertStudent(expectedStudent, actualStudent);
    }

    @Test
    void testGivenId_WhenFindByFirstName_IgnoreCaseThenReturnRecord() {
        // Given
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        List<Student> actualStudents = studentRepository.findByFirstNameIgnoreCase(expectedStudent.getFirstName().toLowerCase());

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        assertStudent(expectedStudent,actualStudents.get(0));
    }

    @Test
    void testGivenId_WhenFindByLastName_IgnoreCaseThenReturnRecord() {
        // Given
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        List<Student> actualStudents = studentRepository.findByLastNameIgnoreCase(expectedStudent.getLastName().toLowerCase());

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        assertStudent(expectedStudent,actualStudents.get(0));
    }

    @Test
    void testGivenId_WhenFindByFirstNameLike_ThenReturnRecord() {
        // Given
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        List<Student> actualStudents = studentRepository.findByFirstNameLike("%" + expectedStudent.getFirstName().substring(1, 5) + "%");

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        assertStudent(expectedStudent,actualStudents.get(0));
    }

    @Test
    void testGivenId_WhenFindByMarksGreaterThanEqual_ThenReturnRecord() {
        // Given
        studentRepository.saveAll(students);
        Tuple[] expectedTupleStudents = students.stream()
                .filter(student -> student.getMarks() >= 800.0f)
                .map(student -> AssertionsForClassTypes.tuple(student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()))
                .toArray(Tuple[]::new);


        // When
        List<Student> actualStudents = studentRepository.findByMarksGreaterThanEqual(800.0f);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(6);
        Assertions.assertThat(actualStudents)
                .extracting(Student::getRollNo,
                        Student::getFirstName,
                        Student::getLastName,
                        Student::getDateOfBirth,
                        Student::getMarks)
                .containsExactly(expectedTupleStudents);
    }

    @Test
    void testGivenId_WhenExistsById_ThenReturnRecord() {
        // Given
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        Boolean actualStudent = studentRepository.existsById(expectedStudent.getId());

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isTrue();
    }

    @Test
    void testGivenRandomId_WhenExistsById_ThenReturnRecord() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Boolean actualStudent = studentRepository.existsById(id);

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isFalse();
    }

    @Test
    void testGivenExample_WhenFindByExample_ThenReturn1Record() {
        // Given
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        Example<Student> studentExample = Example.of(expectedStudent, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        List<Student> actualStudents = studentRepository.findAll(studentExample);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        assertStudent(expectedStudent,actualStudents.get(0));
    }


    @ParameterizedTest
    @MethodSource(value = "generateExample")
    void testGivenExample_WhenFindByExample_ThenReturn2Record(Example<Student> studentExample, int count) {
        // Given
        studentRepository.saveAll(students);
        Tuple[] expectedTupleStudents = students.stream()
                .filter(student -> student.getDateOfBirth().equals(studentExample.getProbe().getDateOfBirth()))
                .map(student -> AssertionsForClassTypes.tuple(student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()))
                .toArray(Tuple[]::new);

        // When
        List<Student> actualStudents = studentRepository.findAll(studentExample);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents.size()).isEqualTo(count);
        Assertions.assertThat(actualStudents)
                .extracting(Student::getRollNo,
                        Student::getFirstName,
                        Student::getLastName,
                        Student::getDateOfBirth,
                        Student::getMarks)
                .containsExactly(expectedTupleStudents);
    }

    @Test
    void test_saveGivenStudent_WhenSave_ThenReturnStudent() {
        // Given
        Student expectedStudent = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);

        // When
        Student actualStudent = studentRepository.save(expectedStudent);

        // Then
        assertStudent(expectedStudent,actualStudent);
    }

    @Test
    void testGivenId_WhenDeleteRecord_ThenReturnTrue() {
        // Given
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        studentRepository.deleteById(expectedStudent.getId());
        Boolean deletedStudent = studentRepository.existsById(expectedStudent.getId());

        // Then
        Assertions.assertThat(deletedStudent).isFalse();
    }

    @Test
    void testGivenId_WhenEditRecord_ThenReturnEditedRecord() {
        // Given
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student savedStudent = studentRepository.save(student);

        // When
        Student expectedStudent = studentRepository.findById(savedStudent.getId()).orElseGet(Student::new);
        expectedStudent.setMarks(999.0f);
        Student actualStudent = studentRepository.save(expectedStudent);

        // Then
        assertStudent(expectedStudent,actualStudent);
    }

    @Test
    void testGivenNon_WhenFindAll_ThenReturnEmptyRecord() {
        // Given
        studentRepository.deleteAll();

        // When
        List<Student> students = studentRepository.findAll();

        // Then
        Assertions.assertThat(students).isNotNull();
        Assertions.assertThat(students.size()).isEqualTo(0);
    }

    @Test
    void testGivenId_WhenDeleteId_ThenThrowException() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(EmptyResultDataAccessException.class, () -> studentRepository.deleteById(id));

        // Then
        Assertions.assertThat(exception).isInstanceOf(EmptyResultDataAccessException.class);
        Assertions.assertThat(exception.getMessage()).isEqualTo(String.format("No class com.spring.crud.demo.model.Student entity with id %d exists!", id));
    }

    private static Stream<Arguments> generateExample() {
        Student studentWithDateOfBirth = new Student();
        studentWithDateOfBirth.setDateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)));

        return Stream.of(
                Arguments.of(Example.of(studentWithDateOfBirth, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 10)
        );
    }

    private void assertStudent(Student expectedStudent, Student actualStudent) {
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent.getRollNo()).isEqualTo(expectedStudent.getRollNo());
        Assertions.assertThat(actualStudent.getFirstName()).isEqualTo(expectedStudent.getFirstName());
        Assertions.assertThat(actualStudent.getLastName()).isEqualTo(expectedStudent.getLastName());
        Assertions.assertThat(actualStudent.getDateOfBirth()).isEqualTo(expectedStudent.getDateOfBirth());
        Assertions.assertThat(actualStudent.getMarks()).isEqualTo(expectedStudent.getMarks());
    }

}