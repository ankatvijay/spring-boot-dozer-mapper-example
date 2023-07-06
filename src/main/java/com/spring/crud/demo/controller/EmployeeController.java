package com.spring.crud.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.crud.demo.dto.emp.EmployeeDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.mapper.emp.EmployeeMapper;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/employees")
@RestController(value = "employeeController")
public class EmployeeController {

    private final IEmployeeService employeeService;
    private final EmployeeMapper employeeMapper;
    private final ObjectMapper objectMapper;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<EmployeeDTO>> findAllEmployees() {
        List<Employee> employeeList = employeeService.findAllEmployees();
        if (employeeList.isEmpty()) {
            throw new NotFoundException("No record found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(employeeList.stream().map(employeeMapper::convertFromEntityToDto).collect(Collectors.toList()));
    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<EmployeeDTO> findEmployeeById(@PathVariable int id) {
        Optional<Employee> optionalEmployee = employeeService.findEmployeeById(id);
        if (optionalEmployee.isEmpty()) {
            throw new NotFoundException("No record found with id " + id);
        }
        return ResponseEntity.status(HttpStatus.OK).body(employeeMapper.convertFromEntityToDto(optionalEmployee.get()));
    }

    @PostMapping(value = "/search", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<EmployeeDTO>> findEmployeesByExample(@RequestBody Map<String, Object> allRequestParams) {
        EmployeeDTO employeeDTO = objectMapper.convertValue(allRequestParams, EmployeeDTO.class);
        List<Employee> employeeList = employeeService.findEmployeesByExample(employeeMapper.convertFromDtoToEntity(employeeDTO));
        if (employeeList.isEmpty()) {
            throw new NotFoundException("No record found with map " + allRequestParams);
        }
        return ResponseEntity.status(HttpStatus.OK).body(employeeList.stream().map(employeeMapper::convertFromEntityToDto).collect(Collectors.toList()));
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<EmployeeDTO> saveEmployee(@RequestBody EmployeeDTO employeeDTO) {
        Optional<Employee> optionalEmployee = employeeService.saveEmployee(employeeMapper.convertFromDtoToEntity(employeeDTO));
        if (optionalEmployee.isEmpty()) {
            throw new InternalServerErrorException("Something went wrong");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeMapper.convertFromEntityToDto(optionalEmployee.get()));
    }


    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable int id, @RequestBody EmployeeDTO employeeDTO) {
        Optional<Employee> optionalEmployee = employeeService.updateEmployee(id, employeeMapper.convertFromDtoToEntity(employeeDTO));
        if (optionalEmployee.isEmpty()) {
            throw new InternalServerErrorException("Something went wrong");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(employeeMapper.convertFromEntityToDto(optionalEmployee.get()));
    }


    @DeleteMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Boolean> deleteEmployee(@PathVariable int id) {
        return ResponseEntity.ok().body(employeeService.deleteEmployee(id));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllEmployee() {
        employeeService.deleteAllEmployee();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

