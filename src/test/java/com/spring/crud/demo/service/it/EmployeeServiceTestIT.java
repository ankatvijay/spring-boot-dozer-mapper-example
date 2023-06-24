package com.spring.crud.demo.service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.emp.Address;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;
import com.spring.crud.demo.service.IEmployeeService;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@SpringBootTest(value = "EmployeeServiceTestIT")
class EmployeeServiceTestIT {

    @Autowired
    private IEmployeeService employeeService;
    private static Tuple[] expectedEmployees = null;
    private static List<Employee> employees;

    @BeforeAll
    static void initOnce() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        File file = FileLoader.getFileFromResource("employees.json");
        employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        expectedEmployees = employees.stream()
                .map(employee -> AssertionsForClassTypes.tuple(employee.getFirstName(),
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
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray(),
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()
                ))
                .toArray(Tuple[]::new);
    }

    @BeforeEach
    void init() {
        employeeService.deleteAllEmployee();
    }

    @Test
    void testGivenNon_WhenFindAllEmployees_ThenReturnAllRecord() {
        // Given
        employees.forEach(employee -> employeeService.saveEmployee(employee));

        // When
        List<Employee> actualEmployees = employeeService.findAllEmployees();

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees.size()).isGreaterThan(0);
        Assertions.assertThat(actualEmployees)
                .extracting(Employee::getFirstName,
                        Employee::getLastName,
                        Employee::getAge,
                        Employee::getNoOfChildrens,
                        Employee::getSpouse,
                        Employee::getDateOfJoining,
                        employee -> employee.getHobbies().toArray(),
                        employee -> employee.getAddress().getStreetAddress(),
                        employee -> employee.getAddress().getCity(),
                        employee -> employee.getAddress().getState(),
                        employee -> employee.getAddress().getCountry(),
                        employee -> employee.getAddress().getPostalCode(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()
                )
                .containsExactly(expectedEmployees);
    }

    @Test
    void testGivenId_WhenFindEmployeeById_ThenReturnRecord() {
        // Given
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeService.saveEmployee(employee).orElseGet(Employee::new);

        // When
        Employee actualEmployee = employeeService.findEmployeeById(expectedEmployee.getId()).orElseGet(Employee::new);

        // Then
        assertEmployee(expectedEmployee, actualEmployee);
    }

    @Test
    void testGivenRandomId_WhenFindEmployeeById_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Assertions.assertThatThrownBy(() -> employeeService.findEmployeeById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);

        // Then
    }

    @Test
    void testGivenEmployee_WhenFindEmployeesByExample_ThenReturnRecords() {
        // Given
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeService.saveEmployee(employee).orElseGet(Employee::new);
        Employee employeeExample = new Employee();
        employeeExample.setFirstName("Rahul");
        employeeExample.setLastName("Ghadage");
        employeeExample.setNoOfChildrens(0);
        employeeExample.setAge(28);
        employeeExample.setSpouse(true);
        employeeExample.setAddress(null);
        employeeExample.setPhoneNumbers(null);
        //employeeExample.setDateOfJoining(LocalDateTime.parse("01-01-2000 01:01:01", DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)));

        // When
        List<Employee> actualEmployees = employeeService.findEmployeesByExample(expectedEmployee);

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees).isNotEmpty();
        Assertions.assertThat(actualEmployees.size()).isEqualTo(1);
        assertEmployee(expectedEmployee, actualEmployees.get(0));
    }

    @Test
    void testGivenRandomEmployee_WhenFindEmployeesByExample_ThenReturnRecords() {
        // Given
        Employee expectedEmployee = new Employee();
        expectedEmployee.setFirstName("Rahul");
        expectedEmployee.setLastName("Ghadage");
        expectedEmployee.setNoOfChildrens(0);
        expectedEmployee.setAge(28);
        expectedEmployee.setSpouse(true);

        // When
        List<Employee> actualEmployees = employeeService.findEmployeesByExample(expectedEmployee);

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees).isEmpty();
        Assertions.assertThat(actualEmployees.size()).isEqualTo(0);
    }

    @Test
    void testGivenEmployee_WhenSaveEmployee_ThenReturnNewEmployee() {
        // Given
        PhoneNumber expectedPhoneNumber = new PhoneNumber();
        expectedPhoneNumber.setType("Mobile");
        expectedPhoneNumber.setNumber("1234567890");

        Address expectedAddress = new Address();
        expectedAddress.setStreetAddress("SV road");
        expectedAddress.setCity("Mumbai");
        expectedAddress.setState("Maharashtra");
        expectedAddress.setCountry("India");
        expectedAddress.setPostalCode("400001");

        Employee expectedEmployee = new Employee();
        expectedEmployee.setFirstName("Natasha");
        expectedEmployee.setLastName("Black Widow");
        expectedEmployee.setNoOfChildrens(1);
        expectedEmployee.setAge(35);
        expectedEmployee.setSpouse(false);
        expectedEmployee.setHobbies(Arrays.asList("Running", "Fighting"));
        expectedEmployee.setDateOfJoining(LocalDateTime.parse("01-01-2000 01:01:01", DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)));
        expectedEmployee.setPhoneNumbers(List.of(expectedPhoneNumber));
        expectedEmployee.setAddress(expectedAddress);

        expectedAddress.setEmployee(expectedEmployee);
        expectedPhoneNumber.setEmployee(expectedEmployee);

        // When
        Employee actualEmployee = employeeService.saveEmployee(expectedEmployee).orElseGet(Employee::new);

        // Then
        assertEmployee(expectedEmployee, actualEmployee);
    }

    @Test
    void testGivenExistingEmployee_WhenSaveEmployee_ThenThrowError() {
        // Given
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeService.saveEmployee(employee).orElseGet(Employee::new);

        // When
        Assertions.assertThatThrownBy(() -> employeeService.saveEmployee(expectedEmployee))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedEmployee.getId());

        // Then
    }

    @Test
    void testGivenExistingEmployee_WhenUpdateEmployee_ThenReturnUpdatedEmployee() {
        // Given
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee savedEmployee = employeeService.saveEmployee(employee).orElseGet(Employee::new);

        // When
        Employee expectedEmployee = employeeService.findEmployeeById(savedEmployee.getId()).orElseGet(Employee::new);
        expectedEmployee.setAge(18);
        Employee actualEmployee = employeeService.updateEmployee(savedEmployee.getId(), expectedEmployee).orElseGet(Employee::new);

        // Then
        assertEmployee(expectedEmployee, actualEmployee);
    }

    @Test
    void testGivenNull_WhenUpdateEmployee_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Assertions.assertThatThrownBy(() -> employeeService.updateEmployee(id, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Payload record id is null");
    }

    @Test
    void testGivenEmployeeAndIdDifferent_WhenUpdateEmployee_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee savedEmployee = employeeService.saveEmployee(employee).orElseGet(Employee::new);

        // When & Then
        Employee expectedEmployee = employeeService.findEmployeeById(savedEmployee.getId()).orElseGet(Employee::new);
        expectedEmployee.setAge(18);
        Assertions.assertThatThrownBy(() -> employeeService.updateEmployee(id, expectedEmployee))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedEmployee.getId());
    }

    @Test
    void testGivenEmployeeAndId_WhenUpdateEmployee_ThenThrowError() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(25);

        // When & Then
        Assertions.assertThatThrownBy(() -> employeeService.updateEmployee(expectedEmployee.getId(), expectedEmployee))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedEmployee.getId());
    }

    @Test
    void testGiveId_WhenDeleteEmployee_ThenReturnTrue() {
        // Given
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee savedEmployee = employeeService.saveEmployee(employee).orElseGet(Employee::new);

        // When
        Boolean flag = employeeService.deleteEmployee(savedEmployee.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
    }

    @Test
    void testGiveRandomId_WhenDeleteEmployee_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Boolean flag = employeeService.deleteEmployee(id);

        // Then
        Assertions.assertThat(flag).isFalse();
    }

    private void assertEmployee(Employee expectedEmployee, Employee actualEmployee) {
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee.getFirstName()).isEqualTo(expectedEmployee.getFirstName());
        Assertions.assertThat(actualEmployee.getLastName()).isEqualTo(expectedEmployee.getLastName());
        Assertions.assertThat(actualEmployee.getAge()).isEqualTo(expectedEmployee.getAge());
        Assertions.assertThat(actualEmployee.getNoOfChildrens()).isEqualTo(expectedEmployee.getNoOfChildrens());
        Assertions.assertThat(actualEmployee.getSpouse()).isEqualTo(expectedEmployee.getSpouse());
        Assertions.assertThat(actualEmployee.getDateOfJoining()).isEqualTo(expectedEmployee.getDateOfJoining());
        Assertions.assertThat(actualEmployee.getHobbies().toArray()).isEqualTo(expectedEmployee.getHobbies().toArray());
        Assertions.assertThat(actualEmployee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray()).isEqualTo(expectedEmployee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray());
        Assertions.assertThat(actualEmployee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()).isEqualTo(expectedEmployee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray());
        Assertions.assertThat(actualEmployee.getAddress().getStreetAddress()).isEqualTo(expectedEmployee.getAddress().getStreetAddress());
        Assertions.assertThat(actualEmployee.getAddress().getCity()).isEqualTo(expectedEmployee.getAddress().getCity());
        Assertions.assertThat(actualEmployee.getAddress().getState()).isEqualTo(expectedEmployee.getAddress().getState());
        Assertions.assertThat(actualEmployee.getAddress().getCountry()).isEqualTo(expectedEmployee.getAddress().getCountry());
        Assertions.assertThat(actualEmployee.getAddress().getPostalCode()).isEqualTo(expectedEmployee.getAddress().getPostalCode());
    }
}