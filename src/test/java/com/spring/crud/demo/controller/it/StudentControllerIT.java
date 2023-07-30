package com.spring.crud.demo.controller.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.controller.BaseControllerTest;
import com.spring.crud.demo.dto.ResponseDTO;
import com.spring.crud.demo.dto.StudentDTO;
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
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerIT implements BaseControllerTest<StudentDTO, StudentDTO> {

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
        restTemplate.delete(url + "/students");
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws IOException {
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
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenThrowException() {
        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/students", HttpMethod.GET, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found");
    }

    @Test
    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO insertRecord = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO expectedStudent = restTemplate.postForEntity(url + "/students", insertRecord, StudentDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<StudentDTO> responseEntity = restTemplate.exchange(url + "/students/" + expectedStudent.getId(), HttpMethod.GET, entity, StudentDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getId()).isEqualTo(expectedStudent.getId());
        assertRecord(expectedStudent, responseEntity.getBody());
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/students/" + id, HttpMethod.GET, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with id " + id);
    }

    @Test
    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO insertRecord = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO expectedStudent = restTemplate.postForEntity(url + "/students", insertRecord, StudentDTO.class).getBody();
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
        assertRecord(expectedStudent, responseEntity.getBody().get(0));
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenThrowException() {
        // Given
        Student expectedStudent = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);
        StudentDTO map = objectMapper.convertValue(expectedStudent, StudentDTO.class);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> entity = new HttpEntity<StudentDTO>(map, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/students/search", HttpMethod.POST, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with map " + map);
    }

    @Test
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() throws IOException {
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
        assertRecord(expectedStudent, responseEntity.getBody());
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO insertRecord = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO expectedStudent = restTemplate.postForEntity(url + "/students", insertRecord, StudentDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> entity = new HttpEntity<StudentDTO>(expectedStudent, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/students", HttpMethod.POST, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.FOUND.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Record already found with id " + expectedStudent.getId());
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO insertRecord = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO expectedStudent = restTemplate.postForEntity(url + "/students", insertRecord, StudentDTO.class).getBody();

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
        assertRecord(expectedStudent, responseEntity.getBody());
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();
        StudentDTO student = new StudentDTO();
        student.setId(1);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> entity = new HttpEntity<StudentDTO>(student, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/students/" + id, HttpMethod.PUT, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Update Record id: " + id + " not equal to payload id: " + student.getId());
    }

    @Test
    void testGivenRandomIdAndRandomRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        StudentDTO student = new StudentDTO();
        student.setId(1);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> entity = new HttpEntity<StudentDTO>(student, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/students/" + student.getId(), HttpMethod.PUT, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with id " + student.getId());
    }


    @Test
    void testGivenRandomIdAndExistingRecordWithoutId_WhenUpdateRecord_ThenThrowException() throws IOException {
        // Given
        int id = RandomUtils.nextInt();
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> entity = new HttpEntity<StudentDTO>(expectedStudent, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/students/" + id, HttpMethod.PUT, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Payload record id is null");
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() throws IOException {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO savedStudent = restTemplate.postForEntity(url + "/students", student, StudentDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/students/" + savedStudent.getId(), HttpMethod.DELETE, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Record deleted with id " + savedStudent.getId());
    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/students/" + id, HttpMethod.DELETE, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with id " + id);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given & When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/students", HttpMethod.DELETE, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    public void assertRecord(StudentDTO expectedRecord, StudentDTO actualRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getRollNo()).isEqualTo(expectedRecord.getRollNo());
        Assertions.assertThat(actualRecord.getFirstName()).isEqualTo(expectedRecord.getFirstName());
        Assertions.assertThat(actualRecord.getLastName()).isEqualTo(expectedRecord.getLastName());
        Assertions.assertThat(actualRecord.getDateOfBirth()).isEqualTo(expectedRecord.getDateOfBirth());
        Assertions.assertThat(actualRecord.getMarks()).isEqualTo(expectedRecord.getMarks());
    }
}
