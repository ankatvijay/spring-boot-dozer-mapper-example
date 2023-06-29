package com.spring.crud.demo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.model.emp.Address;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import com.spring.crud.demo.utils.HelperUtil;
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@DataJpaTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;
    public static File file = FileLoader.getFileFromResource("employees.json");
    public static ObjectMapper objectMapper = new ObjectMapper();
    public static TypeFactory typeFactory = objectMapper.getTypeFactory();

    @BeforeEach
    void init() {
        employeeRepository.deleteAll();
    }

    @Test
    void testGivenNon_WhenFindAll_ThenReturnAllRecord() throws IOException {
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
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()
                )
                .containsExactly(expectedEmployees);
    }

    @Test
    void testGivenId_WhenFindById_ThenReturnRecord() throws IOException {
        // Given
        List<Employee> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee expectedEmployee = employeeRepository.save(employee);

        // When
        Employee actualEmployee = employeeRepository.findById(expectedEmployee.getId()).orElseGet(Employee::new);

        // Then
        assertEmployee(expectedEmployee, actualEmployee);
    }

    @Test
    void testGivenId_WhenExistsById_ThenReturnRecord() throws IOException {
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

    @Test
    void testGivenRandomId_WhenExistsById_ThenReturnRecord() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Boolean actualEmployee = employeeRepository.existsById(id);

        // Then
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee).isFalse();
    }

    @Test
    void testGivenExample_WhenFindByExample_ThenReturn1Record() throws IOException {
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
        assertEmployee(expectedEmployee, actualEmployees.get(0));
    }


    @ParameterizedTest
    @MethodSource(value = "generateExample")
    void testGivenExample_WhenFindByExample_ThenReturn2Record(Example<Employee> employeeExample, int count) throws IOException {
        // Given
        List<Employee> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        employeeRepository.saveAll(employees);
        Tuple[] expectedTupleEmployees = HelperUtil.employeeSupplier.get().stream()
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
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray(),
                        employee -> employee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()
                )
                .containsExactly(expectedTupleEmployees);
    }

    @Test
    void test_saveGivenEmployee_WhenSave_ThenReturnEmployee() {
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
        assertEmployee(expectedEmployee, actualEmployee);
    }

    @Test
    void testGivenId_WhenDeleteRecord_ThenReturnTrue() throws IOException {
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

    @Test
    void testGivenId_WhenEditRecord_ThenReturnEditedRecord() throws IOException {
        // Given
        List<Employee> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        Employee employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        Employee savedEmployee = employeeRepository.save(employee);

        // When
        Employee expectedEmployee = employeeRepository.findById(savedEmployee.getId()).orElseGet(Employee::new);
        expectedEmployee.setAge(18);
        Employee actualEmployee = employeeRepository.save(expectedEmployee);

        // Then
        assertEmployee(expectedEmployee, actualEmployee);
    }

    @Test
    void testGivenNon_WhenFindAll_ThenReturnEmptyRecord() {
        // Given
        employeeRepository.deleteAll();

        // When
        List<Employee> employees = employeeRepository.findAll();

        // Then
        Assertions.assertThat(employees).isNotNull();
        Assertions.assertThat(employees.size()).isEqualTo(0);
    }

    @Test
    void testGivenId_WhenDeleteId_ThenThrowException() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(EmptyResultDataAccessException.class, () -> employeeRepository.deleteById(id));

        // Then
        Assertions.assertThat(exception).isInstanceOf(EmptyResultDataAccessException.class);
        Assertions.assertThat(exception.getMessage()).isEqualTo(String.format("No class com.spring.crud.demo.model.emp.Employee entity with id %d exists!", id));
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