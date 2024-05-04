package com.spring.crud.demo.controller.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.controller.BaseControllerTest;
import com.spring.crud.demo.controller.StudentController;
import com.spring.crud.demo.dto.ResponseDTO;
import com.spring.crud.demo.dto.StudentDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.mapper.StudentMapper;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.service.StudentService;
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
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest implements BaseControllerTest<Student, StudentDTO> {

    private static File file;
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;
    private static StudentMapper studentMapper;
    private static StudentService studentService;
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
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws IOException {
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
        Mockito.when(studentService.getAllRecords()).thenReturn(students);
        students.forEach(student -> Mockito.when(studentMapper.convertFromEntityToDto(student)).thenReturn(objectMapper.convertValue(student, StudentDTO.class)));
        ResponseEntity<List<StudentDTO>> actualStudents = studentController.getAllRecords();

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
        Mockito.verify(studentService, Mockito.atLeastOnce()).getAllRecords();
        students.forEach(student -> Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromEntityToDto(student));
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenThrowException() {
        // Given
        List<Student> studentList = new ArrayList<>();

        // When & Then
        Mockito.when(studentService.getAllRecords()).thenReturn(studentList);
        Assertions.assertThatThrownBy(() -> studentController.getAllRecords())
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found");
        Mockito.verify(studentService, Mockito.atLeastOnce()).getAllRecords();
    }

    @Test
    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws IOException {
        // Given
        int id = 12;
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        // When
        Mockito.when(studentService.getRecordsById(id)).thenReturn(Optional.of(expectedStudent));
        Mockito.when(studentMapper.convertFromEntityToDto(expectedStudent)).thenReturn(objectMapper.convertValue(expectedStudent, StudentDTO.class));
        ResponseEntity<StudentDTO> actualStudent = studentController.getRecordsById(id);

        // Then
        Assertions.assertThat(actualStudent.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualStudent.getBody()).isNotNull();
        assertRecord(expectedStudent, actualStudent.getBody());
        Mockito.verify(studentService).getRecordsById(id);
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromEntityToDto(expectedStudent);
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(studentService.getRecordsById(id)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> studentController.getRecordsById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);
    }

    /*
    @Test@Override
    void testGivenId_WhenFindStudentByRollNo_ThenReturnRecord() throws IOException {
        // Given
        int rollNo = 12;
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        // When
        Mockito.when(studentService.findStudentByRollNo(rollNo)).thenReturn(Optional.of(expectedStudent));
        Mockito.when(studentMapper.convertFromEntityToDto(expectedStudent)).thenReturn(objectMapper.convertValue(expectedStudent, StudentDTO.class));
        ResponseEntity<StudentDTO> actualStudent = studentController.findStudentByRollNo(rollNo);

        // Then
        Assertions.assertThat(actualStudent.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualStudent.getBody()).isNotNull();
        assertStudent(expectedStudent, actualStudent.getBody());
        Mockito.verify(studentService).findStudentByRollNo(rollNo);
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromEntityToDto(expectedStudent);
    }

    @Test@Override
    void testGivenRandomId_WhenFindStudentByRollNo_ThenReturnRecord() {
        // Given
        int rollNo = RandomUtils.nextInt();

        // When & Then
        Mockito.when(studentService.findStudentByRollNo(rollNo)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> studentController.findStudentByRollNo(rollNo))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with rollNo " + rollNo);
    }
    */

    @Test
    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        StudentDTO map = new ObjectMapper().convertValue(expectedStudent, StudentDTO.class);

        // When
        Mockito.when(studentService.getAllRecordsByExample(expectedStudent)).thenReturn(List.of(expectedStudent));
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedStudent);
        Mockito.when(studentMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(map, StudentDTO.class));
        ResponseEntity<List<StudentDTO>> actualStudents = studentController.getAllRecordsByExample(map);

        // Then
        Assertions.assertThat(actualStudents.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualStudents.getBody()).isNotNull();
        Assertions.assertThat(actualStudents.getBody().size()).isGreaterThan(0);
        assertRecord(expectedStudent, actualStudents.getBody().get(0));
        Mockito.verify(studentService).getAllRecordsByExample(expectedStudent);
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenThrowException() throws JsonProcessingException {
        // Given
        Student expectedStudent = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);
        StudentDTO map = objectMapper.convertValue(expectedStudent, StudentDTO.class);
        List<Student> students = new ArrayList<>();

        // When & Then
        Mockito.when(studentService.getAllRecordsByExample(expectedStudent)).thenReturn(students);
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedStudent);
        Assertions.assertThatThrownBy(() -> studentController.getAllRecordsByExample(map))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with map " + objectMapper.writeValueAsString(map));


        Mockito.verify(studentService).getAllRecordsByExample(expectedStudent);
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        // When
        Mockito.when(studentService.insertRecord(expectedStudent)).thenReturn(Optional.of(expectedStudent));
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedStudent);
        Mockito.when(studentMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(expectedStudent, StudentDTO.class));
        ResponseEntity<StudentDTO> actualStudent = studentController.insertRecord(objectMapper.convertValue(expectedStudent, StudentDTO.class));

        // Then
        Assertions.assertThat(actualStudent.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(actualStudent.getBody()).isNotNull();
        assertRecord(expectedStudent, actualStudent.getBody());
        Mockito.verify(studentService).insertRecord(expectedStudent);
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(15);

        // When
        Mockito.when(studentService.insertRecord(expectedStudent)).thenReturn(Optional.empty());
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedStudent);
        Assertions.assertThatThrownBy(() -> studentController.insertRecord(objectMapper.convertValue(expectedStudent, StudentDTO.class)))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Something went wrong");

        // Then
        Mockito.verify(studentService).insertRecord(expectedStudent);
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(15);

        // When
        Mockito.when(studentService.updateRecord(expectedStudent.getId(), expectedStudent)).thenReturn(Optional.of(expectedStudent));
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedStudent);
        Mockito.when(studentMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(expectedStudent, StudentDTO.class));
        ResponseEntity<StudentDTO> actualStudent = studentController.updateRecord(expectedStudent.getId(), objectMapper.convertValue(expectedStudent, StudentDTO.class));

        // Then
        Assertions.assertThat(actualStudent.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(actualStudent.getBody()).isNotNull();
        assertRecord(expectedStudent, actualStudent.getBody());
        Mockito.verify(studentService).updateRecord(expectedStudent.getId(), expectedStudent);
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(studentService.updateRecord(id, null)).thenReturn(Optional.empty());
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(null);
        Assertions.assertThatThrownBy(() -> studentController.updateRecord(id, null))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Something went wrong");
        Mockito.verify(studentService).updateRecord(id, null);
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() throws IOException {
        // Given
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(15);

        // When
        Mockito.when(studentService.deleteRecordById(expectedStudent.getId())).thenReturn(true);
        ResponseEntity<ResponseDTO> response = studentController.deleteRecordById(expectedStudent.getId());

        // Then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().message()).isEqualTo("Record deleted with id " + expectedStudent.getId());
        Mockito.verify(studentService).deleteRecordById(expectedStudent.getId());
    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(studentService.deleteRecordById(id)).thenReturn(false);
        Assertions.assertThatThrownBy(() -> studentController.deleteRecordById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);

        // Then
        Mockito.verify(studentService).deleteRecordById(id);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given & When
        Mockito.doNothing().when(studentService).deleteAllRecords();

        // Then
        ResponseEntity<Void> response = studentController.deleteAllRecords();

        // Then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    public void assertRecord(Student expectedRecord, StudentDTO actualRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getRollNo()).isEqualTo(expectedRecord.getRollNo());
        Assertions.assertThat(actualRecord.getFirstName()).isEqualTo(expectedRecord.getFirstName());
        Assertions.assertThat(actualRecord.getLastName()).isEqualTo(expectedRecord.getLastName());
        Assertions.assertThat(actualRecord.getDateOfBirth()).isEqualTo(expectedRecord.getDateOfBirth().format(DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)));
        Assertions.assertThat(actualRecord.getMarks()).isEqualTo(expectedRecord.getMarks());
    }
}