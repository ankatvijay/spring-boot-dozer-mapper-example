package com.spring.crud.demo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
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
import java.util.Optional;
import java.util.stream.Stream;

@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudentRepositoryTest implements BaseRepositoryTest<Student> {

    @Autowired
    private StudentRepository studentRepository;
    private static final File file = FileLoader.getFileFromResource("students.json");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeFactory typeFactory = objectMapper.getTypeFactory();

    @BeforeEach
    void init() {
        studentRepository.deleteAll();
    }

    @Override
    @Test
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        studentRepository.saveAll(students);
        Tuple[] expectedStudents = students.stream()
                .map(student -> AssertionsForClassTypes.tuple(student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()))
                .toArray(Tuple[]::new);

        // When
        List<Student> actualStudents = studentRepository.findAll();

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents.size()).isGreaterThan(0);
        Assertions.assertThat(actualStudents)
                .extracting(Student::getRollNo,
                        Student::getFirstName,
                        Student::getLastName,
                        Student::getDateOfBirth,
                        Student::getMarks)
                .containsExactly(expectedStudents);
    }

    @Override
    @Test
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        Student actualStudent = studentRepository.findById(expectedStudent.getId()).orElseGet(Student::new);

        // Then
        assertRecord(expectedStudent, actualStudent);
    }

    @Override
    @Test
    public void testGivenRandomId_WhenGetRecordsById_ThenReturnEmpty() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Optional<Student> actualStudent = studentRepository.findById(id);

        // Then
        Assertions.assertThat(actualStudent).isEmpty();
    }

    @Test
    public void testGivenRollNo_WhenGetRecordByRollNo_ThenReturnRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        Student actualStudent = studentRepository.findByRollNo(expectedStudent.getRollNo()).orElseGet(Student::new);

        // Then
        assertRecord(expectedStudent, actualStudent);
    }

    @Test
    public void testGivenRandomRollNo_WhenGetRecordByRollNo_ThenReturnEmpty() {
        // Given
        int rollNo = RandomUtils.nextInt();

        // When
        Optional<Student> actualStudent = studentRepository.findByRollNo(rollNo);

        // Then
        Assertions.assertThat(actualStudent).isEmpty();
    }

    @Test
    public void testGivenFirstName_WhenGetRecordByFirstName_ThenReturnRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        Student actualStudent = studentRepository.findByFirstName(expectedStudent.getFirstName()).orElseGet(Student::new);

        // Then
        assertRecord(expectedStudent, actualStudent);
    }

    @Test
    public void testGivenRandomFirstName_WhenGetRecordByFirstName_ThenReturnEmpty() {
        // Given
        String firstName = RandomStringUtils.random(5);

        // When
        Optional<Student> actualStudent = studentRepository.findByFirstName(firstName);

        // Then
        Assertions.assertThat(actualStudent).isEmpty();
    }

    @Test
    public void testGivenFirstName_WhenGetRecordByFirstNameIgnoreCase_ThenReturnRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        Student actualStudent = studentRepository.findByFirstNameIgnoreCase(expectedStudent.getFirstName().toLowerCase()).orElseGet(Student::new);

        // Then
        assertRecord(expectedStudent, actualStudent);
    }

    @Test
    public void testGivenRandomFirstName_WhenGetRecordByFirstNameIgnoreCase_ThenReturnEmpty() {
        // Given
        String firstName = RandomStringUtils.random(5);

        // When
        Optional<Student> actualStudent = studentRepository.findByFirstNameIgnoreCase(firstName);

        // Then
        Assertions.assertThat(actualStudent).isEmpty();
    }

    @Test
    public void testGivenLastName_WhenFindByLastNameIgnoreCase_ThenReturnRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        Student actualStudent = studentRepository.findByLastNameIgnoreCase(expectedStudent.getLastName().toLowerCase()).orElseGet(Student::new);

        // Then
        assertRecord(expectedStudent, actualStudent);
    }

    @Test
    public void testGivenRandomLastName_WhenFindByLastNameIgnoreCase_ThenReturnEmpty() {
        // Given
        String firstName = RandomStringUtils.random(5);

        // When
        Optional<Student> actualStudent = studentRepository.findByLastNameIgnoreCase(firstName);

        // Then
        Assertions.assertThat(actualStudent).isEmpty();
    }

    @Test
    public void testGivenFirstName_WhenGetRecordByFirstNameLike_ThenReturnRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        List<Student> actualStudents = studentRepository.findByFirstNameLike("%" + expectedStudent.getFirstName().substring(1, 5) + "%");

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        assertRecord(expectedStudent, actualStudents.get(0));
    }

    @Test
    public void testGivenRandomFirstName_WhenGetRecordByFirstNameLike_ThenReturnEmptyListRecords() {
        // Given
        String firstName = RandomStringUtils.random(10);

        // When
        List<Student> actualStudents = studentRepository.findByFirstNameLike("%" + firstName.substring(1, 5) + "%");

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents.size()).isEqualTo(0);
    }

    @Test
    public void testGivenMarks_WhenGetRecordByMarksGreaterThanEqual_ThenReturnRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
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
    public void testGivenGreaterMarks_WhenGetRecordByMarksGreaterThanEqual_ThenReturnEmptyListRecords() {
        // Given
        float marks = RandomUtils.nextFloat(1000.f, 5000.f);


        // When
        List<Student> actualStudents = studentRepository.findByMarksGreaterThanEqual(marks);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents.size()).isEqualTo(0);
    }

    @Override
    @Test
    public void testGivenId_WhenExistRecordById_ThenReturnTrue() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        Boolean actualStudent = studentRepository.existsById(expectedStudent.getId());

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isTrue();
    }

    @Override
    @Test
    public void testGivenRandomId_WhenExistRecordById_ThenReturnFalse() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Boolean actualStudent = studentRepository.existsById(id);

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isFalse();
    }

    @Override
    @Test
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        Example<Student> studentExample = Example.of(expectedStudent, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        List<Student> actualStudents = studentRepository.findAll(studentExample);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        assertRecord(expectedStudent, actualStudents.get(0));
    }

    @Override
    @Test
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenReturnEmptyListRecords() {
        // Given
        Student expectedStudent = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);

        // When
        Example<Student> studentExample = Example.of(expectedStudent, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        List<Student> actualStudents = studentRepository.findAll(studentExample);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isEmpty();
    }

    @Override
    @ParameterizedTest
    @MethodSource(value = "generateExample")
    public void testGivenMultipleExample_WhenGetAllRecordsByExample_ThenReturnListRecord(Example<Student> studentExample, int count) throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
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

    @Override
    @Test
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() {
        // Given
        Student expectedStudent = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);

        // When
        Student actualStudent = studentRepository.save(expectedStudent);

        // Then
        assertRecord(expectedStudent, actualStudent);
    }

    @Override
    @Test
    public void testGivenExistingRecordAndUpdate_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);
        expectedStudent.setMarks(900f);

        // When
        Student actualStudent = studentRepository.save(expectedStudent);

        // Then
        assertRecord(expectedStudent, actualStudent);
    }

    @Override
    @Test
    public void testGivenIdAndUpdatedRecord_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student savedStudent = studentRepository.save(student);

        // When
        Student expectedStudent = studentRepository.findById(savedStudent.getId()).orElseGet(Student::new);
        expectedStudent.setMarks(999.0f);
        Student actualStudent = studentRepository.save(expectedStudent);

        // Then
        assertRecord(expectedStudent, actualStudent);
    }

    @Override
    @Test
    public void testGivenId_WhenDeleteRecord_ThenReturnFalse() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentRepository.save(student);

        // When
        studentRepository.deleteById(expectedStudent.getId());
        Boolean deletedStudent = studentRepository.existsById(expectedStudent.getId());

        // Then
        Assertions.assertThat(deletedStudent).isFalse();
    }

    @Override
    @Test
    public void testGivenRandomId_WhenDeleteRecord_ThenThrowException() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When & Then
        Assertions.assertThatThrownBy(() -> studentRepository.deleteById(id))
                .isInstanceOf(EmptyResultDataAccessException.class)
                .hasMessage(String.format("No class com.spring.crud.demo.model.Student entity with id %d exists!", id));
    }

    @Override
    @Test
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given
        studentRepository.deleteAll();

        // When
        List<Student> students = studentRepository.findAll();

        // Then
        Assertions.assertThat(students).isNotNull();
        Assertions.assertThat(students.size()).isEqualTo(0);
    }

    private static Stream<Arguments> generateExample() {
        Student studentWithDateOfBirth = new Student();
        studentWithDateOfBirth.setDateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)));

        return Stream.of(
                Arguments.of(Example.of(studentWithDateOfBirth, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 10)
        );
    }

    public void assertRecord(Student expectedRecord, Student actualRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getRollNo()).isEqualTo(expectedRecord.getRollNo());
        Assertions.assertThat(actualRecord.getFirstName()).isEqualTo(expectedRecord.getFirstName());
        Assertions.assertThat(actualRecord.getLastName()).isEqualTo(expectedRecord.getLastName());
        Assertions.assertThat(actualRecord.getDateOfBirth()).isEqualTo(expectedRecord.getDateOfBirth());
        Assertions.assertThat(actualRecord.getMarks()).isEqualTo(expectedRecord.getMarks());
    }
}