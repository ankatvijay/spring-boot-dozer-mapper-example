package com.spring.crud.demo.controller.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.controller.BaseControllerTest;
import com.spring.crud.demo.dto.ResponseDTO;
import com.spring.crud.demo.dto.SuperHeroDTO;
import com.spring.crud.demo.model.SuperHero;
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
public class SuperHeroControllerIT implements BaseControllerTest<SuperHeroDTO, SuperHeroDTO> {

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
        file = FileLoader.getFileFromResource("superheroes.json");
    }

    @BeforeEach
    public void setUp() {
        url = String.format("http://localhost:%d", port);
        restTemplate.delete(url + "/super-heroes");
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws IOException {
        // Given
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        superHeros.forEach(s -> restTemplate.postForEntity(url + "/super-heroes", s, SuperHeroDTO.class));
        Tuple[] expectedSuperHeroes = superHeros.stream()
                .map(superHero -> AssertionsForClassTypes.tuple(
                        superHero.getName(),
                        superHero.getSuperName(),
                        superHero.getProfession(),
                        superHero.getAge(),
                        superHero.getCanFly()))
                .toArray(Tuple[]::new);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<List<SuperHeroDTO>> responseEntity = restTemplate.exchange(url + "/super-heroes", HttpMethod.GET, entity, new ParameterizedTypeReference<List<SuperHeroDTO>>() {
        });

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().size()).isGreaterThan(0);
        Assertions.assertThat(responseEntity.getBody()).extracting(
                        SuperHeroDTO::getName,
                        SuperHeroDTO::getSuperName,
                        SuperHeroDTO::getProfession,
                        SuperHeroDTO::getAge,
                        SuperHeroDTO::getCanFly)
                .containsExactly(expectedSuperHeroes);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenThrowException() {
        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes", HttpMethod.GET, entity, ResponseDTO.class);

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
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO insertRecord = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO expectedSuperHero = restTemplate.postForEntity(url + "/super-heroes", insertRecord, SuperHeroDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<SuperHeroDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/" + expectedSuperHero.getId(), HttpMethod.GET, entity, SuperHeroDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getId()).isEqualTo(expectedSuperHero.getId());
        assertRecord(expectedSuperHero, responseEntity.getBody());
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
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/" + id, HttpMethod.GET, entity, ResponseDTO.class);

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
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO insertRecord = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO expectedSuperHero = restTemplate.postForEntity(url + "/super-heroes", insertRecord, SuperHeroDTO.class).getBody();
        Map<String, Object> map = new ObjectMapper().convertValue(expectedSuperHero, Map.class);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> entity = new HttpEntity<Map>(map, headers);
        ResponseEntity<List<SuperHeroDTO>> responseEntity = restTemplate.exchange(url + "/super-heroes/search", HttpMethod.POST, entity, new ParameterizedTypeReference<List<SuperHeroDTO>>() {
        });

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().size()).isGreaterThan(0);
        Assertions.assertThat(responseEntity.getBody().get(0).getId()).isEqualTo(expectedSuperHero.getId());
        assertRecord(expectedSuperHero, responseEntity.getBody().get(0));
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenThrowException() {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Bruce Wayne", "Batman", "Business man", 35, true);
        SuperHeroDTO map = objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SuperHeroDTO> entity = new HttpEntity<SuperHeroDTO>(map, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/search", HttpMethod.POST, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("No record found with map " + map);
    }

    @Test
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() throws IOException {
        // Given
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO expectedSuperHero = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SuperHeroDTO> entity = new HttpEntity<SuperHeroDTO>(expectedSuperHero, headers);
        ResponseEntity<SuperHeroDTO> responseEntity = restTemplate.exchange(url + "/super-heroes", HttpMethod.POST, entity, SuperHeroDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        assertRecord(expectedSuperHero, responseEntity.getBody());
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() throws IOException {
        // Given
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO insertRecord = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO expectedSuperHero = restTemplate.postForEntity(url + "/super-heroes", insertRecord, SuperHeroDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SuperHeroDTO> entity = new HttpEntity<SuperHeroDTO>(expectedSuperHero, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes", HttpMethod.POST, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(HttpStatus.FOUND.value());
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("Record already found with id " + expectedSuperHero.getId());
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {
        // Given
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO insertRecord = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO expectedSuperHero = restTemplate.postForEntity(url + "/super-heroes", insertRecord, SuperHeroDTO.class).getBody();

        // When
        expectedSuperHero.setAge(45);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SuperHeroDTO> entity = new HttpEntity<SuperHeroDTO>(expectedSuperHero, headers);
        ResponseEntity<SuperHeroDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/" + expectedSuperHero.getId(), HttpMethod.PUT, entity, SuperHeroDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getId()).isEqualTo(expectedSuperHero.getId());
        assertRecord(expectedSuperHero, responseEntity.getBody());
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();
        SuperHeroDTO superHero = new SuperHeroDTO();
        superHero.setId(1);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SuperHeroDTO> entity = new HttpEntity<SuperHeroDTO>(superHero, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/" + id, HttpMethod.PUT, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(500);
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("Update Record id: " + id + " not equal to payload id: " + superHero.getId());
    }

    @Test
    void testGivenRandomIdAndRandomRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        SuperHeroDTO superHero = new SuperHeroDTO();
        superHero.setId(1);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SuperHeroDTO> entity = new HttpEntity<SuperHeroDTO>(superHero, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/" + superHero.getId(), HttpMethod.PUT, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("No record found with id " + superHero.getId());
    }

    @Test
    void testGivenRandomIdAndExistingRecordWithoutId_WhenUpdateRecord_ThenThrowException() throws IOException {
        // Given
        int id = RandomUtils.nextInt();
        List<SuperHeroDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO expectedSuperHero = students.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SuperHeroDTO> entity = new HttpEntity<SuperHeroDTO>(expectedSuperHero, headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/" + id, HttpMethod.PUT, entity, ResponseDTO.class);

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
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO superHero = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO savedSuperHero = restTemplate.postForEntity(url + "/super-heroes", superHero, SuperHeroDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/" + savedSuperHero.getId(), HttpMethod.DELETE, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().status()).isEqualTo(HttpStatus.ACCEPTED.value());
        Assertions.assertThat(responseEntity.getBody().message()).isEqualTo("Record deleted with id " + savedSuperHero.getId());
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
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/" + id, HttpMethod.DELETE, entity, ResponseDTO.class);

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
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes", HttpMethod.DELETE, entity, ResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    public void assertRecord(SuperHeroDTO expectedRecord, SuperHeroDTO actualRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getName()).isEqualTo(expectedRecord.getName());
        Assertions.assertThat(actualRecord.getSuperName()).isEqualTo(expectedRecord.getSuperName());
        Assertions.assertThat(actualRecord.getProfession()).isEqualTo(expectedRecord.getProfession());
        Assertions.assertThat(actualRecord.getAge()).isEqualTo(expectedRecord.getAge());
        Assertions.assertThat(actualRecord.getCanFly()).isEqualTo(expectedRecord.getCanFly());
    }
}
