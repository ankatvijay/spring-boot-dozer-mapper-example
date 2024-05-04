package com.spring.crud.demo.controller.mockmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.controller.BaseControllerTest;
import com.spring.crud.demo.controller.BaseSetUp;
import com.spring.crud.demo.dto.SuperHeroDTO;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


//@ExtendWith(SpringExtension.class)
//@WebMvcTest(controllers = SuperHeroController.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SuperHeroControllerMockMVCTest implements BaseControllerTest<SuperHero, SuperHeroDTO> {

    @Autowired
    private MockMvc mockMvc;

    private static File file;
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;

    @BeforeAll
    static void init() {
        objectMapper = new ObjectMapper();
        typeFactory = objectMapper.getTypeFactory();
        file = FileLoader.getFileFromResource("superheroes.json");
    }

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/super-heroes")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws Exception {
        // Given
        List<SuperHeroDTO> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        superHeroes.forEach(superHeroDTO -> new BaseSetUp<SuperHeroDTO,SuperHeroDTO>("/super-heroes", mockMvc,objectMapper).accept(superHeroDTO));
        Tuple[] expectedSuperHeroes = superHeroes.stream()
                .map(superHero -> AssertionsForClassTypes.tuple(
                        superHero.getName(),
                        superHero.getSuperName(),
                        superHero.getProfession(),
                        superHero.getAge(),
                        superHero.getCanFly()))
                .toArray(Tuple[]::new);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/super-heroes")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.[*]").isArray());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(Matchers.greaterThan(0)));
        String strResult = resultActions.andReturn().getResponse().getContentAsString();
        List<SuperHeroDTO> actualSuperHeroes = objectMapper.readValue(strResult, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        Assertions.assertThat(actualSuperHeroes)
                .extracting(SuperHeroDTO::getName,
                        SuperHeroDTO::getSuperName,
                        SuperHeroDTO::getProfession,
                        SuperHeroDTO::getAge,
                        SuperHeroDTO::getCanFly)
                .containsExactly(expectedSuperHeroes);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenThrowException() throws Exception {
        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/super-heroes")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status", 404).exists());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.message", "No record found").exists());
    }

    @Test
    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws Exception {
        // Given
        List<SuperHeroDTO> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO insertRecord = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO expectedSuperHero = new BaseSetUp<SuperHeroDTO,SuperHeroDTO>("/super-heroes", mockMvc,objectMapper).apply(insertRecord);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/super-heroes/{id}", expectedSuperHero.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expectedSuperHero.getId()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(expectedSuperHero.getName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.superName").value(expectedSuperHero.getSuperName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.profession").value(expectedSuperHero.getProfession()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.age").value(expectedSuperHero.getAge()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.canFly").value(expectedSuperHero.getCanFly()));
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() throws Exception {
        // Given
        int id = RandomUtils.nextInt();

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/super-heroes/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status", 404).exists());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.message", "No record found").exists());
    }

    @Test
    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws Exception {
        // Given
        List<SuperHeroDTO> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO insertRecord = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO expectedSuperHero = new BaseSetUp<SuperHeroDTO,SuperHeroDTO>("/super-heroes", mockMvc,objectMapper).apply(insertRecord);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/super-heroes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedSuperHero))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.[*]").isArray());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(Matchers.greaterThan(0)));
        String strResult = resultActions.andReturn().getResponse().getContentAsString();
        List<SuperHeroDTO> actualSuperHeroes = objectMapper.readValue(strResult, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        assertRecord(objectMapper.readValue(objectMapper.writeValueAsString(expectedSuperHero), SuperHero.class), actualSuperHeroes.getFirst());
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenThrowException() throws Exception {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Bruce Wayne", "Batman", "Business man", 35, true);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/super-heroes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedSuperHero))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status", 404).exists());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.message", "No record found").exists());
    }

    @Test
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() throws Exception {
        // Given
        List<SuperHeroDTO> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO expectedSuperHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/super-heroes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedSuperHero))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isCreated());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(expectedSuperHero.getName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.superName").value(expectedSuperHero.getSuperName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.profession").value(expectedSuperHero.getProfession()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.age").value(expectedSuperHero.getAge()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.canFly").value(expectedSuperHero.getCanFly()));
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() throws Exception {
        // Given
        List<SuperHeroDTO> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO insertRecord = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO expectedSuperHero = new BaseSetUp<SuperHeroDTO,SuperHeroDTO>("/super-heroes", mockMvc,objectMapper).apply(insertRecord);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/super-heroes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedSuperHero))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isFound());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status", 404).exists());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.message", "No record found").exists());
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() throws Exception {
        // Given
        List<SuperHeroDTO> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO insertRecord = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO expectedSuperHero = new BaseSetUp<SuperHeroDTO,SuperHeroDTO>("/super-heroes", mockMvc,objectMapper).apply(insertRecord);

        // When
        expectedSuperHero.setAge(45);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .put("/super-heroes/{id}", expectedSuperHero.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedSuperHero))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isAccepted());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expectedSuperHero.getId()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(expectedSuperHero.getName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.superName").value(expectedSuperHero.getSuperName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.profession").value(expectedSuperHero.getProfession()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.age").value(expectedSuperHero.getAge()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.canFly").value(expectedSuperHero.getCanFly()));
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() throws Exception {
        // Given
        int id = RandomUtils.nextInt();
        SuperHeroDTO superHero = new SuperHeroDTO();
        superHero.setId(1);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .put("/super-heroes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(superHero))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isInternalServerError());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status", 500).exists());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.message", "Update Record id: " + id + " not equal to payload id: " + superHero.getId()).exists());
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() throws Exception {
        // Given
        List<SuperHeroDTO> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        SuperHeroDTO superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
        SuperHeroDTO savedSuperHero = new BaseSetUp<SuperHeroDTO,SuperHeroDTO>("/super-heroes", mockMvc,objectMapper).apply(superHero);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/super-heroes/{id}", savedSuperHero.getId()))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isAccepted());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status", HttpStatus.ACCEPTED.value()).exists());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.message", "Record deleted with id " + savedSuperHero.getId()).exists());
    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() throws Exception {
        // Given
        int id = RandomUtils.nextInt();

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/super-heroes/{id}", id))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status", HttpStatus.NOT_FOUND.value()).exists());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.message", "No record found with id " + id).exists());
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() throws Exception {
        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/super-heroes"))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Override
    public void assertRecord(SuperHero expectedRecord, SuperHeroDTO actualRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getName()).isEqualTo(expectedRecord.getName());
        Assertions.assertThat(actualRecord.getSuperName()).isEqualTo(expectedRecord.getSuperName());
        Assertions.assertThat(actualRecord.getProfession()).isEqualTo(expectedRecord.getProfession());
        Assertions.assertThat(actualRecord.getAge()).isEqualTo(expectedRecord.getAge());
        Assertions.assertThat(actualRecord.getCanFly()).isEqualTo(expectedRecord.getCanFly());
    }
}
