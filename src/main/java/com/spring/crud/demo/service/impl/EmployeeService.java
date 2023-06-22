package com.spring.crud.demo.service.impl;

import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.repository.EmployeeRepository;
import com.spring.crud.demo.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service(value = "employeeService")
public class EmployeeService implements IEmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public List<Employee> findAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public Optional<Employee> findEmployeeById(int id) {
        return employeeRepository.findById(id);
    }

    @Override
    public List<Employee> findEmployeesByExample(Employee employee) {
        Example<Employee> employeeExample = Example.of(employee, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return employeeRepository.findAll(employeeExample);
    }

    @Transactional
    @Override
    public Optional<Employee> saveEmployee(Employee employee) {
        if (Objects.nonNull(employee) && Objects.nonNull(employee.getId()) && employeeRepository.existsById(employee.getId())) {
            throw new RecordFoundException("Record already found with id " + employee.getId());
        }
        return Optional.of(employeeRepository.save(employee));
    }

    @Transactional
    @Override
    public Optional<Employee> updateEmployee(int id, Employee employee) {
        if (id > 0 && Objects.nonNull(employee) && Objects.nonNull(employee.getId())) {
            if (id == employee.getId().intValue()) {
                if (employeeRepository.existsById(id)) {
                    return Optional.of(employeeRepository.save(employee));
                }
                throw new NotFoundException("No record found with id " + id);
            } else {
                throw new InternalServerErrorException("Update Record id: " + id + " not equal to payload id: " + employee.getId());
            }
        } else {
            throw new NullPointerException("Payload record id is null");
        }
    }

    @Transactional
    @Override
    public boolean deleteEmployee(int id) {
        Optional<Employee> optionalEmployee = findEmployeeById(id);
        if (optionalEmployee.isPresent()) {
            employeeRepository.delete(optionalEmployee.get());
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}
