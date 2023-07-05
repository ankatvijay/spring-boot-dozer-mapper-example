package com.spring.crud.demo.controller.mockmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.dto.ErrorResponseDTO;
import com.spring.crud.demo.dto.StudentDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerMockMVC {

    @LocalServerPort
    private int port;
    private String url;
    private static File file;
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void init() {
        objectMapper = new ObjectMapper();
        typeFactory = objectMapper.getTypeFactory();
        file = FileLoader.getFileFromResource("students.json");
    }

    @BeforeEach
    public void setUp() {
        url = String.format("http://localhost:%d", port);
    }

    @Test
    void testGivenNon_WhenFindAllStudents_ThenReturnAllRecord() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        students.forEach(s -> restTemplate.postForEntity(url + "/students", s, StudentDTO.class));
        Tuple[] expectedStudents = students.stream()
                .map(student -> AssertionsForClassTypes.tuple(student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()))
                .toArray(Tuple[]::new);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<List<StudentDTO>> responseEntity = restTemplate.exchange(url + "/students", HttpMethod.GET, entity, new ParameterizedTypeReference<List<StudentDTO>>() {
        });

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().size()).isGreaterThan(0);
        Assertions.assertThat(responseEntity.getBody()).extracting(
                        StudentDTO::getRollNo,
                        StudentDTO::getFirstName,
                        StudentDTO::getLastName,
                        StudentDTO::getDateOfBirth,
                        StudentDTO::getMarks)
                .containsExactly(expectedStudents);
    }

    @Test
    void testGivenNon_WhenFindAllStudents_ThenReturnError() {
        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/students", HttpMethod.GET, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found");
    }



    @Test
    void testGivenId_WhenFindStudentById_ThenReturnRecord() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO saveStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO expectedStudent = restTemplate.postForEntity(url + "/students", saveStudent, StudentDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<StudentDTO> responseEntity = restTemplate.exchange(url + "/students/" + expectedStudent.getId(), HttpMethod.GET, entity, StudentDTO.class);


        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        assertStudent(expectedStudent, responseEntity.getBody());
    }

    @Test
    void testGivenRandomId_WhenFindStudentById_ThenReturnRecord() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/students/"+id, HttpMethod.GET, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found");
    }

    @Test
    void testGivenStudent_WhenFindStudentsByExample_ThenReturnRecords() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO saveStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO expectedStudent = restTemplate.postForEntity(url + "/students", saveStudent, StudentDTO.class).getBody();


        Map<String, Object> map = new ObjectMapper().convertValue(expectedStudent, Map.class);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<StudentDTO> responseEntity = restTemplate.exchange(url + "/students/search" + expectedStudent.getId(), HttpMethod.POST, entity, StudentDTO.class);


        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        assertStudent(expectedStudent, responseEntity.getBody());
    }

    @Test
    void testGivenRandomStudent_WhenFindStudentsByExample_ThenReturnError() {
        // Given
        Student expectedStudent = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);
        Map<String, Object> map = objectMapper.convertValue(expectedStudent, Map.class);
        List<Student> students = new ArrayList<>();

        // When & Then
        Mockito.when(studentService.findStudentsByExample(expectedStudent)).thenReturn(students);
        Mockito.when(studentMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedStudent);
        Assertions.assertThatThrownBy(() -> studentController.findStudentsByExample(map))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with map " + map);


        Mockito.verify(studentService).findStudentsByExample(expectedStudent);
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
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
        Mockito.verify(studentService).saveStudent(expectedStudent);
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
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
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
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
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(studentMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
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

    private void assertStudent(StudentDTO expectedStudent, StudentDTO actualStudent) {
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent.getRollNo()).isEqualTo(expectedStudent.getRollNo());
        Assertions.assertThat(actualStudent.getFirstName()).isEqualTo(expectedStudent.getFirstName());
        Assertions.assertThat(actualStudent.getLastName()).isEqualTo(expectedStudent.getLastName());
        Assertions.assertThat(actualStudent.getDateOfBirth()).isEqualTo(expectedStudent.getDateOfBirth());
        Assertions.assertThat(actualStudent.getMarks()).isEqualTo(expectedStudent.getMarks());
    }
}
