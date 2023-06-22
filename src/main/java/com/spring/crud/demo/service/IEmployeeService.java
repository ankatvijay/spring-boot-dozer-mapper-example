package com.spring.crud.demo.service;

import com.spring.crud.demo.model.emp.Employee;

import java.util.List;
import java.util.Optional;

public interface IEmployeeService {

    List<Employee> findAllEmployees();

    Optional<Employee> findEmployeeById(int id);

    boolean existsByEmployeeId(int id);

    List<Employee> findEmployeesByExample(Employee employee);

    Optional<Employee> saveEmployee(Employee employee);

    Optional<Employee> updateEmployee(int id, Employee employee);

    boolean deleteEmployee(int id);

    void deleteAllEmployee();
}
