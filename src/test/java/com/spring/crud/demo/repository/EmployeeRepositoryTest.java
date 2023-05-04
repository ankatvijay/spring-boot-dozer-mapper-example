package com.spring.crud.demo.repository;

import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;
import com.spring.crud.demo.utils.HelperUtil;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@DataJpaTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;
    private static Tuple[] expectedEmployees = null;

    @BeforeAll
    static void init() {
        expectedEmployees = HelperUtil.employeeSupplier.get().stream()
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

    @Test
    void testGivenNon_WhenFindAll_ThenReturnAllRecord() {
        // Given

        // When
        List<Employee> employees = employeeRepository.findAll();

        // Then
        Assertions.assertThat(employees).isNotNull();
        Assertions.assertThat(employees.size()).isGreaterThan(0);
        Assertions.assertThat(employees)
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
    void testGivenId_WhenFindById_ThenReturnRecord() {
        // Given
        Optional<Employee> optionalRahulGhadage = employeeRepository.findAll().stream().filter(employee -> employee.getFirstName().equals("Rahul") && employee.getLastName().equals("Ghadage")).findFirst();
        Employee expectedRahulGhadage = optionalRahulGhadage.orElseGet(() -> Employee.builder().build());

        // When
        Optional<Employee> actualEmployee = employeeRepository.findById(expectedRahulGhadage.getId());

        // Then
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee).isNotEmpty();
        Assertions.assertThat(actualEmployee.get()).isEqualTo(expectedRahulGhadage);
    }

    @Test
    void testGivenId_WhenExistsById_ThenReturnRecord() {
        // Given
        Optional<Employee> optionalRahulGhadage = employeeRepository.findAll().stream().filter(employee -> employee.getFirstName().equals("Rahul") && employee.getLastName().equals("Ghadage")).findFirst();
        Employee expectedRahulGhadage = optionalRahulGhadage.orElseGet(() -> Employee.builder().build());

        // When
        Boolean actualEmployee = employeeRepository.existsById(expectedRahulGhadage.getId());

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
    void testGivenExample_WhenFindByExample_ThenReturn1Record() {
        // Given
        Optional<Employee> optionalRahulGhadage = HelperUtil.employeeSupplier.get().stream().filter(employee -> employee.getFirstName().equals("Rahul") && employee.getLastName().equals("Ghadage")).findFirst();
        Employee exampleEmployee = optionalRahulGhadage.orElseGet(() -> Employee.builder().build());
        exampleEmployee.setId(null);
        exampleEmployee.setAddress(null);

        // When
        Example<Employee> employeeExample = Example.of(exampleEmployee, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        List<Employee> actualEmployees = employeeRepository.findAll(employeeExample);

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees.size()).isEqualTo(1);
        Assertions.assertThat(actualEmployees.get(0).getFirstName()).isEqualTo(exampleEmployee.getFirstName());
        Assertions.assertThat(actualEmployees.get(0).getLastName()).isEqualTo(exampleEmployee.getLastName());
        Assertions.assertThat(actualEmployees.get(0).getAge()).isEqualTo(exampleEmployee.getAge());
        Assertions.assertThat(actualEmployees.get(0).getNoOfChildrens()).isEqualTo(exampleEmployee.getNoOfChildrens());
        Assertions.assertThat(actualEmployees.get(0).getSpouse()).isEqualTo(exampleEmployee.getSpouse());
        Assertions.assertThat(actualEmployees.get(0).getDateOfJoining()).isEqualTo(exampleEmployee.getDateOfJoining());
        Assertions.assertThat(actualEmployees.get(0).getHobbies().toArray()).isEqualTo(exampleEmployee.getHobbies().toArray());
        Assertions.assertThat(actualEmployees.get(0).getPhoneNumbers().stream().map(PhoneNumber::getType).toArray()).isEqualTo(exampleEmployee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray());
        Assertions.assertThat(actualEmployees.get(0).getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()).isEqualTo(exampleEmployee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray());
    }


    @ParameterizedTest
    @MethodSource(value = "generateExample")
    void testGivenExample_WhenFindByExample_ThenReturn2Record(Example<Employee> employeeExample, int count) {
        // Given
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
        Employee natasha = Employee.builder().firstName("Natasha").lastName("Black Widow").noOfChildrens(1).age(35).spouse(false).build();

        // When
        Employee employee = employeeRepository.save(natasha);

        // Then
        Assertions.assertThat(employee).isNotNull();
        Assertions.assertThat(employee.getFirstName()).isEqualTo(natasha.getFirstName());
        Assertions.assertThat(employee.getAge()).isEqualTo(natasha.getAge());
        Assertions.assertThat(employee.getNoOfChildrens()).isEqualTo(natasha.getNoOfChildrens());
        Assertions.assertThat(employee.getSpouse()).isEqualTo(natasha.getSpouse());
        Assertions.assertThat(employee.getDateOfJoining()).isEqualTo(natasha.getDateOfJoining());
        Assertions.assertThat(employee.getHobbies()).isEqualTo(natasha.getHobbies());
        Assertions.assertThat(employee.getPhoneNumbers()).isEqualTo(natasha.getPhoneNumbers());
    }

    @Test
    void testGivenId_WhenDeleteRecord_ThenReturnTrue() {
        // Given
        Optional<Employee> optionalRahulGhadage = employeeRepository.findAll().stream().filter(employee -> employee.getFirstName().equals("Rahul") && employee.getLastName().equals("Ghadage")).findFirst();
        Employee exampleEmployee = optionalRahulGhadage.orElseGet(() -> Employee.builder().build());

        // When
        employeeRepository.deleteById(exampleEmployee.getId());
        Boolean deletedEmployee = employeeRepository.existsById(exampleEmployee.getId());

        // Then
        Assertions.assertThat(deletedEmployee).isFalse();
    }

    @Test
    void testGivenId_WhenEditRecord_ThenReturnEditedRecord() {
        // Given
        Optional<Employee> optionalRahulGhadage = employeeRepository.findAll().stream().filter(employee -> employee.getFirstName().equals("Rahul") && employee.getLastName().equals("Ghadage")).findFirst();
        Employee exampleEmployee = optionalRahulGhadage.orElseGet(() -> Employee.builder().build());

        // When
        Optional<Employee> optionalEmployee = employeeRepository.findById(exampleEmployee.getId());
        Employee editEmployee = optionalEmployee.orElseGet(() -> Employee.builder().build());
        editEmployee.setAge(18);
        Employee employee = employeeRepository.save(editEmployee);

        // Then
        Assertions.assertThat(employee).isNotNull();
        Assertions.assertThat(employee.getId()).isEqualTo(editEmployee.getId());
        Assertions.assertThat(employee.getFirstName()).isEqualTo(editEmployee.getFirstName());
        Assertions.assertThat(employee.getAge()).isEqualTo(editEmployee.getAge());
        Assertions.assertThat(employee.getNoOfChildrens()).isEqualTo(editEmployee.getNoOfChildrens());
        Assertions.assertThat(employee.getSpouse()).isEqualTo(editEmployee.getSpouse());
        Assertions.assertThat(employee.getDateOfJoining()).isEqualTo(editEmployee.getDateOfJoining());
        Assertions.assertThat(employee.getHobbies()).isEqualTo(editEmployee.getHobbies());
        Assertions.assertThat(employee.getPhoneNumbers()).isEqualTo(editEmployee.getPhoneNumbers());
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
        Employee canFlyEmployees = Employee.builder().spouse(true).build();
        Employee cannotFlyEmployees = Employee.builder().spouse(false).build();
        return Stream.of(
                Arguments.of(Example.of(canFlyEmployees, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 1),
                Arguments.of(Example.of(cannotFlyEmployees, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 1)
        );
    }
}