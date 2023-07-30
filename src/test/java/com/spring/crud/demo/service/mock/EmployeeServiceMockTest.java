package com.spring.crud.demo.service.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;
import com.spring.crud.demo.repository.EmployeeRepository;
import com.spring.crud.demo.service.BaseServiceTest;
import com.spring.crud.demo.service.EmployeeService;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceMockTest implements BaseServiceTest<Employee> {

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
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() {
        // Given

        // When
        Mockito.when(employeeRepository.findAll()).thenReturn(employees);
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
        Mockito.verify(employeeRepository).findAll();
    }

    @Test
    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() {
        // Given
        int id = 12;
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);

        // When
        Mockito.when(employeeRepository.findById(id)).thenReturn(Optional.of(expectedEmployee));
        Employee actualEmployee = employeeService.getRecordsById(id).orElseGet(Employee::new);

        // Then
        assertRecord(expectedEmployee, actualEmployee);
        Mockito.verify(employeeRepository).findById(id);
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(employeeRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> employeeService.getRecordsById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);
        Mockito.verify(employeeRepository).findById(id);
    }

    @Test
    @Override
    public void testGivenId_WhenExistRecordById_ThenReturnTrue(){
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(25);

        // When
        Mockito.when(employeeRepository.existsById(expectedEmployee.getId())).thenReturn(true);
        Boolean actualEmployee = employeeRepository.existsById(expectedEmployee.getId());

        // Then
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee).isTrue();
    }

    @Test
    @Override
    public void testGivenRandomId_WhenExistRecordById_ThenReturnFalse() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Mockito.when(employeeRepository.existsById(id)).thenReturn(false);
        Boolean actualEmployee = employeeRepository.existsById(id);

        // Then
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee).isFalse();
    }

    @Test
    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);

        // When
        Mockito.when(employeeRepository.findAll((Example) Mockito.any())).thenReturn(List.of(expectedEmployee));
        List<Employee> actualEmployees = employeeService.getAllRecordsByExample(expectedEmployee);

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees).isNotEmpty();
        Assertions.assertThat(actualEmployees.size()).isEqualTo(1);
        assertRecord(actualEmployees.get(0), expectedEmployee);
        Mockito.verify(employeeRepository).findAll((Example) Mockito.any());
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

        List<Employee> employees = new ArrayList<>();

        // When
        Mockito.when(employeeRepository.findAll((Example) Mockito.any())).thenReturn(employees);
        List<Employee> actualEmployees = employeeService.getAllRecordsByExample(expectedEmployee);

        // Then
        Assertions.assertThat(actualEmployees).isNotNull();
        Assertions.assertThat(actualEmployees).isEmpty();
        Assertions.assertThat(actualEmployees.size()).isEqualTo(0);
        Mockito.verify(employeeRepository).findAll((Example) Mockito.any());
    }

    @ParameterizedTest
    @MethodSource(value = "generateExample")
    public void testGivenMultipleExample_WhenGetAllRecordsByExample_ThenReturnListRecord(Example<Employee> example, int count) {
        // Given
        List<Employee> expectedEmployeees = employees.stream().filter(employee -> employee.getSpouse().equals(example.getProbe().getSpouse())).toList();
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
        Mockito.when(employeeRepository.findAll((Example) Mockito.any())).thenReturn(expectedEmployeees);
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
        Mockito.verify(employeeRepository).findAll((Example) Mockito.any());
    }

    @Test
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);

        // When
        Mockito.when(employeeRepository.save(expectedEmployee)).thenReturn(expectedEmployee);
        Employee actualEmployee = employeeService.insertRecord(expectedEmployee).orElseGet(Employee::new);

        // Then
        assertRecord(expectedEmployee, actualEmployee);
        Mockito.verify(employeeRepository).save(expectedEmployee);
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeRepository.existsById(expectedEmployee.getId())).thenReturn(true);
        Assertions.assertThatThrownBy(() -> employeeService.insertRecord(expectedEmployee))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedEmployee.getId());

        // Then
        Mockito.verify(employeeRepository).existsById(expectedEmployee.getId());
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeRepository.existsById(expectedEmployee.getId())).thenReturn(true);
        Mockito.when(employeeRepository.save(expectedEmployee)).thenReturn(expectedEmployee);
        Employee actualEmployee = employeeService.updateRecord(expectedEmployee.getId(), expectedEmployee).orElseGet(Employee::new);

        // Then
        assertRecord(expectedEmployee, actualEmployee);
        Mockito.verify(employeeRepository).existsById(expectedEmployee.getId());
        Mockito.verify(employeeRepository).save(expectedEmployee);
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
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When & Then
        Assertions.assertThatThrownBy(() -> employeeService.updateRecord(id, expectedEmployee))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedEmployee.getId());
    }

    @Test
    @Override
    public void testGivenRecordIdAndRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeRepository.existsById(expectedEmployee.getId())).thenReturn(false);

        // Then
        Assertions.assertThatThrownBy(() -> employeeService.updateRecord(expectedEmployee.getId(), expectedEmployee))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedEmployee.getId());
        Mockito.verify(employeeRepository).existsById(expectedEmployee.getId());
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() {
        // Given
        Employee expectedEmployee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);
        expectedEmployee.setId(15);

        // When
        Mockito.when(employeeRepository.existsById(expectedEmployee.getId())).thenReturn(true);
        Boolean flag = employeeService.deleteRecordById(expectedEmployee.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
        Mockito.verify(employeeRepository).existsById(expectedEmployee.getId());
    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(employeeRepository.existsById(id)).thenReturn(false);
        Boolean flag = employeeService.deleteRecordById(id);

        // Then
        Assertions.assertThat(flag).isFalse();
        Mockito.verify(employeeRepository).existsById(id);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given

        // When
        Mockito.doNothing().when(employeeRepository).deleteAll();
        employeeService.deleteAllRecords();

        // Then
        Mockito.verify(employeeRepository).deleteAll();
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