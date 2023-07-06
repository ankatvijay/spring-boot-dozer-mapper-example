package com.spring.crud.demo.controller.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.dto.ErrorResponseDTO;
import com.spring.crud.demo.dto.SuperHeroDTO;
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
public class SuperHeroControllerIT {

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
        restTemplate.delete(url + "/super-heroes" );
    }

    @Test
    void testGivenNon_WhenFindAllSuperHeroes_ThenReturnAllRecord() throws IOException {
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
    void testGivenNon_WhenFindAllSuperHeroes_ThenReturnError() {
        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes", HttpMethod.GET, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found");
    }

    @Test
    void testGivenId_WhenFindSuperHeroById_ThenReturnRecord() throws IOException {
        // Given
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO saveSuperHero = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO expectedSuperHero = restTemplate.postForEntity(url + "/super-heroes", saveSuperHero, SuperHeroDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<SuperHeroDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/" + expectedSuperHero.getId(), HttpMethod.GET, entity, SuperHeroDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getId()).isEqualTo(expectedSuperHero.getId());
        assertSuperHero(expectedSuperHero, responseEntity.getBody());
    }

    @Test
    void testGivenRandomId_WhenFindSuperHeroById_ThenReturnRecord() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/" + id, HttpMethod.GET, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with id " + id);
    }

    @Test
    void testGivenSuperHero_WhenFindSuperHeroesByExample_ThenReturnRecords() throws IOException {
        // Given
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO saveSuperHero = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO expectedSuperHero = restTemplate.postForEntity(url + "/super-heroes", saveSuperHero, SuperHeroDTO.class).getBody();
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
        assertSuperHero(expectedSuperHero, responseEntity.getBody().get(0));
    }

    @Test
    void testGivenRandomSuperHero_WhenFindSuperHeroesByExample_ThenReturnError() throws IOException {
        // Given
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO saveSuperHero = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        Map<String, Object> map = objectMapper.convertValue(saveSuperHero, Map.class);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> entity = new HttpEntity<Map>(map, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/search", HttpMethod.POST, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with map " + map);
    }

    @Test
    void testGivenSuperHero_WhenSaveSuperHero_ThenReturnNewSuperHero() throws IOException {
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
        assertSuperHero(expectedSuperHero, responseEntity.getBody());
    }

    @Test
    void testGivenSavedSuperHero_WhenSaveSuperHero_ThenReturnError() throws IOException {
        // Given
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO saveSuperHero = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO expectedSuperHero = restTemplate.postForEntity(url + "/super-heroes", saveSuperHero, SuperHeroDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SuperHeroDTO> entity = new HttpEntity<SuperHeroDTO>(expectedSuperHero, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes", HttpMethod.POST, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.FOUND.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Record already found with id " + expectedSuperHero.getId());
    }

    @Test
    void testGivenExistingSuperHero_WhenUpdateSuperHero_ThenReturnUpdatedSuperHero() throws IOException {
        // Given
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO saveSuperHero = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO expectedSuperHero = restTemplate.postForEntity(url + "/super-heroes", saveSuperHero, SuperHeroDTO.class).getBody();

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
        assertSuperHero(expectedSuperHero, responseEntity.getBody());
    }

    @Test
    void testGivenNull_WhenUpdateSuperHero_ThenThrowError() throws IOException {
        // Given
        SuperHeroDTO superHero = new SuperHeroDTO();
        superHero.setId(1);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SuperHeroDTO> entity = new HttpEntity<SuperHeroDTO>(superHero, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/" + superHero.getId(), HttpMethod.PUT, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("No record found with id " + superHero.getId());
    }

    @Test
    void testGivenNull_WhenUpdateSuperHero_ThenThrowError1() throws IOException {
        // Given
        int id = RandomUtils.nextInt();
        SuperHeroDTO superHero = new SuperHeroDTO();
        superHero.setId(1);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SuperHeroDTO> entity = new HttpEntity<SuperHeroDTO>(superHero, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/" + id, HttpMethod.PUT, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(500);
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Update Record id: " + id + " not equal to payload id: " + superHero.getId());
    }

    @Test
    void testGivenNull_WhenUpdateSuperHero_ThenThrowError2() throws IOException {
        // Given
        int id = RandomUtils.nextInt();
        List<SuperHeroDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO expectedSuperHero = students.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SuperHeroDTO> entity = new HttpEntity<SuperHeroDTO>(expectedSuperHero, headers);
        ResponseEntity<ErrorResponseDTO> responseEntity = restTemplate.exchange(url + "/super-heroes/"+id, HttpMethod.PUT, entity, ErrorResponseDTO.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Assertions.assertThat(responseEntity.getBody().getMessage()).isEqualTo("Payload record id is null");
    }

    @Test
    void testGiveId_WhenDeleteSuperHero_ThenReturnTrue() throws IOException {
        // Given
        List<SuperHeroDTO> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO superHero = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO savedSuperHero = restTemplate.postForEntity(url + "/super-heroes", superHero, SuperHeroDTO.class).getBody();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(url + "/super-heroes/" + savedSuperHero.getId(), HttpMethod.DELETE, entity, Boolean.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody()).isTrue();
    }

    @Test
    void testGiveRandomId_WhenDeleteSuperHero_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(url + "/super-heroes/" + id, HttpMethod.DELETE, entity, Boolean.class);

        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody()).isFalse();
    }

    private void assertSuperHero(SuperHeroDTO actualSuperHero, SuperHeroDTO expectedSuperHero) {
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero.getName()).isEqualTo(expectedSuperHero.getName());
        Assertions.assertThat(actualSuperHero.getSuperName()).isEqualTo(expectedSuperHero.getSuperName());
        Assertions.assertThat(actualSuperHero.getProfession()).isEqualTo(expectedSuperHero.getProfession());
        Assertions.assertThat(actualSuperHero.getAge()).isEqualTo(expectedSuperHero.getAge());
        Assertions.assertThat(actualSuperHero.getCanFly()).isEqualTo(expectedSuperHero.getCanFly());
    }
}
