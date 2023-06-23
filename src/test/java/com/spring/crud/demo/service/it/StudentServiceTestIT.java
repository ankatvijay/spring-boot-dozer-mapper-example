package com.spring.crud.demo.service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.service.IStudentService;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootTest(value = "StudentServiceTestIT")
class StudentServiceTestIT {

    @Autowired
    private IStudentService studentService;
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
    void init(){
        studentService.deleteAllStudent();
    }

    @Test
    void testGivenNon_WhenFindAllStudents_ThenReturnAllRecord() {
        // Given
        studentes.forEach(student -> studentService.saveStudent(student));

        // When
        List<Student> actualStudents = studentService.findAllStudents();

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
    void testGivenId_WhenFindStudentById_ThenReturnRecord() {
        // Given
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentService.saveStudent(student).orElseGet(Student::new);

        // When
        Student actualStudent = studentService.findStudentById(expectedStudent.getId()).orElseGet(Student::new);

        // Then
        assertStudent(expectedStudent, actualStudent);
    }

    @Test
    void testGivenRandomId_WhenFindStudentById_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Assertions.assertThatThrownBy(() -> studentService.findStudentById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);

        // Then
    }

    @Test
    void testGivenStudent_WhenFindStudentsByExample_ThenReturnRecords() {
        // Given
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentService.saveStudent(student).orElseGet(Student::new);

        // When
        List<Student> actualStudents = studentService.findStudentsByExample(expectedStudent);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        assertStudent(actualStudents.get(0), expectedStudent);
    }

    @Test
    void testGivenRandomStudent_WhenFindStudentsByExample_ThenReturnRecords() {
        // Given
        Student expectedStudent = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);

        // When
        List<Student> actualStudents = studentService.findStudentsByExample(expectedStudent);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(0);
    }

    @Test
    void testGivenStudent_WhenSaveStudent_ThenReturnNewStudent() {
        // Given
        Student expectedStudent = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);

        // When
        Student actualStudent = studentService.saveStudent(expectedStudent).orElseGet(Student::new);

        // Then
        assertStudent(expectedStudent, actualStudent);
    }

    @Test
    void testGivenExistingStudent_WhenSaveStudent_ThenThrowError() {
        // Given
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student expectedStudent = studentService.saveStudent(student).orElseGet(Student::new);

        // When
        Assertions.assertThatThrownBy(() -> studentService.saveStudent(expectedStudent))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedStudent.getId());

        // Then
    }

    @Test
    void testGivenExistingStudent_WhenUpdateStudent_ThenReturnUpdatedStudent() {
        // Given
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student savedStudent = studentService.saveStudent(student).orElseGet(Student::new);

        // When
        Student expectedStudent = studentService.findStudentById(savedStudent.getId()).orElseGet(Student::new);
        expectedStudent.setMarks(999.0f);
        Student actualStudent = studentService.updateStudent(savedStudent.getId(),expectedStudent).orElseGet(Student::new);

        // Then
        assertStudent(expectedStudent, actualStudent);
    }

    @Test
    void testGivenNull_WhenUpdateStudent_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Assertions.assertThatThrownBy(() -> studentService.updateStudent(id, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Payload record id is null");
    }

    @Test
    void testGivenStudentAndIdDifferent_WhenUpdateStudent_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student savedStudent = studentService.saveStudent(student).orElseGet(Student::new);

        // When & Then
        Student expectedStudent = studentService.findStudentById(savedStudent.getId()).orElseGet(Student::new);
        expectedStudent.setMarks(999.0f);
        Assertions.assertThatThrownBy(() -> studentService.updateStudent(id, expectedStudent))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedStudent.getId());
    }

    @Test
    void testGivenStudentAndId_WhenUpdateStudent_ThenThrowError() {
        // Given
        Student expectedStudent = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When & Then
        Assertions.assertThatThrownBy(() -> studentService.updateStudent(expectedStudent.getId(), expectedStudent))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedStudent.getId());
    }

    @Test
    void testGiveId_WhenDeleteStudent_ThenReturnTrue() {
        // Given
        Student student = studentes.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Student savedStudent = studentService.saveStudent(student).orElseGet(Student::new);

        // When
        Boolean flag = studentService.deleteStudent(savedStudent.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
    }

    @Test
    void testGiveRandomId_WhenDeleteStudent_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Boolean flag = studentService.deleteStudent(id);

        // Then
        Assertions.assertThat(flag).isFalse();
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