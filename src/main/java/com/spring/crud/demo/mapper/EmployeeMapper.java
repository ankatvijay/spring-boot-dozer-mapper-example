package com.spring.crud.demo.mapper;

import com.spring.crud.demo.dto.emp.EmployeeDTO;
import com.spring.crud.demo.model.emp.Employee;
import lombok.RequiredArgsConstructor;
import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor
@Component(value = "employeeMapper")
public class EmployeeMapper implements BaseMapper<Employee, EmployeeDTO> {

    private final DozerBeanMapper dozerBeanMapper;

    @Override
    public Employee convertFromDtoToEntity(EmployeeDTO employeeDTO) {
        Employee employee = dozerBeanMapper.map(employeeDTO, Employee.class);
        employee.setDateOfJoining(employeeDTO.getDateOfJoining() != null ? LocalDateTime.parse(employeeDTO.getDateOfJoining(), dateTimeFormatter) : null);
        if(Objects.nonNull(employee.getAddress())){
            employee.getAddress().setEmployee(employee);
        }
        if(Objects.nonNull(employee.getPhoneNumbers())){
            employee.getPhoneNumbers().forEach(phoneNumber -> phoneNumber.setEmployee(employee));
        }
        return employee;
    }

    @Override
    public EmployeeDTO convertFromEntityToDto(Employee employee) {
        EmployeeDTO employeeDTO = dozerBeanMapper.map(employee, EmployeeDTO.class);
        employeeDTO.setDateOfJoining(employee.getDateOfJoining() != null ? employee.getDateOfJoining().format(dateTimeFormatter) : null);
        return employeeDTO;
    }
}