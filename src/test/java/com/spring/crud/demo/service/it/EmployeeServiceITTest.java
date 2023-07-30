package com.spring.crud.demo.service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.emp.Address;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;
import com.spring.crud.demo.service.BaseServiceTest;
import com.spring.crud.demo.service.EmployeeService;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SpringBootTest(value = "EmployeeServiceITTest")
class EmployeeServiceITTest implements BaseServiceTest<Employee> {

    @Autowired
    private EmployeeService employeeService;
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
        employeeService.deleteAllRecords();
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() {
        // Given
        employees.forEach(employee -> employeeService.insertRecord(employee));

        // When
        List<Employee> actualEmployees = employeeService.getAllRecords();

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
    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() {
        // Given
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeService.insertRecord(employee).orElseGet(Employee::new);

        // When
        Employee actualEmployee = employeeService.getRecordsById(expectedEmployee.getId()).orElseGet(Employee::new);

        // Then
        assertRecord(expectedEmployee, actualEmployee);
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Assertions.assertThatThrownBy(() -> employeeService.getRecordsById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);

        // Then
    }

    @Test
    @Override
    public void testGivenId_WhenExistRecordById_ThenReturnTrue() {
        // Given
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeService.insertRecord(employee).orElseGet(Employee::new);

        // When
        Boolean actualEmployee = employeeService.existRecordById(expectedEmployee.getId());

        // Then
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee).isTrue();
    }

    @Test
    @Override
    public void testGivenRandomId_WhenExistRecordById_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Boolean actualEmployee = employeeService.existRecordById(id);

