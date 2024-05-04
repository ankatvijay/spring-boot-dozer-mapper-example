package com.spring.crud.demo.controller.mockmvc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.controller.BaseControllerTest;
import com.spring.crud.demo.controller.BaseSetUp;
import com.spring.crud.demo.dto.emp.EmployeeDTO;
import com.spring.crud.demo.dto.emp.PhoneNumberDTO;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;
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
//@WebMvcTest(controllers = EmployeeController.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerMockMVCTest implements BaseControllerTest<Employee, EmployeeDTO> {

    @Autowired
    private MockMvc mockMvc;

    private static File file;
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;

    @BeforeAll
    static void init() {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        typeFactory = objectMapper.getTypeFactory();
        file = FileLoader.getFileFromResource("employees.json");
    }

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/employees")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws Exception {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        employees.forEach(employeeDTO -> new BaseSetUp<EmployeeDTO,EmployeeDTO>("/employees", mockMvc,objectMapper).accept(employeeDTO));
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
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/employees")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.[*]").isArray());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(Matchers.greaterThan(0)));
        String strResult = resultActions.andReturn().getResponse().getContentAsString();
        List<EmployeeDTO> actualEmployees = objectMapper.readValue(strResult, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        Assertions.assertThat(actualEmployees)
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
    public void testGivenNon_WhenGetAllRecords_ThenThrowException() throws Exception {
        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/employees")
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
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO insertRecord = employees.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO expectedEmployee = new BaseSetUp<EmployeeDTO,EmployeeDTO>("/employees", mockMvc,objectMapper).apply(insertRecord);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/employees/{id}", expectedEmployee.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expectedEmployee.getId()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(expectedEmployee.getFirstName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(expectedEmployee.getLastName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.age").value(expectedEmployee.getAge()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.noOfChildrens").value(expectedEmployee.getNoOfChildrens()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.spouse").value(expectedEmployee.getSpouse()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.dateOfJoining").value(expectedEmployee.getDateOfJoining()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.hobbies").value(expectedEmployee.getHobbies()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.id").value(expectedEmployee.getAddress().getId()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.streetAddress").value(expectedEmployee.getAddress().getStreetAddress()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.city").value(expectedEmployee.getAddress().getCity()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.state").value(expectedEmployee.getAddress().getState()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.country").value(expectedEmployee.getAddress().getCountry()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.postalCode").value(expectedEmployee.getAddress().getPostalCode()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.phoneNumbers[0].id").value(expectedEmployee.getPhoneNumbers().get(0).getId()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.phoneNumbers[0].type").value(expectedEmployee.getPhoneNumbers().get(0).getType()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.phoneNumbers[0].number").value(expectedEmployee.getPhoneNumbers().get(0).getNumber()));
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() throws Exception {
        // Given
        int id = RandomUtils.nextInt();

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get("/employees/{id}", id)
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
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO saveEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO expectedEmployee = new BaseSetUp<EmployeeDTO,EmployeeDTO>("/employees", mockMvc,objectMapper).apply(saveEmployee);
        EmployeeDTO searchEmployee = objectMapper.convertValue(expectedEmployee, EmployeeDTO.class);
        searchEmployee.setAddress(null);
        searchEmployee.setPhoneNumbers(null);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/employees/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchEmployee))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.[*]").isArray());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(Matchers.greaterThan(0)));
        String strResult = resultActions.andReturn().getResponse().getContentAsString();
        List<EmployeeDTO> actualEmployees = objectMapper.readValue(strResult, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        assertRecord(objectMapper.readValue(objectMapper.writeValueAsString(expectedEmployee), Employee.class), actualEmployees.getFirst());
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenThrowException() throws Exception {
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
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/employees/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedEmployee))
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
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO expectedEmployee = employees.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedEmployee))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isCreated());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(expectedEmployee.getFirstName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(expectedEmployee.getLastName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.age").value(expectedEmployee.getAge()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.noOfChildrens").value(expectedEmployee.getNoOfChildrens()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.spouse").value(expectedEmployee.getSpouse()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.dateOfJoining").value(expectedEmployee.getDateOfJoining()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.hobbies").value(expectedEmployee.getHobbies()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.streetAddress").value(expectedEmployee.getAddress().getStreetAddress()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.city").value(expectedEmployee.getAddress().getCity()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.state").value(expectedEmployee.getAddress().getState()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.country").value(expectedEmployee.getAddress().getCountry()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.postalCode").value(expectedEmployee.getAddress().getPostalCode()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.phoneNumbers[0].type").value(expectedEmployee.getPhoneNumbers().get(0).getType()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.phoneNumbers[0].number").value(expectedEmployee.getPhoneNumbers().get(0).getNumber()));
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() throws Exception {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO insertRecord = employees.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO expectedEmployee = new BaseSetUp<EmployeeDTO,EmployeeDTO>("/employees", mockMvc,objectMapper).apply(insertRecord);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedEmployee))
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
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO insertRecord = employees.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO expectedEmployee = new BaseSetUp<EmployeeDTO,EmployeeDTO>("/employees", mockMvc,objectMapper).apply(insertRecord);

        // When
        expectedEmployee.setAge(45);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .put("/employees/{id}", expectedEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedEmployee))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isAccepted());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expectedEmployee.getId()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(expectedEmployee.getFirstName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(expectedEmployee.getLastName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.age").value(expectedEmployee.getAge()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.noOfChildrens").value(expectedEmployee.getNoOfChildrens()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.spouse").value(expectedEmployee.getSpouse()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.dateOfJoining").value(expectedEmployee.getDateOfJoining()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.hobbies").value(expectedEmployee.getHobbies()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.id").value(expectedEmployee.getAddress().getId()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.streetAddress").value(expectedEmployee.getAddress().getStreetAddress()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.city").value(expectedEmployee.getAddress().getCity()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.state").value(expectedEmployee.getAddress().getState()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.country").value(expectedEmployee.getAddress().getCountry()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.address.postalCode").value(expectedEmployee.getAddress().getPostalCode()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.phoneNumbers[0].id").value(expectedEmployee.getPhoneNumbers().get(0).getId()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.phoneNumbers[0].type").value(expectedEmployee.getPhoneNumbers().get(0).getType()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.phoneNumbers[0].number").value(expectedEmployee.getPhoneNumbers().get(0).getNumber()));
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() throws Exception {
        // Given
        int id = RandomUtils.nextInt();
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(1);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .put("/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isInternalServerError());
        resultActions.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status", 500).exists());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.message", "Update Record id: " + id + " not equal to payload id: " + employee.getId()).exists());
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() throws Exception {
        // Given
        List<EmployeeDTO> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        EmployeeDTO employee = employees.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
        EmployeeDTO savedEmployee = new BaseSetUp<EmployeeDTO,EmployeeDTO>("/employees", mockMvc,objectMapper).apply(employee);

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/employees/{id}", savedEmployee.getId()))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isAccepted());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status", HttpStatus.ACCEPTED.value()).exists());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.message", "Record deleted with id " + savedEmployee.getId()).exists());
    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() throws Exception {
        // Given
        int id = RandomUtils.nextInt();

        // When
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/employees/{id}", id))
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
                        .delete("/employees"))
                .andDo(MockMvcResultHandlers.log());

        // Then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Override
    public void assertRecord(Employee expectedRecord, EmployeeDTO actualRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getFirstName()).isEqualTo(expectedRecord.getFirstName());
        Assertions.assertThat(actualRecord.getLastName()).isEqualTo(expectedRecord.getLastName());
        Assertions.assertThat(actualRecord.getAge()).isEqualTo(expectedRecord.getAge());
        Assertions.assertThat(actualRecord.getNoOfChildrens()).isEqualTo(expectedRecord.getNoOfChildrens());
        Assertions.assertThat(actualRecord.getSpouse()).isEqualTo(expectedRecord.getSpouse());
        Assertions.assertThat(actualRecord.getDateOfJoining()).isEqualTo(expectedRecord.getDateOfJoining().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)));
        Assertions.assertThat(actualRecord.getHobbies().toArray()).isEqualTo(expectedRecord.getHobbies().toArray());
        Assertions.assertThat(actualRecord.getPhoneNumbers().stream().map(PhoneNumberDTO::getId).toArray()).isEqualTo(expectedRecord.getPhoneNumbers().stream().map(PhoneNumber::getId).toArray());
        Assertions.assertThat(actualRecord.getPhoneNumbers().stream().map(PhoneNumberDTO::getType).toArray()).isEqualTo(expectedRecord.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray());
        Assertions.assertThat(actualRecord.getPhoneNumbers().stream().map(PhoneNumberDTO::getNumber).toArray()).isEqualTo(expectedRecord.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray());
        Assertions.assertThat(actualRecord.getAddress().getStreetAddress()).isEqualTo(expectedRecord.getAddress().getStreetAddress());
        Assertions.assertThat(actualRecord.getAddress().getCity()).isEqualTo(expectedRecord.getAddress().getCity());
        Assertions.assertThat(actualRecord.getAddress().getState()).isEqualTo(expectedRecord.getAddress().getState());
        Assertions.assertThat(actualRecord.getAddress().getCountry()).isEqualTo(expectedRecord.getAddress().getCountry());
        Assertions.assertThat(actualRecord.getAddress().getPostalCode()).isEqualTo(expectedRecord.getAddress().getPostalCode());
    }
}
