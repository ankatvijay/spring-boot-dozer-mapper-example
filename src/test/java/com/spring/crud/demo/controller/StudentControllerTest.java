package com.spring.crud.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.dto.StudentDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.mapper.StudentMapper;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.service.IStudentService;
import com.spring.crud.demo.service.impl.StudentService;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    private static File file;
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;
    private static StudentMapper studentMapper;
    private static IStudentService studentService;
    private static StudentController studentController;

    @BeforeAll
    static void init() {
        objectMapper = new ObjectMapper();
        typeFactory = objectMapper.getTypeFactory();
        file = FileLoader.getFileFromResource("students.json");

        studentMapper = Mockito.mock(StudentMapper.class);
        studentService = Mockito.mock(StudentService.class);
        studentController = new StudentController(studentService, studentMapper, objectMapper);
    }

    @Test
    void testGivenNon_WhenFindAllStudents_ThenReturnAllRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Tuple[] expectedStudents = students.stream()
                .map(student -> AssertionsForClassTypes.tuple(student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth().format(DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)),
                        student.getMarks()))
                .toArray(Tuple[]::new);

        // When
        Mockito.when(studentService.findAllStudents()).thenReturn(students);
        students.forEach(student -> Mockito.when(studentMapper.convertFromEntityToDto(student)).thenReturn(objectMapper.convertValue(student, StudentDTO.class)));
        ResponseEntity<List<StudentDTO>> actualStudents = studentController.findAllStudents();

        // Then
        Assertions.assertThat(actualStudents.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualStudents.getBody()).isNotNull();
        Assertions.assertThat(actualStudents.getBody().size()).isGreaterThan(0);
        Assertions.assertThat(actualStudents.getBody())
                .extracting(StudentDTO::getRollNo,
                        StudentDTO::getFirstName,
                        StudentDTO::getLastName,
                        StudentDTO::getDateOfBirth,
                        StudentDTO::getMarks)
                .containsExactly(expectedStudents);
        Mockito.verify(studentService, Mockito.atLeastOnce()).findAllStudents();
        students.forEach(student -> Mockito.verify(studentMapper).convertFromEntityToDto(student));
    }

    @Test
    void testGivenNon_WhenFindAllStudents_ThenReturnError() {
        // Given
        List<Student> studentList = new ArrayList<>();

        // When & Then
        Mockito.when(studentService.findAllStudents()).thenReturn(studentList);
        Assertions.assertThatThrownBy(() -> studentController.findAllStudents())
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found");
        Mockito.verify(studentService, Mockito.atLeastOnce()).findAllStudents();
    }

    @Test
    void testGivenId_WhenFindStudentById_ThenReturnRecord() throws IOException {
        // Given
        int id = 12;
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        // When
        Mockito.when(studentService.findStudentById(id)).thenReturn(Optional.of(expectedStudent));
        Mockito.when(studentMapper.convertFromEntityToDto(expectedStudent)).thenReturn(objectMapper.convertValue(expectedStudent, StudentDTO.class));
        ResponseEntity<StudentDTO> actualStudent = studentController.findStudentById(id);

        // Then
        Assertions.assertThat(actualStudent.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        Assertions.assertThat(actualStudent.getBody()).isNotNull();
        assertStudent(expectedStudent, actualStudent.getBody());
        Mockito.verify(studentService).findStudentById(id);
        Mockito.verify(studentMapper).convertFromEntityToDto(expectedStudent);
    }

    @Test
    void testGivenRandomId_WhenFindStudentById_ThenReturnRecord() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(studentService.findStudentById(id)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> studentController.findStudentById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);
    }

    @Test
    void testGivenStudent_WhenFindStudentsByExample_ThenReturnRecords() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        Map map = new ObjectMapper().convertValue(expectedStudent, Map.class);

        // When
        Mockito.when(studentService.findStudentsByExample(expectedStudent)).thenReturn(List.of(expectedStudent));
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedStudent);
        Mockito.when(studentMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(map, StudentDTO.class));
        ResponseEntity<List<StudentDTO>> actualStudents = studentController.findStudentsByExample(map);

        // Then
        Assertions.assertThat(actualStudents.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        Assertions.assertThat(actualStudents.getBody()).isNotNull();
        Assertions.assertThat(actualStudents.getBody().size()).isGreaterThan(0);
        assertStudent(expectedStudent, actualStudents.getBody().get(0));
        Mockito.verify(studentService).findStudentsByExample(expectedStudent);
    }

    @Test
    void testGivenRandomStudent_WhenFindStudentsByExample_ThenReturnError() {
        // Given
        Student expectedStudent = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);
        Map map = objectMapper.convertValue(expectedStudent, Map.class);
        List<Student> students = new ArrayList<>();

        // When & Then
        Mockito.when(studentService.findStudentsByExample(expectedStudent)).thenReturn(students);
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedStudent);
        Assertions.assertThatThrownBy(() -> studentController.findStudentsByExample(map))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with map " + map);


        Mockito.verify(studentService).findStudentsByExample(expectedStudent);
        //Mockito.verify(studentMapper).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    void testGivenStudent_WhenSaveStudent_ThenReturnNewStudent() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        // When
        Mockito.when(studentService.saveStudent(expectedStudent)).thenReturn(Optional.of(expectedStudent));
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedStudent);
        Mockito.when(studentMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(expectedStudent, StudentDTO.class));
        ResponseEntity<StudentDTO> actualStudent = studentController.saveStudent(objectMapper.convertValue(expectedStudent, StudentDTO.class));

        // Then
        Assertions.assertThat(actualStudent.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(actualStudent.getBody()).isNotNull();
        assertStudent(expectedStudent, actualStudent.getBody());
        //Mockito.verify(studentMapper).convertFromDtoToEntity(Mockito.any());
        //Mockito.verify(studentMapper).convertFromEntityToDto(Mockito.any());
        Mockito.verify(studentService).saveStudent(expectedStudent);
    }

    @Test
    void testGivenExistingStudent_WhenSaveStudent_ThenThrowError() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(15);

        // When
        Mockito.when(studentService.saveStudent(expectedStudent)).thenReturn(Optional.empty());
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedStudent);
        Assertions.assertThatThrownBy(() -> studentController.saveStudent(objectMapper.convertValue(expectedStudent, StudentDTO.class)))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Something went wrong");

        // Then
        Mockito.verify(studentService).saveStudent(expectedStudent);
    }

    @Test
    void testGivenExistingStudent_WhenUpdateStudent_ThenReturnUpdatedStudent() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(15);

        // When
        Mockito.when(studentService.updateStudent(expectedStudent.getId(), expectedStudent)).thenReturn(Optional.of(expectedStudent));
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedStudent);
        Mockito.when(studentMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(expectedStudent, StudentDTO.class));
        ResponseEntity<StudentDTO> actualStudent = studentController.updateStudent(expectedStudent.getId(), objectMapper.convertValue(expectedStudent, StudentDTO.class));

        // Then
        Assertions.assertThat(actualStudent.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(actualStudent.getBody()).isNotNull();
        assertStudent(expectedStudent, actualStudent.getBody());
        Mockito.verify(studentService).updateStudent(expectedStudent.getId(), expectedStudent);
        //Mockito.verify(studentMapper).convertFromDtoToEntity(Mockito.any());
        //Mockito.verify(studentMapper).convertFromEntityToDto(Mockito.any());
    }

    @Test
    void testGivenNull_WhenUpdateStudent_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(studentService.updateStudent(id, null)).thenReturn(Optional.empty());
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(null);
        Assertions.assertThatThrownBy(() -> studentController.updateStudent(id, null))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Something went wrong");
        Mockito.verify(studentService).updateStudent(id, null);
    }

    @Test
    void testGiveId_WhenDeleteStudent_ThenReturnTrue() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(15);

        // When
        Mockito.when(studentService.deleteStudent(expectedStudent.getId())).thenReturn(true);
        ResponseEntity<Boolean> flag = studentController.deleteStudent(expectedStudent.getId());

        // Then
        Assertions.assertThat(flag.getBody()).isTrue();
        Mockito.verify(studentService).deleteStudent(expectedStudent.getId());
    }

    @Test
    void testGiveRandomId_WhenDeleteStudent_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(studentService.deleteStudent(id)).thenReturn(false);
        ResponseEntity<Boolean> flag = studentController.deleteStudent(id);

        // Then
        Assertions.assertThat(flag.getBody()).isFalse();
        Mockito.verify(studentService).deleteStudent(id);
    }


    private void assertStudent(Student expectedStudent, StudentDTO actualStudent) {
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent.getRollNo()).isEqualTo(expectedStudent.getRollNo());
        Assertions.assertThat(actualStudent.getFirstName()).isEqualTo(expectedStudent.getFirstName());
        Assertions.assertThat(actualStudent.getLastName()).isEqualTo(expectedStudent.getLastName());
        Assertions.assertThat(actualStudent.getDateOfBirth()).isEqualTo(expectedStudent.getDateOfBirth().format(DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)));
        Assertions.assertThat(actualStudent.getMarks()).isEqualTo(expectedStudent.getMarks());
    }
}