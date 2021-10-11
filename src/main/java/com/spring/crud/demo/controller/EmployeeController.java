package com.spring.crud.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.crud.demo.dto.StudentDTO;
import com.spring.crud.demo.dto.emp.EmployeeDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.mapper.emp.EmployeeMapper;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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
        return ResponseEntity.ok().body(employeeList.stream().map(employee -> employeeMapper.convertFromEntityToDto(employee)).collect(Collectors.toList()));
    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<EmployeeDTO> findEmployeeById(@PathVariable int id) {
        try {
            return ResponseEntity.ok().body(employeeMapper.convertFromEntityToDto(employeeService.findEmployeeById(id).get()));
        } catch (Exception ex) {
            throw new NotFoundException("No Employee found : " + id);
        }
    }

    @GetMapping(value = "/search", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<EmployeeDTO>> findEmployeesByExample(@RequestParam Map<String, Object> allRequestParams) {
        try {
            EmployeeDTO employeeDTO = objectMapper.convertValue(allRequestParams, EmployeeDTO.class);
            List<Employee> employeeList = employeeService.findEmployeesByExample(employeeMapper.convertFromDtoToEntity(employeeDTO));
            return ResponseEntity.status(HttpStatus.OK).body(employeeList.stream().map(employee -> employeeMapper.convertFromEntityToDto(employee)).collect(Collectors.toList()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Something went wrong");
        }
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<EmployeeDTO> saveEmployee(@RequestBody EmployeeDTO employeeDTO) {
        try {
            Optional<Employee> optionalEmployee = employeeService.saveEmployee(employeeMapper.convertFromDtoToEntity(employeeDTO));
            URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/{id}")
                    .buildAndExpand(optionalEmployee.get().getId())
                    .toUri();
            return ResponseEntity.created(uri).body(employeeMapper.convertFromEntityToDto(optionalEmployee.get()));
        } catch (Exception ex) {
            throw new InternalServerErrorException("Something went wrong");
        }
    }


    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable int id, @RequestBody EmployeeDTO employeeDTO) {
        try {
            Optional<Employee> optionalEmployee = employeeService.updateEmployee(id, employeeMapper.convertFromDtoToEntity(employeeDTO));
            URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/{id}")
                    .buildAndExpand(optionalEmployee.get().getId())
                    .toUri();
            return ResponseEntity.created(uri).body(employeeMapper.convertFromEntityToDto(optionalEmployee.get()));
        } catch (Exception ex) {
            throw new InternalServerErrorException("Something went wrong");
        }
    }


    @DeleteMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Boolean> deleteEmployee(@PathVariable int id) {
        try {
            return ResponseEntity.ok().body(employeeService.deleteEmployee(id));
        } catch (Exception ex) {
            throw new InternalServerErrorException("Something went wrong");
        }
    }
}