        // Then
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee).isFalse();
    }

    @Test
    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() {
        // Given
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeService.insertRecord(employee).orElseGet(Employee::new);
        Employee employeeExample = new Employee();
        employeeExample.setFirstName("Rahul");
        employeeExample.setLastName("Ghadage");
        employeeExample.setNoOfChildrens(0);
        employeeExample.setAge(28);
        employeeExample.setSpouse(true);
        employeeExample.setAddress(null);
        employeeExample.setPhoneNumbers(null);
        employeeExample.setDateOfJoining(LocalDateTime.parse("01-01-2000 01:01:01", DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)));

        // When
        List<Employee> actualEmployees = employeeService.getAllRecordsByExample(employeeExample);

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees).isNotEmpty();
        Assertions.assertThat(actualEmployees.size()).isEqualTo(1);
        assertRecord(expectedEmployee, actualEmployees.get(0));
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenReturnEmptyListRecords() {
        // Given
        Employee expectedEmployee = new Employee();
        expectedEmployee.setFirstName("Rahul");
        expectedEmployee.setLastName("Ghadage");
        expectedEmployee.setNoOfChildrens(0);
        expectedEmployee.setAge(28);
        expectedEmployee.setSpouse(true);

        // When
        List<Employee> actualEmployees = employeeService.getAllRecordsByExample(expectedEmployee);

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees).isEmpty();
    }

    @ParameterizedTest
    @MethodSource(value = "generateExample")
    @Override
    public void testGivenMultipleExample_WhenGetAllRecordsByExample_ThenReturnListRecord(Example<Employee> example, int count) {
        // Given
        employeeService.insertBulkRecords(employees);
        List<Employee> expectedEmployeees = employees.stream()
                .filter(employee -> employee.getSpouse().equals(example.getProbe().getSpouse())).toList();
        Tuple[] expectedTupleEmployees = expectedEmployeees.stream()
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

        // When
        List<Employee> actualEmployees = employeeService.getAllRecordsByExample(example.getProbe());

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees.size()).isEqualTo(count);
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
                .containsExactly(expectedTupleEmployees);
    }

    @Test
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() {
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
        Employee actualEmployee = employeeService.insertRecord(expectedEmployee).orElseGet(Employee::new);

        // Then
        assertRecord(expectedEmployee, actualEmployee);
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() {
        // Given
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeService.insertRecord(employee).orElseGet(Employee::new);

        // When
        Assertions.assertThatThrownBy(() -> employeeService.insertRecord(expectedEmployee))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedEmployee.getId());

        // Then
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() {
        // Given
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee savedEmployee = employeeService.insertRecord(employee).orElseGet(Employee::new);

        // When
        Employee expectedEmployee = employeeService.getRecordsById(savedEmployee.getId()).orElseGet(Employee::new);
        expectedEmployee.setAge(18);
        Employee actualEmployee = employeeService.updateRecord(savedEmployee.getId(), expectedEmployee).orElseGet(Employee::new);

        // Then
        assertRecord(expectedEmployee, actualEmployee);
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Assertions.assertThatThrownBy(() -> employeeService.updateRecord(id, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Payload record id is null");
    }

    @Test
    @Override
    public void testGivenExistingRecordAndRandomId_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee savedEmployee = employeeService.insertRecord(employee).orElseGet(Employee::new);

        // When & Then
        Employee expectedEmployee = employeeService.getRecordsById(savedEmployee.getId()).orElseGet(Employee::new);
        expectedEmployee.setAge(18);
        Assertions.assertThatThrownBy(() -> employeeService.updateRecord(id, expectedEmployee))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedEmployee.getId());
    }

    @Test
    @Override
    public void testGivenRecordIdAndRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(25);

        // When & Then
        Assertions.assertThatThrownBy(() -> employeeService.updateRecord(expectedEmployee.getId(), expectedEmployee))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedEmployee.getId());
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() {
        // Given
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee savedEmployee = employeeService.insertRecord(employee).orElseGet(Employee::new);

        // When
        Boolean flag = employeeService.deleteRecordById(savedEmployee.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Boolean flag = employeeService.deleteRecordById(id);

        // Then
        Assertions.assertThat(flag).isFalse();
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given & When
        employeeService.deleteAllRecords();
        List<Employee> actualEmployees = employeeService.getAllRecords();

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees).isEmpty();
    }

    private static Stream<Arguments> generateExample() {
        Employee canFlyEmployees = new Employee();
        canFlyEmployees.setSpouse(true);

        Employee cannotFlyEmployees = new Employee();
        cannotFlyEmployees.setSpouse(false);

        return Stream.of(
                Arguments.of(Example.of(canFlyEmployees, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 1),
                Arguments.of(Example.of(cannotFlyEmployees, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 1)
        );
    }

    public void assertRecord(Employee expectedRecord, Employee actualRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getFirstName()).isEqualTo(expectedRecord.getFirstName());
        Assertions.assertThat(actualRecord.getLastName()).isEqualTo(expectedRecord.getLastName());
        Assertions.assertThat(actualRecord.getAge()).isEqualTo(expectedRecord.getAge());
        Assertions.assertThat(actualRecord.getNoOfChildrens()).isEqualTo(expectedRecord.getNoOfChildrens());
        Assertions.assertThat(actualRecord.getSpouse()).isEqualTo(expectedRecord.getSpouse());
        Assertions.assertThat(actualRecord.getDateOfJoining()).isEqualTo(expectedRecord.getDateOfJoining());
        Assertions.assertThat(actualRecord.getHobbies().toArray()).isEqualTo(expectedRecord.getHobbies().toArray());
        Assertions.assertThat(actualRecord.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray()).isEqualTo(expectedRecord.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray());
        Assertions.assertThat(actualRecord.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()).isEqualTo(expectedRecord.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray());
        Assertions.assertThat(actualRecord.getAddress().getStreetAddress()).isEqualTo(expectedRecord.getAddress().getStreetAddress());
        Assertions.assertThat(actualRecord.getAddress().getCity()).isEqualTo(expectedRecord.getAddress().getCity());
        Assertions.assertThat(actualRecord.getAddress().getState()).isEqualTo(expectedRecord.getAddress().getState());
        Assertions.assertThat(actualRecord.getAddress().getCountry()).isEqualTo(expectedRecord.getAddress().getCountry());
        Assertions.assertThat(actualRecord.getAddress().getPostalCode()).isEqualTo(expectedRecord.getAddress().getPostalCode());
    }
}