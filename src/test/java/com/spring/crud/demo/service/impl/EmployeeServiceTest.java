package com.spring.crud.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;
import com.spring.crud.demo.repository.EmployeeRepository;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeService employeeService;

    private static Tuple[] expectedEmployees = null;
    private static List<Employee> employees;

    @BeforeAll
    static void init() throws IOException {
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

    @Test
    void testGivenNon_WhenFindAllEmployees_ThenReturnAllRecord() {
        // Given

        // When
        Mockito.when(employeeRepository.findAll()).thenReturn(employees);
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
        Mockito.verify(employeeRepository).findAll();
    }

    @Test
    void testGivenId_WhenFindEmployeeById_ThenReturnRecord() {
        // Given
        int id = 12;
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);

        // When
        Mockito.when(employeeRepository.findById(id)).thenReturn(Optional.of(expectedEmployee));
        Employee actualEmployee = employeeService.findEmployeeById(id).orElseGet(Employee::new);

        // Then
        assertEmployee(expectedEmployee, actualEmployee);
        Mockito.verify(employeeRepository).findById(id);
    }

    @Test
    void testGivenRandomId_WhenFindEmployeeById_ThenReturnRecord() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Mockito.when(employeeRepository.findById(id)).thenReturn(Optional.empty());
        Employee actualEmployee = employeeService.findEmployeeById(id).orElseGet(Employee::new);

        // Then
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee).hasAllNullFieldsOrProperties();
        Mockito.verify(employeeRepository).findById(id);
    }

    @Test
    void testGivenEmployee_WhenFindEmployeesByExample_ThenReturnRecords() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);

        // When
        Mockito.when(employeeRepository.findAll((Example) Mockito.any())).thenReturn(List.of(expectedEmployee));
        List<Employee> actualEmployees = employeeService.findEmployeesByExample(expectedEmployee);

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees).isNotEmpty();
        Assertions.assertThat(actualEmployees.size()).isEqualTo(1);
        assertEmployee(actualEmployees.get(0), expectedEmployee);
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

        List<Employee> employees = new ArrayList<>();

        // When
        Mockito.when(employeeRepository.findAll((Example) Mockito.any())).thenReturn(employees);
        List<Employee> actualEmployees = employeeService.findEmployeesByExample(expectedEmployee);

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees).isEmpty();
        Assertions.assertThat(actualEmployees.size()).isEqualTo(0);
    }

    @Test
    void testGivenEmployee_WhenSaveEmployee_ThenReturnNewEmployee() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);

        // When
        Mockito.when(employeeRepository.save(expectedEmployee)).thenReturn(expectedEmployee);
        Employee actualEmployee = employeeService.saveEmployee(expectedEmployee).orElseGet(Employee::new);

        // Then
        assertEmployee(expectedEmployee, actualEmployee);
        Mockito.verify(employeeRepository).save(expectedEmployee);
    }

    @Test
    void testGivenExistingEmployee_WhenSaveEmployee_ThenThrowError() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeRepository.existsById(expectedEmployee.getId())).thenReturn(true);
        Assertions.assertThatThrownBy(() -> employeeService.saveEmployee(expectedEmployee))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedEmployee.getId());

        // Then
        Mockito.verify(employeeRepository).existsById(expectedEmployee.getId());
    }

    @Test
    void testGivenExistingEmployee_WhenUpdateEmployee_ThenReturnUpdatedEmployee() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeRepository.existsById(expectedEmployee.getId())).thenReturn(true);
        Mockito.when(employeeRepository.save(expectedEmployee)).thenReturn(expectedEmployee);
        Employee actualEmployee = employeeService.updateEmployee(expectedEmployee.getId(), expectedEmployee).orElseGet(Employee::new);

        // Then
        assertEmployee(expectedEmployee, actualEmployee);
        Mockito.verify(employeeRepository).existsById(expectedEmployee.getId());
        Mockito.verify(employeeRepository).save(expectedEmployee);
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
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When & Then
        Assertions.assertThatThrownBy(() -> employeeService.updateEmployee(id, expectedEmployee))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedEmployee.getId());
    }

    @Test
    void testGivenEmployeeAndId_WhenUpdateEmployee_ThenThrowError() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeRepository.existsById(expectedEmployee.getId())).thenReturn(false);

        // Then
        Assertions.assertThatThrownBy(() -> employeeService.updateEmployee(expectedEmployee.getId(), expectedEmployee))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedEmployee.getId());
        Mockito.verify(employeeRepository).existsById(expectedEmployee.getId());
    }

    @Test
    void testGiveId_WhenDeleteEmployee_ThenReturnTrue() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeRepository.findById(expectedEmployee.getId())).thenReturn(Optional.of(expectedEmployee));
        Boolean flag = employeeService.deleteEmployee(expectedEmployee.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
        Mockito.verify(employeeRepository).findById(expectedEmployee.getId());
    }

    @Test
    void testGiveRandomId_WhenDeleteEmployee_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(employeeRepository.findById(id)).thenReturn(Optional.empty());
        Boolean flag = employeeService.deleteEmployee(id);

        // Then
        Assertions.assertThat(flag).isFalse();
        Mockito.verify(employeeRepository).findById(id);
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