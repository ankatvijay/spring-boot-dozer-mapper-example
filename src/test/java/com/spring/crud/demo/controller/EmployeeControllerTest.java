package com.spring.crud.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.dto.emp.EmployeeDTO;
import com.spring.crud.demo.dto.emp.PhoneNumberDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.mapper.emp.EmployeeMapper;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;
import com.spring.crud.demo.service.IEmployeeService;
import com.spring.crud.demo.service.impl.EmployeeService;
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
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    private static File file;
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;
    private static EmployeeMapper employeeMapper;
    private static IEmployeeService employeeService;
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
    void testGivenNon_WhenFindAllEmployees_ThenReturnAllRecord() throws IOException {
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
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray(),
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()
                ))
                .toArray(Tuple[]::new);

        // When
        Mockito.when(employeeService.findAllEmployees()).thenReturn(employeees);
        employeees.forEach(employee -> Mockito.when(employeeMapper.convertFromEntityToDto(employee)).thenReturn(objectMapper.convertValue(employee, EmployeeDTO.class)));
        ResponseEntity<List<EmployeeDTO>> actualEmployees = employeeController.findAllEmployees();

        // Then
        Assertions.assertThat(actualEmployees.getStatusCode()).isEqualTo(HttpStatus.FOUND);
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
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumberDTO::getType).toArray(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumberDTO::getNumber).toArray()
                )
                .containsExactly(expectedEmployees);
        Mockito.verify(employeeService, Mockito.atLeastOnce()).findAllEmployees();
        employeees.forEach(employee -> Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromEntityToDto(employee));
    }

    @Test
    void testGivenNon_WhenFindAllEmployees_ThenReturnError() {
        // Given
        List<Employee> employeeList = new ArrayList<>();

        // When & Then
        Mockito.when(employeeService.findAllEmployees()).thenReturn(employeeList);
        Assertions.assertThatThrownBy(() -> employeeController.findAllEmployees())
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found");
        Mockito.verify(employeeService, Mockito.atLeastOnce()).findAllEmployees();
    }

    @Test
    void testGivenId_WhenFindEmployeeById_ThenReturnRecord() throws IOException {
        // Given
        int id = 12;
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee expectedEmployee = employeees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);

        // When
        Mockito.when(employeeService.findEmployeeById(id)).thenReturn(Optional.of(expectedEmployee));
        Mockito.when(employeeMapper.convertFromEntityToDto(expectedEmployee)).thenReturn(objectMapper.convertValue(expectedEmployee, EmployeeDTO.class));
        ResponseEntity<EmployeeDTO> actualEmployee = employeeController.findEmployeeById(id);

        // Then
        Assertions.assertThat(actualEmployee.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        Assertions.assertThat(actualEmployee.getBody()).isNotNull();
        assertEmployee(expectedEmployee, actualEmployee.getBody());
        Mockito.verify(employeeService).findEmployeeById(id);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromEntityToDto(expectedEmployee);
    }

    @Test
    void testGivenRandomId_WhenFindEmployeeById_ThenReturnRecord() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(employeeService.findEmployeeById(id)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> employeeController.findEmployeeById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);
    }

    @Test
    void testGivenEmployee_WhenFindEmployeesByExample_ThenReturnRecords() throws IOException {
        // Given
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee expectedEmployee = employeees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Map<String, Object> map = new ObjectMapper().convertValue(expectedEmployee, Map.class);

        // When
        Mockito.when(employeeService.findEmployeesByExample(expectedEmployee)).thenReturn(List.of(expectedEmployee));
        Mockito.when(employeeMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedEmployee);
        Mockito.when(employeeMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(map, EmployeeDTO.class));
        ResponseEntity<List<EmployeeDTO>> actualEmployees = employeeController.findEmployeesByExample(map);

        // Then
        Assertions.assertThat(actualEmployees.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        Assertions.assertThat(actualEmployees.getBody()).isNotNull();
        Assertions.assertThat(actualEmployees.getBody().size()).isGreaterThan(0);
        assertEmployee(expectedEmployee, actualEmployees.getBody().get(0));
        Mockito.verify(employeeService).findEmployeesByExample(expectedEmployee);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    void testGivenRandomEmployee_WhenFindEmployeesByExample_ThenReturnError() {
        // Given
        Employee expectedEmployee = new Employee();
        expectedEmployee.setFirstName("Rahul");
        expectedEmployee.setLastName("Ghadage");
        expectedEmployee.setNoOfChildrens(0);
        expectedEmployee.setAge(28);
        expectedEmployee.setSpouse(true);
        Map<String, Object> map = objectMapper.convertValue(expectedEmployee, Map.class);
        List<Employee> employeees = new ArrayList<>();

        // When & Then
        Mockito.when(employeeService.findEmployeesByExample(expectedEmployee)).thenReturn(employeees);
        Mockito.when(employeeMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedEmployee);
        Assertions.assertThatThrownBy(() -> employeeController.findEmployeesByExample(map))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with map " + map);


        Mockito.verify(employeeService).findEmployeesByExample(expectedEmployee);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    void testGivenEmployee_WhenSaveEmployee_ThenReturnNewEmployee() throws IOException {
        // Given
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee expectedEmployee = employeees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);

        // When
        Mockito.when(employeeService.saveEmployee(expectedEmployee)).thenReturn(Optional.of(expectedEmployee));
        Mockito.when(employeeMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedEmployee);
        Mockito.when(employeeMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(expectedEmployee, EmployeeDTO.class));
        ResponseEntity<EmployeeDTO> actualEmployee = employeeController.saveEmployee(objectMapper.convertValue(expectedEmployee, EmployeeDTO.class));

        // Then
        Assertions.assertThat(actualEmployee.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(actualEmployee.getBody()).isNotNull();
        assertEmployee(expectedEmployee, actualEmployee.getBody());
        Mockito.verify(employeeService).saveEmployee(expectedEmployee);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    void testGivenExistingEmployee_WhenSaveEmployee_ThenThrowError() throws IOException {
        // Given
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee expectedEmployee = employeees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeService.saveEmployee(expectedEmployee)).thenReturn(Optional.empty());
        Mockito.when(employeeMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedEmployee);
        Assertions.assertThatThrownBy(() -> employeeController.saveEmployee(objectMapper.convertValue(expectedEmployee, EmployeeDTO.class)))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Something went wrong");

        // Then
        Mockito.verify(employeeService).saveEmployee(expectedEmployee);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    void testGivenExistingEmployee_WhenUpdateEmployee_ThenReturnUpdatedEmployee() throws IOException {
        // Given
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee expectedEmployee = employeees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeService.updateEmployee(expectedEmployee.getId(), expectedEmployee)).thenReturn(Optional.of(expectedEmployee));
        Mockito.when(employeeMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedEmployee);
        Mockito.when(employeeMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(expectedEmployee, EmployeeDTO.class));
        ResponseEntity<EmployeeDTO> actualEmployee = employeeController.updateEmployee(expectedEmployee.getId(), objectMapper.convertValue(expectedEmployee, EmployeeDTO.class));

        // Then
        Assertions.assertThat(actualEmployee.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(actualEmployee.getBody()).isNotNull();
        assertEmployee(expectedEmployee, actualEmployee.getBody());
        Mockito.verify(employeeService).updateEmployee(expectedEmployee.getId(), expectedEmployee);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    void testGivenNull_WhenUpdateEmployee_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(employeeService.updateEmployee(id, null)).thenReturn(Optional.empty());
        Mockito.when(employeeMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(null);
        Assertions.assertThatThrownBy(() -> employeeController.updateEmployee(id, null))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Something went wrong");
        Mockito.verify(employeeService).updateEmployee(id, null);
        Mockito.verify(employeeMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    void testGiveId_WhenDeleteEmployee_ThenReturnTrue() throws IOException {
        // Given
        List<Employee> employeees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee expectedEmployee = employeees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeService.deleteEmployee(expectedEmployee.getId())).thenReturn(true);
        ResponseEntity<Boolean> flag = employeeController.deleteEmployee(expectedEmployee.getId());

        // Then
        Assertions.assertThat(flag.getBody()).isTrue();
        Mockito.verify(employeeService).deleteEmployee(expectedEmployee.getId());
    }

    @Test
    void testGiveRandomId_WhenDeleteEmployee_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(employeeService.deleteEmployee(id)).thenReturn(false);
        ResponseEntity<Boolean> flag = employeeController.deleteEmployee(id);

        // Then
        Assertions.assertThat(flag.getBody()).isFalse();
        Mockito.verify(employeeService).deleteEmployee(id);
    }


    private void assertEmployee(Employee expectedEmployee, EmployeeDTO actualEmployee) {
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee.getFirstName()).isEqualTo(expectedEmployee.getFirstName());
        Assertions.assertThat(actualEmployee.getLastName()).isEqualTo(expectedEmployee.getLastName());
        Assertions.assertThat(actualEmployee.getAge()).isEqualTo(expectedEmployee.getAge());
        Assertions.assertThat(actualEmployee.getNoOfChildrens()).isEqualTo(expectedEmployee.getNoOfChildrens());
        Assertions.assertThat(actualEmployee.getSpouse()).isEqualTo(expectedEmployee.getSpouse());
        Assertions.assertThat(actualEmployee.getDateOfJoining()).isEqualTo(expectedEmployee.getDateOfJoining().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)));
        Assertions.assertThat(actualEmployee.getHobbies().toArray()).isEqualTo(expectedEmployee.getHobbies().toArray());
        Assertions.assertThat(actualEmployee.getPhoneNumbers().stream().map(PhoneNumberDTO::getType).toArray()).isEqualTo(expectedEmployee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray());
        Assertions.assertThat(actualEmployee.getPhoneNumbers().stream().map(PhoneNumberDTO::getNumber).toArray()).isEqualTo(expectedEmployee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray());
        Assertions.assertThat(actualEmployee.getAddress().getStreetAddress()).isEqualTo(expectedEmployee.getAddress().getStreetAddress());
        Assertions.assertThat(actualEmployee.getAddress().getCity()).isEqualTo(expectedEmployee.getAddress().getCity());
        Assertions.assertThat(actualEmployee.getAddress().getState()).isEqualTo(expectedEmployee.getAddress().getState());
        Assertions.assertThat(actualEmployee.getAddress().getCountry()).isEqualTo(expectedEmployee.getAddress().getCountry());
        Assertions.assertThat(actualEmployee.getAddress().getPostalCode()).isEqualTo(expectedEmployee.getAddress().getPostalCode());
    }
}