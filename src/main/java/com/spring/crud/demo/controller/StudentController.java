package com.spring.crud.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.crud.demo.dto.ResponseDTO;
import com.spring.crud.demo.dto.StudentDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.mapper.BaseMapper;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/students")
@RestController(value = "studentController")
public class StudentController implements BaseController<StudentDTO> {

    private final StudentService studentService;
    private final BaseMapper<Student, StudentDTO> studentMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<List<StudentDTO>> getAllRecords() {
        List<Student> studentList = studentService.getAllRecords();
        if (studentList.isEmpty()) {
            throw new NotFoundException("No record found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(studentList.stream().map(studentMapper::convertFromEntityToDto).toList());
    }

    @Override
    public ResponseEntity<StudentDTO> getRecordsById(Integer id) {
        Optional<Student> optionalStudent = studentService.getRecordsById(id);
        if (optionalStudent.isEmpty()) {
            throw new NotFoundException("No record found with id " + id);
        }
        return ResponseEntity.status(HttpStatus.OK).body(studentMapper.convertFromEntityToDto(optionalStudent.get()));
    }

    @Override
    public ResponseEntity<List<StudentDTO>> getAllRecordsByExample(StudentDTO allRequestParams) {
        StudentDTO studentDTO = objectMapper.convertValue(allRequestParams, StudentDTO.class);
        List<Student> studentList = studentService.getAllRecordsByExample(studentMapper.convertFromDtoToEntity(studentDTO));
        if (studentList.isEmpty()) {
            throw new NotFoundException("No record found with map " + allRequestParams);
        }
        return ResponseEntity.status(HttpStatus.OK).body(studentList.stream().map(studentMapper::convertFromEntityToDto).toList());
    }

    @Override
    public ResponseEntity<StudentDTO> insertRecord(@RequestBody StudentDTO studentDTO) {
        Optional<Student> optionalStudent = studentService.insertRecord(studentMapper.convertFromDtoToEntity(studentDTO));
        if (optionalStudent.isEmpty()) {
            throw new InternalServerErrorException("Something went wrong");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(studentMapper.convertFromEntityToDto(optionalStudent.get()));
    }

    @Override
    public ResponseEntity<StudentDTO> updateRecord(Integer id, StudentDTO studentDTO) {
        Optional<Student> optionalStudent = studentService.updateRecord(id, studentMapper.convertFromDtoToEntity(studentDTO));
        if (optionalStudent.isEmpty()) {
            throw new InternalServerErrorException("Something went wrong");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(studentMapper.convertFromEntityToDto(optionalStudent.get()));
    }

    @Override
    public ResponseEntity<ResponseDTO> deleteRecordById(Integer id) {
        if (!studentService.deleteRecordById(id)) {
            throw new NotFoundException("No record found with id " + id);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ResponseDTO(HttpStatus.ACCEPTED.value(),String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis()),"Record deleted with id " + id));
    }

    @Override
    public ResponseEntity<Void> deleteAllRecords() {
        studentService.deleteAllRecords();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}




