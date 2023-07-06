package com.spring.crud.demo.controller.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.dto.ErrorResponseDTO;
import com.spring.crud.demo.dto.StudentDTO;
import com.spring.crud.demo.utils.FileLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerIT {

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
        restTemplate.delete(url + "/students" );
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
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
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
        Assertions.assertThat(responseEntity.getBody().getId()).isEqualTo(expectedStudent.getId());
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
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/students/" + id, HttpMethod.GET, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with id " + id);
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
        HttpEntity<Map> entity = new HttpEntity<Map>(map, headers);
        ResponseEntity<List<StudentDTO>> responseEntity = restTemplate.exchange(url + "/students/search", HttpMethod.POST, entity, new ParameterizedTypeReference<List<StudentDTO>>() {
        });

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().size()).isGreaterThan(0);
        Assertions.assertThat(responseEntity.getBody().get(0).getId()).isEqualTo(expectedStudent.getId());
        assertStudent(expectedStudent, responseEntity.getBody().get(0));
    }

    @Test
    void testGivenRandomStudent_WhenFindStudentsByExample_ThenReturnError() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO saveStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        Map<String, Object> map = objectMapper.convertValue(saveStudent, Map.class);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> entity = new HttpEntity<Map>(map, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/students/search", HttpMethod.POST, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with map " + map);
    }

    @Test
    void testGivenStudent_WhenSaveStudent_ThenReturnNewStudent() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> entity = new HttpEntity<StudentDTO>(expectedStudent, headers);
        ResponseEntity<StudentDTO> responseEntity = restTemplate.exchange(url + "/students", HttpMethod.POST, entity, StudentDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        assertStudent(expectedStudent, responseEntity.getBody());
    }

    @Test
    void testGivenSavedStudent_WhenSaveStudent_ThenReturnError() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO saveStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO expectedStudent = restTemplate.postForEntity(url + "/students", saveStudent, StudentDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> entity = new HttpEntity<StudentDTO>(expectedStudent, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/students", HttpMethod.POST, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.FOUND.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Record already found with id " + expectedStudent.getId());
    }

    @Test
    void testGivenExistingStudent_WhenUpdateStudent_ThenReturnUpdatedStudent() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO saveStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO expectedStudent = restTemplate.postForEntity(url + "/students", saveStudent, StudentDTO.class).getBody();

        // When
        expectedStudent.setMarks(800.f);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> entity = new HttpEntity<StudentDTO>(expectedStudent, headers);
        ResponseEntity<StudentDTO> responseEntity = restTemplate.exchange(url + "/students/" + expectedStudent.getId(), HttpMethod.PUT, entity, StudentDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getId()).isEqualTo(expectedStudent.getId());
        assertStudent(expectedStudent, responseEntity.getBody());
    }

    @Test
    void testGivenNull_WhenUpdateStudent_ThenThrowError() throws IOException {
        // Given
        StudentDTO student = new StudentDTO();
        student.setId(1);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> entity = new HttpEntity<StudentDTO>(student, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/students/" + student.getId(), HttpMethod.PUT, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with id " + student.getId());
    }

    @Test
    void testGivenNull_WhenUpdateStudent_ThenThrowError1() throws IOException {
        // Given
        int id = RandomUtils.nextInt();
        StudentDTO student = new StudentDTO();
        student.setId(1);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> entity = new HttpEntity<StudentDTO>(student, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/students/" + id, HttpMethod.PUT, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Update Record id: " + id + " not equal to payload id: " + student.getId());
    }

    @Test
    void testGivenNull_WhenUpdateStudent_ThenThrowError2() throws IOException {
        // Given
        int id = RandomUtils.nextInt();
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> entity = new HttpEntity<StudentDTO>(expectedStudent, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/students/"+id, HttpMethod.PUT, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Payload record id is null");
    }

    @Test
    void testGiveId_WhenDeleteStudent_ThenReturnTrue() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO savedStudent = restTemplate.postForEntity(url + "/students", student, StudentDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(url + "/students/" + savedStudent.getId(), HttpMethod.DELETE, entity, Boolean.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody()).isTrue();
    }

    @Test
    void testGiveRandomId_WhenDeleteStudent_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(url + "/students/" + id, HttpMethod.DELETE, entity, Boolean.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody()).isFalse();
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
