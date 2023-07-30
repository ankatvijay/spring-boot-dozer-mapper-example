package com.spring.crud.demo.service;

import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.repository.EmployeeRepository;
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
public class EmployeeService implements BaseService<Employee> {

    private final EmployeeRepository employeeRepository;

    @Override
    public List<Employee> getAllRecords() {
        return employeeRepository.findAll();
    }

    @Override
    public Optional<Employee> getRecordsById(int id) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
        if (optionalEmployee.isEmpty()) {
            throw new NotFoundException("No record found with id " + id);
        }
        return optionalEmployee;
    }

    @Override
    public boolean existRecordById(int id) {
        return employeeRepository.existsById(id);
    }

    @Override
    public List<Employee> getAllRecordsByExample(Employee employee) {
        Example<Employee> employeeExample = Example.of(employee, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return employeeRepository.findAll(employeeExample);
    }

    @Transactional
    @Override
    public Optional<Employee> insertRecord(Employee employee) {
        if (Objects.nonNull(employee) && Objects.nonNull(employee.getId()) && employeeRepository.existsById(employee.getId())) {
            throw new RecordFoundException("Record already found with id " + employee.getId());
        }
        return Optional.of(employeeRepository.save(employee));
    }

    @Override
    public List<Employee> insertBulkRecords(Iterable<Employee> employees) {
        return employeeRepository.saveAll(employees);
    }

    @Transactional
    @Override
    public Optional<Employee> updateRecord(int id, Employee employee) {
        if (id > 0 && Objects.nonNull(employee) && Objects.nonNull(employee.getId())) {
            if (id == employee.getId()) {
                if (existRecordById(id)) {
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
    public boolean deleteRecordById(int id) {
        if (existRecordById(id)) {
            employeeRepository.deleteById(id);
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public void deleteAllRecords() {
        employeeRepository.deleteAll();
    }
}
