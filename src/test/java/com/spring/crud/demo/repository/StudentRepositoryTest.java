package com.spring.crud.demo.repository;

import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.HelperUtil;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    private static Tuple[] expectedStudents = null;

    @BeforeAll
    static void init() {
        expectedStudents = HelperUtil.studentSupplier.get().stream()
                .map(student -> AssertionsForClassTypes.tuple(student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()))
                .toArray(Tuple[]::new);
    }

    @Test
    void testGivenNon_WhenFindAll_ThenReturnAllRecord() {
        // Given

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
        Optional<Student> optionalRahulGhadage = studentRepository.findAll().stream().filter(student -> student.getFirstName().equals("Rahul") && student.getLastName().equals("Ghadage")).findFirst();
        Student expectedRahulGhadage = optionalRahulGhadage.orElseGet(() -> new Student());

        // When
        Optional<Student> actualStudent = studentRepository.findById(expectedRahulGhadage.getId());

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isNotEmpty();
        Assertions.assertThat(actualStudent.get()).isEqualTo(expectedRahulGhadage);
    }

    @Test
    void testGivenId_WhenFindByRollNo_ThenReturnRecord() {
        // Given
        Optional<Student> optionalRahulGhadage = studentRepository.findAll().stream().filter(student -> student.getFirstName().equals("Rahul") && student.getLastName().equals("Ghadage")).findFirst();
        Student expectedRahulGhadage = optionalRahulGhadage.orElseGet(() -> new Student());

        // When
        Optional<Student> actualStudent = studentRepository.findByRollNo(expectedRahulGhadage.getRollNo());

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isNotEmpty();
        Assertions.assertThat(actualStudent.get()).isEqualTo(expectedRahulGhadage);
    }

    @Test
    void testGivenId_WhenFindByFirstName_ThenReturnRecord() {
        // Given
        Optional<Student> optionalRahulGhadage = studentRepository.findAll().stream().filter(student -> student.getFirstName().equals("Rahul") && student.getLastName().equals("Ghadage")).findFirst();
        Student expectedRahulGhadage = optionalRahulGhadage.orElseGet(() -> new Student());

        // When
        Optional<Student> actualStudent = studentRepository.findByFirstName(expectedRahulGhadage.getFirstName());

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isNotEmpty();
        Assertions.assertThat(actualStudent.get()).isEqualTo(expectedRahulGhadage);
    }

    @Test
    void testGivenId_WhenFindByFirstName_IgnoreCaseThenReturnRecord() {
        // Given
        Optional<Student> optionalRahulGhadage = studentRepository.findAll().stream().filter(student -> student.getFirstName().equals("Rahul") && student.getLastName().equals("Ghadage")).findFirst();
        Student expectedRahulGhadage = optionalRahulGhadage.orElseGet(() -> new Student());

        // When
        List<Student> actualStudents = studentRepository.findByFirstNameIgnoreCase(expectedRahulGhadage.getFirstName().toLowerCase());

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        Assertions.assertThat(actualStudents.get(0)).isEqualTo(expectedRahulGhadage);
    }

    @Test
    void testGivenId_WhenFindByLastName_IgnoreCaseThenReturnRecord() {
        // Given
        Optional<Student> optionalRahulGhadage = studentRepository.findAll().stream().filter(student -> student.getFirstName().equals("Rahul") && student.getLastName().equals("Ghadage")).findFirst();
        Student expectedRahulGhadage = optionalRahulGhadage.orElseGet(() -> new Student());

        // When
        List<Student> actualStudents = studentRepository.findByLastNameIgnoreCase(expectedRahulGhadage.getLastName().toLowerCase());

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        Assertions.assertThat(actualStudents.get(0)).isEqualTo(expectedRahulGhadage);
    }

    @Test
    void testGivenId_WhenFindByFirstNameLike_ThenReturnRecord() {
        // Given
        Optional<Student> optionalRahulGhadage = studentRepository.findAll().stream().filter(student -> student.getFirstName().equals("Rahul") && student.getLastName().equals("Ghadage")).findFirst();
        Student expectedRahulGhadage = optionalRahulGhadage.orElseGet(() -> new Student());

        // When
        List<Student> actualStudents = studentRepository.findByFirstNameLike("%" + expectedRahulGhadage.getFirstName().substring(1, 5) + "%");

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        Assertions.assertThat(actualStudents.get(0)).isEqualTo(expectedRahulGhadage);
    }

    @Test
    void testGivenId_WhenFindByMarksGreaterThanEqual_ThenReturnRecord() {
        // Given
        Tuple[] expectedTupleStudents = HelperUtil.studentSupplier.get().stream()
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
        Optional<Student> optionalRahulGhadage = studentRepository.findAll().stream().filter(student -> student.getFirstName().equals("Rahul") && student.getLastName().equals("Ghadage")).findFirst();
        Student expectedRahulGhadage = optionalRahulGhadage.orElseGet(() -> new Student());

        // When
        Boolean actualStudent = studentRepository.existsById(expectedRahulGhadage.getId());

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
        Optional<Student> optionalRahulGhadage = studentRepository.findAll().stream().filter(student -> student.getFirstName().equals("Rahul") && student.getLastName().equals("Ghadage")).findFirst();
        Student exampleStudent = optionalRahulGhadage.orElseGet(() -> new Student());

        // When
        Example<Student> studentExample = Example.of(exampleStudent, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        List<Student> actualStudents = studentRepository.findAll(studentExample);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        Assertions.assertThat(actualStudents.get(0)).isEqualTo(exampleStudent);
    }


    @ParameterizedTest
    @MethodSource(value = "generateExample")
    void testGivenExample_WhenFindByExample_ThenReturn2Record(Example<Student> studentExample, int count) {
        // Given
        Tuple[] expectedTupleStudents = HelperUtil.studentSupplier.get().stream()
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
        Student salmanKhan = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);

        // When
        Student student = studentRepository.save(salmanKhan);

        // Then
        Assertions.assertThat(student).isNotNull();
        Assertions.assertThat(student.getRollNo()).isEqualTo(salmanKhan.getRollNo());
        Assertions.assertThat(student.getFirstName()).isEqualTo(salmanKhan.getFirstName());
        Assertions.assertThat(student.getLastName()).isEqualTo(salmanKhan.getLastName());
        Assertions.assertThat(student.getDateOfBirth()).isEqualTo(salmanKhan.getDateOfBirth());
        Assertions.assertThat(student.getMarks()).isEqualTo(salmanKhan.getMarks());
    }

    @Test
    void testGivenId_WhenDeleteRecord_ThenReturnTrue() {
        // Given
        Optional<Student> optionalRahulGhadage = studentRepository.findAll().stream().filter(student -> student.getFirstName().equals("Rahul") && student.getLastName().equals("Ghadage")).findFirst();
        Student expectedRahulGhadage = optionalRahulGhadage.orElseGet(() -> new Student());

        // When
        studentRepository.deleteById(expectedRahulGhadage.getId());
        Boolean deletedStudent = studentRepository.existsById(expectedRahulGhadage.getId());

        // Then
        Assertions.assertThat(deletedStudent).isFalse();
    }

    @Test
    void testGivenId_WhenEditRecord_ThenReturnEditedRecord() {
        // Given
        Optional<Student> optionalRahulGhadage = studentRepository.findAll().stream().filter(student -> student.getFirstName().equals("Rahul") && student.getLastName().equals("Ghadage")).findFirst();
        Student expectedRahulGhadage = optionalRahulGhadage.orElseGet(() -> new Student());

        // When
        Optional<Student> optionalStudent = studentRepository.findById(expectedRahulGhadage.getId());
        Student editStudent = optionalStudent.orElseGet(() -> new Student());
        editStudent.setMarks(999.0f);
        Student student = studentRepository.save(editStudent);

        // Then
        Assertions.assertThat(student).isNotNull();
        Assertions.assertThat(student.getId()).isEqualTo(editStudent.getId());
        Assertions.assertThat(student.getRollNo()).isEqualTo(editStudent.getRollNo());
        Assertions.assertThat(student.getFirstName()).isEqualTo(editStudent.getFirstName());
        Assertions.assertThat(student.getLastName()).isEqualTo(editStudent.getLastName());
        Assertions.assertThat(student.getDateOfBirth()).isEqualTo(editStudent.getDateOfBirth());
        Assertions.assertThat(student.getMarks()).isEqualTo(editStudent.getMarks());
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

}