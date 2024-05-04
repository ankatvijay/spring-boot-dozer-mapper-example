package com.spring.crud.demo.controller.mockmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.controller.BaseControllerTest;
import com.spring.crud.demo.controller.BaseSetUp;
import com.spring.crud.demo.dto.StudentDTO;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.utils.Constant;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


//@ExtendWith(SpringExtension.class)
//@WebMvcTest(controllers = StudentController.class)
@SpringBootTest
@AutoConfigureMockMvc
public class StudentControllerMockMVCTest implements BaseControllerTest<Student, StudentDTO> {

    @Autowired
    private MockMvc mockMvc;

    private static File file;
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;

    @BeforeAll
    static void init() {
        objectMapper = new ObjectMapper();
        typeFactory = objectMapper.getTypeFactory();
        file = FileLoader.getFileFromResource("students.json");
    }

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/students")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws Exception {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        students.forEach(studentDTO -> new BaseSetUp<StudentDTO,StudentDTO>("/students", mockMvc,objectMapper).accept(studentDTO));
        Tuple[] expectedStudents = students.stream()
                .map(student -> AssertionsForClassTypes.tuple(student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()))
                .toArray(Tuple[]::new);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/students")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.[*]").isArray());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(Matchers.greaterThan(0)));
        String strResult = resultActions.andReturn().getResponse().getContentAsString();
        List<StudentDTO> actualStudents = objectMapper.readValue(strResult, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        Assertions.assertThat(actualStudents).extracting(
                        StudentDTO::getRollNo,
                        StudentDTO::getFirstName,
                        StudentDTO::getLastName,
                        StudentDTO::getDateOfBirth,
                        StudentDTO::getMarks)
                .containsExactly(expectedStudents);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenThrowException() throws Exception {
        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/students")
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
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO insertRecord = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO expectedStudent = new BaseSetUp<StudentDTO,StudentDTO>("/students", mockMvc,objectMapper).apply(insertRecord);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/students/{id}", expectedStudent.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expectedStudent.getId()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.rollNo").value(expectedStudent.getRollNo()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(expectedStudent.getFirstName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(expectedStudent.getLastName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.dateOfBirth").value(expectedStudent.getDateOfBirth()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.marks").value(expectedStudent.getMarks()));
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() throws Exception {
        // Given
        int id = RandomUtils.nextInt();

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/students/{id}", id)
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
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO insertRecord = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO expectedStudent = new BaseSetUp<StudentDTO,StudentDTO>("/students", mockMvc,objectMapper).apply(insertRecord);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/students/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedStudent))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.[*]").isArray());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(Matchers.greaterThan(0)));
        String strResult = resultActions.andReturn().getResponse().getContentAsString();
        List<StudentDTO> actualStudents = objectMapper.readValue(strResult, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        assertRecord(objectMapper.readValue(objectMapper.writeValueAsString(expectedStudent), Student.class), actualStudents.getFirst());
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenThrowException() throws Exception {
        // Given
        Student expectedStudent = new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/students/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedStudent))
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
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedStudent))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isCreated());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.rollNo").value(expectedStudent.getRollNo()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(expectedStudent.getFirstName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(expectedStudent.getLastName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.dateOfBirth").value(expectedStudent.getDateOfBirth()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.marks").value(expectedStudent.getMarks()));
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() throws Exception {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO insertRecord = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO expectedStudent = new BaseSetUp<StudentDTO,StudentDTO>("/students", mockMvc,objectMapper).apply(insertRecord);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedStudent))
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
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO insertRecord = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO expectedStudent = new BaseSetUp<StudentDTO,StudentDTO>("/students", mockMvc,objectMapper).apply(insertRecord);

        // When
        expectedStudent.setMarks(800.f);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .put("/students/{id}", expectedStudent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedStudent))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isAccepted());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expectedStudent.getId()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.rollNo").value(expectedStudent.getRollNo()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(expectedStudent.getFirstName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(expectedStudent.getLastName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.dateOfBirth").value(expectedStudent.getDateOfBirth()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.marks").value(expectedStudent.getMarks()));
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() throws Exception {
        // Given
        int id = RandomUtils.nextInt();
        StudentDTO student = new StudentDTO();
        student.setId(1);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .put("/students/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isInternalServerError());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status", 500).exists());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.message", "Update Record id: " + id + " not equal to payload id: " + student.getId()).exists());
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() throws Exception {
        // Given
        List<StudentDTO> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        StudentDTO student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
        StudentDTO savedStudent = new BaseSetUp<StudentDTO,StudentDTO>("/students", mockMvc,objectMapper).apply(student);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/students/{id}", savedStudent.getId()))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isAccepted());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status", HttpStatus.ACCEPTED.value()).exists());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.message", "Record deleted with id " + savedStudent.getId()).exists());
    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() throws Exception {
        // Given
        int id = RandomUtils.nextInt();

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/students/{id}", id))
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
                        .delete("/students"))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Override
    public void assertRecord(Student expectedRecord, StudentDTO actualRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getRollNo()).isEqualTo(expectedRecord.getRollNo());
        Assertions.assertThat(actualRecord.getFirstName()).isEqualTo(expectedRecord.getFirstName());
        Assertions.assertThat(actualRecord.getLastName()).isEqualTo(expectedRecord.getLastName());
        Assertions.assertThat(actualRecord.getDateOfBirth()).isEqualTo(expectedRecord.getDateOfBirth().format(DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)));
        Assertions.assertThat(actualRecord.getMarks()).isEqualTo(expectedRecord.getMarks());
    }
}
