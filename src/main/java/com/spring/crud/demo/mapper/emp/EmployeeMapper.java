package com.spring.crud.demo.mapper.emp;

import com.spring.crud.demo.dto.emp.EmployeeDTO;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.utils.Constant;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@RequiredArgsConstructor
@Component(value = "employeeMapper")
public class EmployeeMapper implements Serializable {

    private final DozerBeanMapper dozerBeanMapper;

    public Employee convertFromDtoToEntity(EmployeeDTO employeeDTO) {
        Employee employee = dozerBeanMapper.map(employeeDTO, Employee.class);
        employee.setDateOfJoining(employeeDTO.getDateOfJoining() != null ? LocalDateTime.parse(employeeDTO.getDateOfJoining(), DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)) : null);
        return employee;
    }

    public EmployeeDTO convertFromEntityToDto(Employee employee) {
        EmployeeDTO employeeDTO = dozerBeanMapper.map(employee, EmployeeDTO.class);
        employeeDTO.setDateOfJoining(employee.getDateOfJoining() != null ? employee.getDateOfJoining().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)) : null);
        return employeeDTO;
    }
}