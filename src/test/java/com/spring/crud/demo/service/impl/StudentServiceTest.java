package com.spring.crud.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.repository.StudentRepository;
import com.spring.crud.demo.utils.FileLoader;
import com.spring.crud.demo.utils.HelperUtil;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @InjectMocks
    private StudentService studentService;
    private static Tuple[] expectedStudents = null;
    private static List<Student> students;

    @BeforeAll
    static void init() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        File file = FileLoader.getFileFromResource("students.json");
        students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        expectedStudents = students.stream()
                .map(student -> AssertionsForClassTypes.tuple(
                        student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()
                )).toArray(Tuple[]::new);
    }

    @Test
    void testGivenNon_WhenFindAllStudents_ThenReturnAllRecord() {
        // Given

        // When
        Mockito.when(studentRepository.findAll()).thenReturn(HelperUtil.studentSupplier.get());
        List<Student> students = studentService.findAllStudents();

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
        Mockito.verify(studentRepository).findAll();
    }

    @Test
    void testGivenId_WhenFindStudentById_ThenReturnRecord() {
        // Given
        int id = 12;
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        // When
        Mockito.when(studentRepository.findById(id)).thenReturn(Optional.of(expectedStudent));
        Student actualStudent = studentService.findStudentById(id).orElseGet(Student::new);

        // Then
        assertStudent(expectedStudent, actualStudent);
        Mockito.verify(studentRepository).findById(id);
    }

    @Test
    void testGivenId_WhenFindStudentByRollNo_ThenReturnRecord() {
        // Given
        int rollNo = 12;
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        // When
        Mockito.when(studentRepository.findByRollNo(rollNo)).thenReturn(Optional.of(expectedStudent));
        Student actualStudent = studentService.findStudentByRollNo(rollNo).orElseGet(Student::new);

        // Then
        assertStudent(expectedStudent, actualStudent);
        Mockito.verify(studentRepository).findByRollNo(rollNo);
    }

    @Test
    void testGivenRandomId_WhenFindStudentById_ThenReturnRecord() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(studentRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> studentService.findStudentById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);

    }

    @Test
    void testGivenStudent_WhenFindStudentsByExample_ThenReturnRecords() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        // When
        Mockito.when(studentRepository.findAll((Example) Mockito.any())).thenReturn(List.of(expectedStudent));
        List<Student> actualStudents = studentService.findStudentsByExample(expectedStudent);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        Assertions.assertThat(actualStudents.get(0)).isEqualTo(expectedStudent);
    }

    @Test
    void testGivenRandomStudent_WhenFindStudentByExample_ThenReturnRecords() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        List<Student> students = new ArrayList<>();

        // When
        Mockito.when(studentRepository.findAll((Example) Mockito.any())).thenReturn(students);
        List<Student> actualStudents = studentService.findStudentsByExample(expectedStudent);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(0);
    }

    @Test
    void testGivenStudent_WhenSaveStudent_ThenReturnNewStudent() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        // When
        Mockito.when(studentRepository.save(expectedStudent)).thenReturn(expectedStudent);
        Student actualStudent = studentService.saveStudent(expectedStudent).orElseGet(Student::new);

        // Then
        assertStudent(expectedStudent, actualStudent);
        Mockito.verify(studentRepository).save(expectedStudent);
    }

    @Test
    void testGivenExistingStudent_WhenSaveStudent_ThenThrowError() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When
        Mockito.when(studentRepository.existsById(expectedStudent.getId())).thenReturn(true);
        Assertions.assertThatThrownBy(() -> studentService.saveStudent(expectedStudent))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedStudent.getId());

        // Then
        Mockito.verify(studentRepository).existsById(expectedStudent.getId());
    }

    @Test
    void testGivenExistingStudent_WhenUpdateStudent_ThenReturnUpdatedStudent() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When
        Mockito.when(studentRepository.existsById(expectedStudent.getId())).thenReturn(true);
        Mockito.when(studentRepository.save(expectedStudent)).thenReturn(expectedStudent);
        Student actualStudent = studentService.updateStudent(expectedStudent.getId(), expectedStudent).orElseGet(Student::new);

        // Then
        assertStudent(expectedStudent, actualStudent);
        Mockito.verify(studentRepository).existsById(expectedStudent.getId());
        Mockito.verify(studentRepository).save(expectedStudent);
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
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When & Then
        Assertions.assertThatThrownBy(() -> studentService.updateStudent(id, expectedStudent))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedStudent.getId());
    }

    @Test
    void testGivenStudentAndId_WhenUpdateStudent_ThenThrowError() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When
        Mockito.when(studentRepository.existsById(expectedStudent.getId())).thenReturn(false);

        // Then
        Assertions.assertThatThrownBy(() -> studentService.updateStudent(expectedStudent.getId(), expectedStudent))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedStudent.getId());
        Mockito.verify(studentRepository).existsById(expectedStudent.getId());
    }

    @Test
    void testGiveId_WhenDeleteStudent_ThenReturnTrue() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When
        Mockito.when(studentRepository.existsById(expectedStudent.getId())).thenReturn(true);
        Boolean flag = studentService.deleteStudent(expectedStudent.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
        Mockito.verify(studentRepository).existsById(expectedStudent.getId());
    }

    @Test
    void testGiveRandomId_WhenDeleteStudent_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(studentRepository.existsById(id)).thenReturn(false);
        Boolean flag = studentService.deleteStudent(id);

        // Then
        Assertions.assertThat(flag).isFalse();
        Mockito.verify(studentRepository).existsById(id);
    }

    @Test
    void testGiveNon_WhenDeleteAllStudent_ThenReturnNon() {
        // Given

        // When
        Mockito.doNothing().when(studentRepository).deleteAll();
        studentService.deleteAllStudent();

        // Then
        Mockito.verify(studentRepository).deleteAll();
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