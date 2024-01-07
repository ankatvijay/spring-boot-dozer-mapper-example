package com.spring.crud.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.crud.demo.dto.ResponseDTO;
import com.spring.crud.demo.dto.emp.EmployeeDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.mapper.BaseMapper;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/employees")
@RestController(value = "employeeController")
public class EmployeeController implements BaseController<EmployeeDTO> {

    private final EmployeeService employeeService;
    private final BaseMapper<Employee, EmployeeDTO> employeeMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<List<EmployeeDTO>> getAllRecords() {
        List<Employee> employeeList = employeeService.getAllRecords();
        if (employeeList.isEmpty()) {
            throw new NotFoundException("No record found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(employeeList.stream().map(employeeMapper::convertFromEntityToDto).toList());
    }

    @Override
    public ResponseEntity<EmployeeDTO> getRecordsById(Integer id) {
        Optional<Employee> optionalEmployee = employeeService.getRecordsById(id);
        if (optionalEmployee.isEmpty()) {
            throw new NotFoundException("No record found with id " + id);
        }
        return ResponseEntity.status(HttpStatus.OK).body(employeeMapper.convertFromEntityToDto(optionalEmployee.get()));
    }

    @Override
    public ResponseEntity<List<EmployeeDTO>> getAllRecordsByExample(EmployeeDTO allRequestParams) throws JsonProcessingException {
        EmployeeDTO employeeDTO = objectMapper.convertValue(allRequestParams, EmployeeDTO.class);
        List<Employee> employeeList = employeeService.getAllRecordsByExample(employeeMapper.convertFromDtoToEntity(employeeDTO));
        if (employeeList.isEmpty()) {
            throw new NotFoundException("No record found with map " + objectMapper.writeValueAsString(employeeDTO));
        }
        return ResponseEntity.status(HttpStatus.OK).body(employeeList.stream().map(employeeMapper::convertFromEntityToDto).toList());
    }

    @Override
    public ResponseEntity<EmployeeDTO> insertRecord(EmployeeDTO employeeDTO) {
        Optional<Employee> optionalEmployee = employeeService.insertRecord(employeeMapper.convertFromDtoToEntity(employeeDTO));
        if (optionalEmployee.isEmpty()) {
            throw new InternalServerErrorException("Something went wrong");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeMapper.convertFromEntityToDto(optionalEmployee.get()));
    }

    @Override
    public ResponseEntity<EmployeeDTO> updateRecord(Integer id, EmployeeDTO employeeDTO) {
        Optional<Employee> optionalEmployee = employeeService.updateRecord(id, employeeMapper.convertFromDtoToEntity(employeeDTO));
        if (optionalEmployee.isEmpty()) {
            throw new InternalServerErrorException("Something went wrong");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(employeeMapper.convertFromEntityToDto(optionalEmployee.get()));
    }

    @Override
    public ResponseEntity<ResponseDTO> deleteRecordById(Integer id) {
        if (!employeeService.deleteRecordById(id)) {
            throw new NotFoundException("No record found with id " + id);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ResponseDTO(HttpStatus.ACCEPTED.value(), String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis()), "Record deleted with id " + id));
    }

    @Override
    public ResponseEntity<Void> deleteAllRecords() {
        employeeService.deleteAllRecords();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

