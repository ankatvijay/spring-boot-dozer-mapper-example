package com.spring.crud.demo.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.dto.emp.EmployeeDTO;
import com.spring.crud.demo.dto.emp.PhoneNumberDTO;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootTest
class EmployeeMapperTest {

    @Autowired
    private EmployeeMapper employeeMapper;
    private static final File file = FileLoader.getFileFromResource("employees.json");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeFactory typeFactory = objectMapper.getTypeFactory();
    private static Employee employee;
    private static EmployeeDTO employeeDTO;


    @BeforeAll
    static void init() throws IOException {
        List<Employee> employees = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Employee.class));
        employee = employees.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(Employee::new);

        List<EmployeeDTO> employeeDTOs = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, EmployeeDTO.class));
        employeeDTO = employeeDTOs.stream().filter(e -> e.getFirstName().equals("Rahul") && e.getLastName().equals("Ghadage")).findFirst().orElseGet(EmployeeDTO::new);
    }

    @Test
    void convertFromDtoToEntity() {
        // Given

        // When
        Employee actualEmployee = employeeMapper.convertFromDtoToEntity(employeeDTO);

        // Then
        Assertions.assertThat(actualEmployee).isNotNull();
        Assertions.assertThat(actualEmployee.getFirstName()).isEqualTo(employeeDTO.getFirstName());
        Assertions.assertThat(actualEmployee.getLastName()).isEqualTo(employeeDTO.getLastName());
        Assertions.assertThat(actualEmployee.getAge()).isEqualTo(employeeDTO.getAge());
        Assertions.assertThat(actualEmployee.getNoOfChildrens()).isEqualTo(employeeDTO.getNoOfChildrens());
        Assertions.assertThat(actualEmployee.getSpouse()).isEqualTo(employeeDTO.getSpouse());
        Assertions.assertThat(actualEmployee.getDateOfJoining().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT))).isEqualTo(employeeDTO.getDateOfJoining());
        Assertions.assertThat(actualEmployee.getHobbies().toArray()).isEqualTo(employeeDTO.getHobbies().toArray());
        Assertions.assertThat(actualEmployee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray()).isEqualTo(employeeDTO.getPhoneNumbers().stream().map(PhoneNumberDTO::getType).toArray());
        Assertions.assertThat(actualEmployee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray()).isEqualTo(employeeDTO.getPhoneNumbers().stream().map(PhoneNumberDTO::getNumber).toArray());
        Assertions.assertThat(actualEmployee.getAddress().getStreetAddress()).isEqualTo(employeeDTO.getAddress().getStreetAddress());
        Assertions.assertThat(actualEmployee.getAddress().getCity()).isEqualTo(employeeDTO.getAddress().getCity());
        Assertions.assertThat(actualEmployee.getAddress().getState()).isEqualTo(employeeDTO.getAddress().getState());
        Assertions.assertThat(actualEmployee.getAddress().getCountry()).isEqualTo(employeeDTO.getAddress().getCountry());
        Assertions.assertThat(actualEmployee.getAddress().getPostalCode()).isEqualTo(employeeDTO.getAddress().getPostalCode());
    }

    @Test
    void convertFromEntityToDto() {
        // Given

        // When
        EmployeeDTO actualEmployeeDTO = employeeMapper.convertFromEntityToDto(employee);

        // Then
        Assertions.assertThat(actualEmployeeDTO).isNotNull();
        Assertions.assertThat(actualEmployeeDTO.getFirstName()).isEqualTo(employee.getFirstName());
        Assertions.assertThat(actualEmployeeDTO.getLastName()).isEqualTo(employee.getLastName());
        Assertions.assertThat(actualEmployeeDTO.getAge()).isEqualTo(employee.getAge());
        Assertions.assertThat(actualEmployeeDTO.getNoOfChildrens()).isEqualTo(employee.getNoOfChildrens());
        Assertions.assertThat(actualEmployeeDTO.getSpouse()).isEqualTo(employee.getSpouse());
        Assertions.assertThat(actualEmployeeDTO.getDateOfJoining()).isEqualTo(employee.getDateOfJoining().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)));
        Assertions.assertThat(actualEmployeeDTO.getHobbies().toArray()).isEqualTo(employee.getHobbies().toArray());
        Assertions.assertThat(actualEmployeeDTO.getPhoneNumbers().stream().map(PhoneNumberDTO::getType).toArray()).isEqualTo(employee.getPhoneNumbers().stream().map(PhoneNumber::getType).toArray());
        Assertions.assertThat(actualEmployeeDTO.getPhoneNumbers().stream().map(PhoneNumberDTO::getNumber).toArray()).isEqualTo(employee.getPhoneNumbers().stream().map(PhoneNumber::getNumber).toArray());
        Assertions.assertThat(actualEmployeeDTO.getAddress().getStreetAddress()).isEqualTo(employee.getAddress().getStreetAddress());
        Assertions.assertThat(actualEmployeeDTO.getAddress().getCity()).isEqualTo(employee.getAddress().getCity());
        Assertions.assertThat(actualEmployeeDTO.getAddress().getState()).isEqualTo(employee.getAddress().getState());
        Assertions.assertThat(actualEmployeeDTO.getAddress().getCountry()).isEqualTo(employee.getAddress().getCountry());
        Assertions.assertThat(actualEmployeeDTO.getAddress().getPostalCode()).isEqualTo(employee.getAddress().getPostalCode());
    }
}