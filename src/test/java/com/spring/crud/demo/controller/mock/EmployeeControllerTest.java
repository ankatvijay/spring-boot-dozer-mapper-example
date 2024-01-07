package com.spring.crud.demo.controller.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.controller.BaseControllerTest;
import com.spring.crud.demo.controller.EmployeeController;
import com.spring.crud.demo.dto.ResponseDTO;
import com.spring.crud.demo.dto.emp.EmployeeDTO;
import com.spring.crud.demo.dto.emp.PhoneNumberDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.mapper.EmployeeMapper;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;
import com.spring.crud.demo.service.EmployeeService;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest implements BaseControllerTest<Employee, EmployeeDTO> {

    private static File file;
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;
    private static EmployeeMapper employeeMapper;
    private static EmployeeService employeeService;
    private static EmployeeController employeeController;

    @BeforeAll
    static void init() {
        objectMapper = new ObjectMapper();
        typeFactory = objectMapper.getTypeFactory();
        file = FileLoader.getFileFromResource("employees.json");

        employeeMapper = Mockito.mock(EmployeeMapper.class);
        employeeService = Mockito.mock(EmployeeService.class);
        employeeController = new EmployeeController(employeeService, employeeMapper, objectMapper);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws IOException {
        // Given
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Tuple[] expectedEmployees = employeees.stream()
                .map(employee -> AssertionsForClassTypes.tuple(
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getAge(),
                        employee.getNoOfChildrens(),
                        employee.getSpouse(),
                        employee.getDateOfJoining().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)),
                        employee.getHobbies().toArray(),
                        employee.getAddress().getStreetAddress(),
                        employee.getAddress().getCity(),
                        employee.getAddress().getState(),
                        employee.getAddress().getCountry(),
                        employee.getAddress().getPostalCode(),
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getId).toArray(),
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray(),
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()
                ))
                .toArray(Tuple[]::new);

        // When
        Mockito.when(employeeService.getAllRecords()).thenReturn(employeees);
        employeees.forEach(employee -> Mockito.when(employeeMapper.convertFromEntityToDto(employee)).thenReturn(objectMapper.convertValue(employee, EmployeeDTO.class)));
        ResponseEntity<List<EmployeeDTO>> actualEmployees = employeeController.getAllRecords();

        // Then
        Assertions.assertThat(actualEmployees.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualEmployees.getBody()).isNotNull();
        Assertions.assertThat(actualEmployees.getBody().size()).isGreaterThan(0);
        Assertions.assertThat(actualEmployees.getBody())
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
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumberDTO::getId).toArray(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumberDTO::getType).toArray(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumberDTO::getNumber).toArray()
                )
                .containsExactly(expectedEmployees);
        Mockito.verify(employeeService, Mockito.atLeastOnce()).getAllRecords();
        employeees.forEach(employee -> Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromEntityToDto(employee));
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenThrowException() {
        // Given
        List<Employee> employeeList = new ArrayList<>();

        // When & Then
        Mockito.when(employeeService.getAllRecords()).thenReturn(employeeList);
        Assertions.assertThatThrownBy(() -> employeeController.getAllRecords())
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found");
        Mockito.verify(employeeService, Mockito.atLeastOnce()).getAllRecords();
    }

    @Test
    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws IOException {
        // Given
        int id = 12;
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee expectedEmployee = employeees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);

        // When
        Mockito.when(employeeService.getRecordsById(id)).thenReturn(Optional.of(expectedEmployee));
        Mockito.when(employeeMapper.convertFromEntityToDto(expectedEmployee)).thenReturn(objectMapper.convertValue(expectedEmployee, EmployeeDTO.class));
        ResponseEntity<EmployeeDTO> actualEmployee = employeeController.getRecordsById(id);

        // Then
        Assertions.assertThat(actualEmployee.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualEmployee.getBody()).isNotNull();
        assertRecord(expectedEmployee, actualEmployee.getBody());
        Mockito.verify(employeeService).getRecordsById(id);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromEntityToDto(expectedEmployee);
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(employeeService.getRecordsById(id)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> employeeController.getRecordsById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);
    }

    @Test
    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws IOException {
        // Given
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee expectedEmployee = employeees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        EmployeeDTO map = new ObjectMapper().convertValue(expectedEmployee, EmployeeDTO.class);

        // When
        Mockito.when(employeeService.getAllRecordsByExample(expectedEmployee)).thenReturn(List.of(expectedEmployee));
        Mockito.when(employeeMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedEmployee);
        Mockito.when(employeeMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(map, EmployeeDTO.class));
        ResponseEntity<List<EmployeeDTO>> actualEmployees = employeeController.getAllRecordsByExample(map);

        // Then
        Assertions.assertThat(actualEmployees.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualEmployees.getBody()).isNotNull();
        Assertions.assertThat(actualEmployees.getBody().size()).isGreaterThan(0);
        assertRecord(expectedEmployee, actualEmployees.getBody().get(0));
        Mockito.verify(employeeService).getAllRecordsByExample(expectedEmployee);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenThrowException() {
        // Given
        Employee expectedEmployee = new Employee();
        expectedEmployee.setFirstName("Rahul");
        expectedEmployee.setLastName("Ghadage");
        expectedEmployee.setNoOfChildrens(0);
        expectedEmployee.setAge(28);
        expectedEmployee.setSpouse(true);
        EmployeeDTO map = objectMapper.convertValue(expectedEmployee, EmployeeDTO.class);
        List<Employee> employeees = new ArrayList<>();

        // When & Then
        Mockito.when(employeeService.getAllRecordsByExample(expectedEmployee)).thenReturn(employeees);
        Mockito.when(employeeMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedEmployee);
        Assertions.assertThatThrownBy(() -> employeeController.getAllRecordsByExample(map))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with map " + map);


        Mockito.verify(employeeService).getAllRecordsByExample(expectedEmployee);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() throws IOException {
        // Given
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee expectedEmployee = employeees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);

        // When
        Mockito.when(employeeService.insertRecord(expectedEmployee)).thenReturn(Optional.of(expectedEmployee));
        Mockito.when(employeeMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedEmployee);
        Mockito.when(employeeMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(expectedEmployee, EmployeeDTO.class));
        ResponseEntity<EmployeeDTO> actualEmployee = employeeController.insertRecord(objectMapper.convertValue(expectedEmployee, EmployeeDTO.class));

        // Then
        Assertions.assertThat(actualEmployee.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(actualEmployee.getBody()).isNotNull();
        assertRecord(expectedEmployee, actualEmployee.getBody());
        Mockito.verify(employeeService).insertRecord(expectedEmployee);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() throws IOException {
        // Given
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee expectedEmployee = employeees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeService.insertRecord(expectedEmployee)).thenReturn(Optional.empty());
        Mockito.when(employeeMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedEmployee);
        Assertions.assertThatThrownBy(() -> employeeController.insertRecord(objectMapper.convertValue(expectedEmployee, EmployeeDTO.class)))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Something went wrong");

        // Then
        Mockito.verify(employeeService).insertRecord(expectedEmployee);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {
        // Given
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee expectedEmployee = employeees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeService.updateRecord(expectedEmployee.getId(), expectedEmployee)).thenReturn(Optional.of(expectedEmployee));
        Mockito.when(employeeMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedEmployee);
        Mockito.when(employeeMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(expectedEmployee, EmployeeDTO.class));
        ResponseEntity<EmployeeDTO> actualEmployee = employeeController.updateRecord(expectedEmployee.getId(), objectMapper.convertValue(expectedEmployee, EmployeeDTO.class));

        // Then
        Assertions.assertThat(actualEmployee.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(actualEmployee.getBody()).isNotNull();
        assertRecord(expectedEmployee, actualEmployee.getBody());
        Mockito.verify(employeeService).updateRecord(expectedEmployee.getId(), expectedEmployee);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(employeeService.updateRecord(id, null)).thenReturn(Optional.empty());
        Mockito.when(employeeMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(null);
        Assertions.assertThatThrownBy(() -> employeeController.updateRecord(id, null))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Something went wrong");
        Mockito.verify(employeeService).updateRecord(id, null);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() throws IOException {
        // Given
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee expectedEmployee = employeees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeService.deleteRecordById(expectedEmployee.getId())).thenReturn(true);
        ResponseEntity<ResponseDTO> response = employeeController.deleteRecordById(expectedEmployee.getId());

        // Then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().message()).isEqualTo("Record deleted with id " + expectedEmployee.getId());
        Mockito.verify(employeeService).deleteRecordById(expectedEmployee.getId());
    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(employeeService.deleteRecordById(id)).thenReturn(false);
        Assertions.assertThatThrownBy(() -> employeeController.deleteRecordById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);

        // Then
        Mockito.verify(employeeService).deleteRecordById(id);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given & When
        Mockito.doNothing().when(employeeService).deleteAllRecords();

        // Then
        ResponseEntity<Void> response = employeeController.deleteAllRecords();

        // Then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

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