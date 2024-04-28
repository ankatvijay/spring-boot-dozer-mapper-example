package com.spring.crud.demo.controller.mockmvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.controller.BaseControllerTest;
import com.spring.crud.demo.dto.SuperHeroDTO;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.utils.FileLoader;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;


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
        superHeroes.stream().forEach(superHeroDTO -> new SuperHeroSupplier().accept(superHeroDTO));
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
        String strResult = resultActions.andReturn().getResponse().getContentAsString();
        List<SuperHeroDTO> actualSuperHeroes = objectMapper.readValue(strResult, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        Assertions.assertThat(actualSuperHeroes)
                .extracting(SuperHeroDTO::getName,
                        SuperHeroDTO::getSuperName,
                        SuperHeroDTO::getProfession,
                        SuperHeroDTO::getAge,
                        SuperHeroDTO::getCanFly)
                .containsExactly(expectedSuperHeroes);

        //resultActions.andExpect(MockMvcResultMatchers.);
        //resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.employees").exists());
        //Assertions.assertEquals(NotFoundException.class, mvcResult.getResolvedException().getClass());
    }

    @Override
    public void testGivenNon_WhenGetAllRecords_ThenThrowException() {

    }

    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws IOException {

    }

    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() {

    }

    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws IOException {

    }

    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenThrowException() throws JsonProcessingException {

    }

    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() throws IOException {

    }

    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() throws IOException {

    }

    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {

    }

    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() {

    }

    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() throws IOException {

    }

    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() {

    }

    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {

    }

    @Override
    public void assertRecord(SuperHero expectedRecord, SuperHeroDTO actualRecord) {

    }

    class SuperHeroSupplier implements Consumer<SuperHeroDTO>{
        @Override
        public void accept(SuperHeroDTO superHeroDTO) {
            try {
                mockMvc.perform(MockMvcRequestBuilders.post("/super-heroes")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(superHeroDTO)))
                        .andDo(MockMvcResultHandlers.log())
                        .andExpect(MockMvcResultMatchers.status().isCreated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
