package com.spring.crud.demo.controller.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.controller.BaseControllerTest;
import com.spring.crud.demo.dto.ResponseDTO;
import com.spring.crud.demo.dto.emp.EmployeeDTO;
import com.spring.crud.demo.dto.emp.PhoneNumberDTO;
import com.spring.crud.demo.model.emp.Employee;
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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeControllerIT implements BaseControllerTest<EmployeeDTO, EmployeeDTO> {

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
        file = FileLoader.getFileFromResource("employees.json");
    }

    @BeforeEach
    public void setUp() {
        url = String.format("http://localhost:%d", port);
        restTemplate.delete(url + "/employees");
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws IOException {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        employees.forEach(s -> restTemplate.postForEntity(url + "/employees", s, EmployeeDTO.class));
        Tuple[] expectedEmployees = employees.stream()
                .map(employee -> AssertionsForClassTypes.tuple(
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getAge(),
                        employee.getNoOfChildrens(),
                        employee.getSpouse(),
                        employee.getDateOfJoining(),
                        employee.getHobbies().toArray(),
                        employee.getAddress().getStreetAddress(),
                        employee.getAddress().getCity(),
                        employee.getAddress().getState(),
                        employee.getAddress().getCountry(),
                        employee.getAddress().getPostalCode(),
                        employee.getPhoneNumbers().stream().map(PhoneNumberDTO::getType).toArray(),
                        employee.getPhoneNumbers().stream().map(PhoneNumberDTO::getNumber).toArray()
                ))
                .toArray(Tuple[]::new);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<List<EmployeeDTO>> responseEntity = restTemplate.exchange(url + "/employees", HttpMethod.GET, entity, new ParameterizedTypeReference<List<EmployeeDTO>>() {
        });

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().size()).isGreaterThan(0);
        Assertions.assertThat(responseEntity.getBody())
                .extracting(EmployeeDTO::getFirstName,
                        EmployeeDTO::getLastName,
                        EmployeeDTO::getAge,
                        EmployeeDTO::getNoOfChildrens,
                        EmployeeDTO::getSpouse,
                        EmployeeDTO::getDateOfJoining,
                        employee -> employee.getHobbies().toArray(),
                        employee -> employee.getAddress().getStreetAddress(),
                        employee -> employee.getAddress().getCity(),
                        employee -> employee.getAddress().getState(),
                        employee -> employee.getAddress().getCountry(),
                        employee -> employee.getAddress().getPostalCode(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumberDTO::getType).toArray(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumberDTO::getNumber).toArray()
                )
                .containsExactly(expectedEmployees);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenThrowException() {
        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/employees", HttpMethod.GET, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("No record found");
    }

    @Test
    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws IOException {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO saveEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO expectedEmployee = restTemplate.postForEntity(url + "/employees", saveEmployee, EmployeeDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<EmployeeDTO> responseEntity = restTemplate.exchange(url + "/employees/" + expectedEmployee.getId(), HttpMethod.GET, entity, EmployeeDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getId()).isEqualTo(expectedEmployee.getId());
        Assertions.assertThat(responseEntity.getBody().getAddress().getId()).isEqualTo(expectedEmployee.getAddress().getId());
        Assertions.assertThat(responseEntity.getBody().getPhoneNumbers().get(0).getId()).isEqualTo(expectedEmployee.getPhoneNumbers().get(0).getId());
        assertRecord(expectedEmployee, responseEntity.getBody());
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
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/employees/" + id, HttpMethod.GET, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("No record found with id " + id);
    }

    @Test
    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws IOException {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO saveEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO expectedEmployee = restTemplate.postForEntity(url + "/employees", saveEmployee, EmployeeDTO.class).getBody();
        EmployeeDTO searchEmployee = objectMapper.convertValue(expectedEmployee, EmployeeDTO.class);
        searchEmployee.setAddress(null);
        searchEmployee.setPhoneNumbers(null);
        Map<String, Object> map = new ObjectMapper().convertValue(searchEmployee, Map.class);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> entity = new HttpEntity<Map>(map, headers);
        ResponseEntity<List<EmployeeDTO>> responseEntity = restTemplate.exchange(url + "/employees/search", HttpMethod.POST, entity, new ParameterizedTypeReference<List<EmployeeDTO>>() {
        });

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().size()).isGreaterThan(0);
        Assertions.assertThat(responseEntity.getBody().get(0).getId()).isEqualTo(expectedEmployee.getId());
        assertRecord(expectedEmployee, responseEntity.getBody().get(0));
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenThrowException() throws JsonProcessingException {
        // Given
        Employee expectedEmployee = new Employee();
        expectedEmployee.setFirstName("Rahul");
        expectedEmployee.setLastName("Ghadage");
        expectedEmployee.setNoOfChildrens(0);
        expectedEmployee.setAge(28);
        expectedEmployee.setSpouse(true);
        expectedEmployee.setAddress(null);
        expectedEmployee.setPhoneNumbers(null);
        EmployeeDTO map = objectMapper.convertValue(expectedEmployee, EmployeeDTO.class);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeDTO> entity = new HttpEntity<EmployeeDTO>(map, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/employees/search", HttpMethod.POST, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("No record found with map " + objectMapper.writeValueAsString(map));
    }

    @Test
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() throws IOException {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeDTO> entity = new HttpEntity<EmployeeDTO>(expectedEmployee, headers);
        ResponseEntity<EmployeeDTO> responseEntity = restTemplate.exchange(url + "/employees", HttpMethod.POST, entity, EmployeeDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        assertRecord(expectedEmployee, responseEntity.getBody());
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() throws IOException {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO saveEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO expectedEmployee = restTemplate.postForEntity(url + "/employees", saveEmployee, EmployeeDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeDTO> entity = new HttpEntity<EmployeeDTO>(expectedEmployee, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/employees", HttpMethod.POST, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(HttpStatus.FOUND.value());
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("Record already found with id " + expectedEmployee.getId());
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO saveEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO expectedEmployee = restTemplate.postForEntity(url + "/employees", saveEmployee, EmployeeDTO.class).getBody();

        // When
        expectedEmployee.setAge(45);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeDTO> entity = new HttpEntity<EmployeeDTO>(expectedEmployee, headers);
        ResponseEntity<EmployeeDTO> responseEntity = restTemplate.exchange(url + "/employees/" + expectedEmployee.getId(), HttpMethod.PUT, entity, EmployeeDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getId()).isEqualTo(expectedEmployee.getId());
        Assertions.assertThat(responseEntity.getBody().getAddress().getId()).isEqualTo(expectedEmployee.getAddress().getId());
        Assertions.assertThat(responseEntity.getBody().getPhoneNumbers().get(0).getId()).isEqualTo(expectedEmployee.getPhoneNumbers().get(0).getId());
        assertRecord(expectedEmployee, responseEntity.getBody());
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(1);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeDTO> entity = new HttpEntity<EmployeeDTO>(employee, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/employees/" + id, HttpMethod.PUT, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(500);
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("Update Record id: " + id + " not equal to payload id: " + employee.getId());
    }

    @Test
    void testGivenRandomIdAndRandomRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(1);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeDTO> entity = new HttpEntity<EmployeeDTO>(employee, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/employees/" + employee.getId(), HttpMethod.PUT, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("No record found with id " + employee.getId());
    }

    @Test
    void testGivenRandomIdAndExistingRecordWithoutId_WhenUpdateRecord_ThenThrowException() throws IOException {
        // Given
        int id = RandomUtils.nextInt();
        List<EmployeeDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO expectedEmployee = students.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeDTO> entity = new HttpEntity<EmployeeDTO>(expectedEmployee, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/employees/" + id, HttpMethod.PUT, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("Payload record id is null");
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() throws IOException {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO savedEmployee = restTemplate.postForEntity(url + "/employees", employee, EmployeeDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/employees/" + savedEmployee.getId(), HttpMethod.DELETE, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(HttpStatus.ACCEPTED.value());
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("Record deleted with id " + savedEmployee.getId());
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
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/employees/" + id, HttpMethod.DELETE, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("No record found with id " + id);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given & When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/employees", HttpMethod.DELETE, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    public void assertRecord(EmployeeDTO actualRecord, EmployeeDTO expectedRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getFirstName()).isEqualTo(expectedRecord.getFirstName());
        Assertions.assertThat(actualRecord.getLastName()).isEqualTo(expectedRecord.getLastName());
        Assertions.assertThat(actualRecord.getAge()).isEqualTo(expectedRecord.getAge());
        Assertions.assertThat(actualRecord.getNoOfChildrens()).isEqualTo(expectedRecord.getNoOfChildrens());
        Assertions.assertThat(actualRecord.getSpouse()).isEqualTo(expectedRecord.getSpouse());
        Assertions.assertThat(actualRecord.getDateOfJoining()).isEqualTo(expectedRecord.getDateOfJoining());
        Assertions.assertThat(actualRecord.getHobbies().toArray()).isEqualTo(expectedRecord.getHobbies().toArray());
        Assertions.assertThat(actualRecord.getPhoneNumbers().stream().map(PhoneNumberDTO::getType).toArray()).isEqualTo(expectedRecord.getPhoneNumbers().stream().map(PhoneNumberDTO::getType).toArray());
        Assertions.assertThat(actualRecord.getPhoneNumbers().stream().map(PhoneNumberDTO::getNumber).toArray()).isEqualTo(expectedRecord.getPhoneNumbers().stream().map(PhoneNumberDTO::getNumber).toArray());
        Assertions.assertThat(actualRecord.getAddress().getStreetAddress()).isEqualTo(expectedRecord.getAddress().getStreetAddress());
        Assertions.assertThat(actualRecord.getAddress().getCity()).isEqualTo(expectedRecord.getAddress().getCity());
        Assertions.assertThat(actualRecord.getAddress().getState()).isEqualTo(expectedRecord.getAddress().getState());
        Assertions.assertThat(actualRecord.getAddress().getCountry()).isEqualTo(expectedRecord.getAddress().getCountry());
        Assertions.assertThat(actualRecord.getAddress().getPostalCode()).isEqualTo(expectedRecord.getAddress().getPostalCode());
    }
}
