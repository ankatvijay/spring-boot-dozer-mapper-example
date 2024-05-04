package com.spring.crud.demo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.model.emp.Address;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@DataJpaTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class EmployeeRepositoryTest implements BaseRepositoryTest<Employee> {

    @Autowired
    private EmployeeRepository employeeRepository;
    public static File file = FileLoader.getFileFromResource("employees.json");
    public static ObjectMapper objectMapper = new ObjectMapper();
    public static TypeFactory typeFactory = objectMapper.getTypeFactory();

    @BeforeEach
    void init() {
        employeeRepository.deleteAll();
    }

    @Override
    @Test
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws IOException {
        // Given
        List<Employee> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        employeeRepository.saveAll(employees);
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
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getId).toArray(),
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray(),
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()
                ))
                .toArray(Tuple[]::new);

        // When
        List<Employee> actualEmployees = employeeRepository.findAll();

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
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumber::getId).toArray(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()
                )
                .containsExactly(expectedEmployees);
    }

    @Override
    @Test
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws IOException {
        // Given
        List<Employee> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeRepository.save(employee);

        // When
        Employee actualEmployee = employeeRepository.findById(expectedEmployee.getId()).orElseGet(Employee::new);

        // Then
        assertRecord(expectedEmployee, actualEmployee);
    }

    @Override
    @Test
    public void testGivenRandomId_WhenGetRecordsById_ThenReturnEmpty() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Optional<Employee> actualEmployee = employeeRepository.findById(id);

        // Then
        Assertions.assertThat(actualEmployee).isEmpty();
    }

    @Override
    @Test
    public void testGivenId_WhenExistRecordById_ThenReturnTrue() throws IOException {
        // Given
        List<Employee> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeRepository.save(employee);

        // When
        Boolean actualEmployee = employeeRepository.existsById(expectedEmployee.getId());

        // Then
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee).isTrue();
    }

    @Override
    @Test
    public void testGivenRandomId_WhenExistRecordById_ThenReturnFalse() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Boolean actualEmployee = employeeRepository.existsById(id);

        // Then
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee).isFalse();
    }

    @Override
    @Test
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws IOException {
        // Given
        List<Employee> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeRepository.save(employee);
        Employee example = new Employee();
        expectedEmployee.setFirstName("Rahul");
        expectedEmployee.setLastName("Ghadage");
        expectedEmployee.setNoOfChildrens(0);
        expectedEmployee.setAge(28);
        expectedEmployee.setSpouse(true);

        // When
        Example<Employee> employeeExample = Example.of(example, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        List<Employee> actualEmployees = employeeRepository.findAll(employeeExample);

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees).isNotEmpty();
        Assertions.assertThat(actualEmployees.size()).isEqualTo(1);
        assertRecord(expectedEmployee, actualEmployees.get(0));
    }

    @Override
    @Test
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenReturnEmptyListRecords() {
        // Given
        Employee expectedEmployee = new Employee();
        expectedEmployee.setFirstName("Raj");
        expectedEmployee.setLastName("Kumar");
        expectedEmployee.setNoOfChildrens(3);
        expectedEmployee.setAge(60);
        expectedEmployee.setSpouse(false);

        // When
        Example<Employee> employeeExample = Example.of(expectedEmployee, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        List<Employee> actualEmployees = employeeRepository.findAll(employeeExample);

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees).isEmpty();
    }

    @ParameterizedTest
    @MethodSource(value = "generateExample")
    public void testGivenMultipleExample_WhenGetAllRecordsByExample_ThenReturnListRecord(Example<Employee> employeeExample, int count) throws IOException {
        // Given
        List<Employee> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        employeeRepository.saveAll(employees);
        Tuple[] expectedTupleEmployees = employees.stream()
                .filter(employee -> employee.getSpouse().equals(employeeExample.getProbe().getSpouse()))
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
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getId).toArray(),
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray(),
                        employee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()
                ))
                .toArray(Tuple[]::new);

        // When
        List<Employee> actualEmployees = employeeRepository.findAll(employeeExample);

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
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumber::getId).toArray(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()
                )
                .containsExactly(expectedTupleEmployees);
    }

    @Override
    @Test
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
        Employee actualEmployee = employeeRepository.save(expectedEmployee);

        // Then
        assertRecord(expectedEmployee, actualEmployee);
    }

    @Override
    @Test
    public void testGivenExistingRecordAndUpdate_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {
        // Given
        List<Employee> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeRepository.save(employee);
        expectedEmployee.setAge(50);

        // When
        Employee actualEmployee = employeeRepository.save(expectedEmployee);

        // Then
        assertRecord(expectedEmployee, actualEmployee);
    }

    @Override
    @Test
    public void testGivenIdAndUpdatedRecord_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {
        // Given
        List<Employee> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee savedEmployee = employeeRepository.save(employee);

        // When
        Employee expectedEmployee = employeeRepository.findById(savedEmployee.getId()).orElseGet(Employee::new);
        expectedEmployee.setAge(18);
        Employee actualEmployee = employeeRepository.save(expectedEmployee);

        // Then
        assertRecord(expectedEmployee, actualEmployee);
    }

    @Override
    @Test
    public void testGivenId_WhenDeleteRecord_ThenReturnFalse() throws IOException {
        // Given
        List<Employee> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeRepository.save(employee);

        // When
        employeeRepository.deleteById(expectedEmployee.getId());
        Boolean deletedEmployee = employeeRepository.existsById(expectedEmployee.getId());

        // Then
        Assertions.assertThat(deletedEmployee).isFalse();
    }

    @Override
    @Test
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        employeeRepository.deleteById(id);
        Boolean deletedEmployee = employeeRepository.existsById(id);

        // Then
        Assertions.assertThat(deletedEmployee).isFalse();
    }

    @Override
    @Test
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given
        employeeRepository.deleteAll();

        // When
        List<Employee> employees = employeeRepository.findAll();

        // Then
        Assertions.assertThat(employees).isNotNull();
        Assertions.assertThat(employees.size()).isEqualTo(0);
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
        Assertions.assertThat(actualRecord.getPhoneNumbers().stream().map(PhoneNumber::getId).toArray()).isEqualTo(expectedRecord.getPhoneNumbers().stream().map(PhoneNumber::getId).toArray());
        Assertions.assertThat(actualRecord.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray()).isEqualTo(expectedRecord.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray());
        Assertions.assertThat(actualRecord.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()).isEqualTo(expectedRecord.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray());
        Assertions.assertThat(actualRecord.getAddress().getStreetAddress()).isEqualTo(expectedRecord.getAddress().getStreetAddress());
        Assertions.assertThat(actualRecord.getAddress().getCity()).isEqualTo(expectedRecord.getAddress().getCity());
        Assertions.assertThat(actualRecord.getAddress().getState()).isEqualTo(expectedRecord.getAddress().getState());
        Assertions.assertThat(actualRecord.getAddress().getCountry()).isEqualTo(expectedRecord.getAddress().getCountry());
        Assertions.assertThat(actualRecord.getAddress().getPostalCode()).isEqualTo(expectedRecord.getAddress().getPostalCode());
    }
}