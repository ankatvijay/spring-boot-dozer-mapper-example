package com.spring.crud.demo.controller.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.dto.ErrorResponseDTO;
import com.spring.crud.demo.dto.emp.EmployeeDTO;
import com.spring.crud.demo.dto.emp.PhoneNumberDTO;
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
public class EmployeeControllerIT {

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
        restTemplate.delete(url + "/employees" );
    }

    @Test
    void testGivenNon_WhenFindAllEmployees_ThenReturnAllRecord() throws IOException {
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
    void testGivenNon_WhenFindAllEmployees_ThenReturnError() {
        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/employees", HttpMethod.GET, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found");
    }

    @Test
    void testGivenId_WhenFindEmployeeById_ThenReturnRecord() throws IOException {
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
        assertEmployee(expectedEmployee, responseEntity.getBody());
    }

    @Test
    void testGivenRandomId_WhenFindEmployeeById_ThenReturnRecord() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/employees/" + id, HttpMethod.GET, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with id " + id);
    }

    @Test
    void testGivenEmployee_WhenFindEmployeesByExample_ThenReturnRecords() throws IOException {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO saveEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO expectedEmployee = restTemplate.postForEntity(url + "/employees", saveEmployee, EmployeeDTO.class).getBody();
        EmployeeDTO searchEmployee = objectMapper.convertValue(expectedEmployee,EmployeeDTO.class);
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
        assertEmployee(expectedEmployee, responseEntity.getBody().get(0));
    }

    @Test
    void testGivenRandomEmployee_WhenFindEmployeesByExample_ThenReturnError() throws IOException {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO saveEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        saveEmployee.setAddress(null);
        saveEmployee.setPhoneNumbers(null);
        Map<String, Object> map = objectMapper.convertValue(saveEmployee, Map.class);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> entity = new HttpEntity<Map>(map, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/employees/search", HttpMethod.POST, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with map " + map);
    }

    @Test
    void testGivenEmployee_WhenSaveEmployee_ThenReturnNewEmployee() throws IOException {
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
        assertEmployee(expectedEmployee, responseEntity.getBody());
    }

    @Test
    void testGivenSavedEmployee_WhenSaveEmployee_ThenReturnError() throws IOException {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO saveEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO expectedEmployee = restTemplate.postForEntity(url + "/employees", saveEmployee, EmployeeDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeDTO> entity = new HttpEntity<EmployeeDTO>(expectedEmployee, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/employees", HttpMethod.POST, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.FOUND.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Record already found with id " + expectedEmployee.getId());
    }

    @Test
    void testGivenExistingEmployee_WhenUpdateEmployee_ThenReturnUpdatedEmployee() throws IOException {
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
        assertEmployee(expectedEmployee, responseEntity.getBody());
    }

    @Test
    void testGivenNull_WhenUpdateEmployee_ThenThrowError() throws IOException {
        // Given
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(1);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeDTO> entity = new HttpEntity<EmployeeDTO>(employee, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/employees/" + employee.getId(), HttpMethod.PUT, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with id " + employee.getId());
    }

    @Test
    void testGivenNull_WhenUpdateEmployee_ThenThrowError1() throws IOException {
        // Given
        int id = RandomUtils.nextInt();
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(1);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeDTO> entity = new HttpEntity<EmployeeDTO>(employee, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/employees/" + id, HttpMethod.PUT, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(500);
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Update Record id: " + id + " not equal to payload id: " + employee.getId());
    }

    @Test
    void testGivenNull_WhenUpdateEmployee_ThenThrowError2() throws IOException {
        // Given
        int id = RandomUtils.nextInt();
        List<EmployeeDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO expectedEmployee = students.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeDTO> entity = new HttpEntity<EmployeeDTO>(expectedEmployee, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/employees/"+id, HttpMethod.PUT, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Payload record id is null");
    }

    @Test
    void testGiveId_WhenDeleteEmployee_ThenReturnTrue() throws IOException {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO savedEmployee = restTemplate.postForEntity(url + "/employees", employee, EmployeeDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(url + "/employees/" + savedEmployee.getId(), HttpMethod.DELETE, entity, Boolean.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody()).isTrue();
    }

    @Test
    void testGiveRandomId_WhenDeleteEmployee_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(url + "/employees/" + id, HttpMethod.DELETE, entity, Boolean.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody()).isFalse();
    }

    private void assertEmployee(EmployeeDTO actualEmployee, EmployeeDTO expectedEmployee) {
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee.getFirstName()).isEqualTo(expectedEmployee.getFirstName());
        Assertions.assertThat(actualEmployee.getLastName()).isEqualTo(expectedEmployee.getLastName());
        Assertions.assertThat(actualEmployee.getAge()).isEqualTo(expectedEmployee.getAge());
        Assertions.assertThat(actualEmployee.getNoOfChildrens()).isEqualTo(expectedEmployee.getNoOfChildrens());
        Assertions.assertThat(actualEmployee.getSpouse()).isEqualTo(expectedEmployee.getSpouse());
        Assertions.assertThat(actualEmployee.getDateOfJoining()).isEqualTo(expectedEmployee.getDateOfJoining());
        Assertions.assertThat(actualEmployee.getHobbies().toArray()).isEqualTo(expectedEmployee.getHobbies().toArray());
        Assertions.assertThat(actualEmployee.getPhoneNumbers().stream().map(PhoneNumberDTO::getType).toArray()).isEqualTo(expectedEmployee.getPhoneNumbers().stream().map(PhoneNumberDTO::getType).toArray());
        Assertions.assertThat(actualEmployee.getPhoneNumbers().stream().map(PhoneNumberDTO::getNumber).toArray()).isEqualTo(expectedEmployee.getPhoneNumbers().stream().map(PhoneNumberDTO::getNumber).toArray());
        Assertions.assertThat(actualEmployee.getAddress().getStreetAddress()).isEqualTo(expectedEmployee.getAddress().getStreetAddress());
        Assertions.assertThat(actualEmployee.getAddress().getCity()).isEqualTo(expectedEmployee.getAddress().getCity());
        Assertions.assertThat(actualEmployee.getAddress().getState()).isEqualTo(expectedEmployee.getAddress().getState());
        Assertions.assertThat(actualEmployee.getAddress().getCountry()).isEqualTo(expectedEmployee.getAddress().getCountry());
        Assertions.assertThat(actualEmployee.getAddress().getPostalCode()).isEqualTo(expectedEmployee.getAddress().getPostalCode());
    }
}
