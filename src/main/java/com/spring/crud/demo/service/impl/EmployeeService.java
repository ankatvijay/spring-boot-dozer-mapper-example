package com.spring.crud.demo.service.impl;

import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.repository.EmployeeRepository;
import com.spring.crud.demo.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @Override
    public Optional<Employee> saveEmployee(Employee employee) {
        return Optional.of(employeeRepository.save(employee));
    }

    @Override
    public Optional<Employee> updateEmployee(int id, Employee employee) {
        return (employeeRepository.existsById(id)) ? Optional.of(employeeRepository.save(employee)) : Optional.empty();
    }

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
