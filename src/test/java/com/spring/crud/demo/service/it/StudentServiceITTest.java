package com.spring.crud.demo.service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.service.BaseServiceTest;
import com.spring.crud.demo.service.StudentService;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@SpringBootTest(value = "StudentServiceITTest")
class StudentServiceITTest implements BaseServiceTest<Student> {

    @Autowired
    private StudentService studentService;
    private static Tuple[] expectedStudents = null;
    private static List<Student> studentes;

    @BeforeAll
    static void initOnce() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        File file = FileLoader.getFileFromResource("students.json");
        studentes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        expectedStudents = studentes.stream()
                .map(student -> AssertionsForClassTypes.tuple(
                        student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()
                )).toArray(Tuple[]::new);
    }

    @BeforeEach
    void init() {
        studentService.deleteAllRecords();
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() {
        // Given
        studentes.forEach(student -> studentService.insertRecord(student));

        // When
        List<Student> actualStudents = studentService.getAllRecords();

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

    @Test
    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() {
        // Given
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentService.insertRecord(student).orElseGet(Student::new);

        // When
        Student actualStudent = studentService.getRecordsById(expectedStudent.getId()).orElseGet(Student::new);

        // Then
        assertRecord(expectedStudent, actualStudent);
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Assertions.assertThatThrownBy(() -> studentService.getRecordsById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);

        // Then
    }

    @Test
    @Override
    public void testGivenId_WhenExistRecordById_ThenReturnTrue() {
        // Given
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentService.insertRecord(student).orElseGet(Student::new);

        // When
        Boolean actualStudent = studentService.existRecordById(expectedStudent.getId());

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isTrue();
    }

    @Test
    @Override
    public void testGivenRandomId_WhenExistRecordById_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Boolean actualStudent = studentService.existRecordById(id);

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isFalse();
    }

    @Test
    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() {
        // Given
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentService.insertRecord(student).orElseGet(Student::new);

        // When
        List<Student> actualStudents = studentService.getAllRecordsByExample(expectedStudent);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        assertRecord(actualStudents.get(0), expectedStudent);
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenReturnEmptyListRecords() {
        // Given
        Student expectedStudent = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);

        // When
        List<Student> actualStudents = studentService.getAllRecordsByExample(expectedStudent);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isEmpty();
    }

    @ParameterizedTest
    @MethodSource(value = "generateExample")
    @Override
    public void testGivenMultipleExample_WhenGetAllRecordsByExample_ThenReturnListRecord(Example<Student> example, int count) throws IOException {
        // Given
        studentService.insertBulkRecords(studentes);
        List<Student> expectedStudentes = studentes.stream()
                .filter(student -> student.getDateOfBirth().equals(example.getProbe().getDateOfBirth())).toList();
        Tuple[] expectedTupleStudents = expectedStudentes.stream()
                .map(student -> AssertionsForClassTypes.tuple(student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()))
                .toArray(Tuple[]::new);

        // When
        List<Student> actualStudents = studentService.getAllRecordsByExample(example.getProbe());

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
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() {
        // Given
        Student expectedStudent = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);

        // When
        Student actualStudent = studentService.insertRecord(expectedStudent).orElseGet(Student::new);

        // Then
        assertRecord(expectedStudent, actualStudent);
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() {
        // Given
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentService.insertRecord(student).orElseGet(Student::new);

        // When
        Assertions.assertThatThrownBy(() -> studentService.insertRecord(expectedStudent))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedStudent.getId());

        // Then
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() {
        // Given
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student savedStudent = studentService.insertRecord(student).orElseGet(Student::new);

        // When
        Student expectedStudent = studentService.getRecordsById(savedStudent.getId()).orElseGet(Student::new);
        expectedStudent.setMarks(999.0f);
        Student actualStudent = studentService.updateRecord(savedStudent.getId(), expectedStudent).orElseGet(Student::new);

        // Then
        assertRecord(expectedStudent, actualStudent);
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Assertions.assertThatThrownBy(() -> studentService.updateRecord(id, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Payload record id is null");
    }

    @Test
    @Override
    public void testGivenExistingRecordAndRandomId_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student savedStudent = studentService.insertRecord(student).orElseGet(Student::new);

        // When & Then
        Student expectedStudent = studentService.getRecordsById(savedStudent.getId()).orElseGet(Student::new);
        expectedStudent.setMarks(999.0f);
        Assertions.assertThatThrownBy(() -> studentService.updateRecord(id, expectedStudent))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedStudent.getId());
    }

    @Test
    @Override
    public void testGivenRecordIdAndRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        Student expectedStudent = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When & Then
        Assertions.assertThatThrownBy(() -> studentService.updateRecord(expectedStudent.getId(), expectedStudent))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedStudent.getId());
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() {
        // Given
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student savedStudent = studentService.insertRecord(student).orElseGet(Student::new);

        // When
        Boolean flag = studentService.deleteRecordById(savedStudent.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Boolean flag = studentService.deleteRecordById(id);

        // Then
        Assertions.assertThat(flag).isFalse();
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given & When
        studentService.deleteAllRecords();
        List<Student> actualStudents = studentService.getAllRecords();

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isEmpty();
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