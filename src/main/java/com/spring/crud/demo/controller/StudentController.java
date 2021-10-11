package com.spring.crud.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.crud.demo.dto.StudentDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.mapper.StudentMapper;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.service.IStudentService;
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
@RequestMapping("/students")
@RestController(value = "studentController")
public class StudentController {

    private final IStudentService studentService;
    private final StudentMapper studentMapper;
    private final ObjectMapper objectMapper;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<StudentDTO>> findAllStudents() {
        List<Student> studentList = studentService.findAllStudents();
        return ResponseEntity.ok().body(studentList.stream().map(student -> studentMapper.convertFromEntityToDto(student)).collect(Collectors.toList()));

    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<StudentDTO> findStudentById(@PathVariable int id) {
        try {
            return ResponseEntity.ok().body(studentMapper.convertFromEntityToDto(studentService.findStudentById(id).get()));
        } catch (Exception ex) {
            throw new NotFoundException("No Student found : " + id);
        }
    }

    @GetMapping(value = "/{rollNo}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<StudentDTO> findStudentByRollNo(@PathVariable int rollNo) {
        try {
            return ResponseEntity.ok().body(studentMapper.convertFromEntityToDto(studentService.findStudentByRollNo(rollNo).get()));
        } catch (Exception ex) {
            throw new NotFoundException("No Student found : " + rollNo);
        }
    }

    @GetMapping(value = "/search", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<StudentDTO>> findStudentsByExample(@RequestParam Map<String, Object> allRequestParams) {
        try {
            StudentDTO studentDTO = objectMapper.convertValue(allRequestParams, StudentDTO.class);
            List<Student> studentList = studentService.findStudentsByExample(studentMapper.convertFromDtoToEntity(studentDTO));
            return ResponseEntity.status(HttpStatus.OK).body(studentList.stream().map(student -> studentMapper.convertFromEntityToDto(student)).collect(Collectors.toList()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Something went wrong");
        }
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<StudentDTO> saveStudent(@RequestBody StudentDTO studentDTO) {
        try {
            Optional<Student> optionalStudent = studentService.saveStudent(studentMapper.convertFromDtoToEntity(studentDTO));
            URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/{id}")
                    .buildAndExpand(optionalStudent.get().getId())
                    .toUri();
            return ResponseEntity.created(uri).body(studentMapper.convertFromEntityToDto(optionalStudent.get()));
        } catch (Exception ex) {
            throw new InternalServerErrorException("Something went wrong");
        }
    }


    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable int id, @RequestBody StudentDTO studentDTO) {
        try {
            Optional<Student> optionalStudent = studentService.updateStudent(id, studentMapper.convertFromDtoToEntity(studentDTO));
            URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/{id}")
                    .buildAndExpand(optionalStudent.get().getId())
                    .toUri();
            return ResponseEntity.created(uri).body(studentMapper.convertFromEntityToDto(optionalStudent.get()));
        } catch (Exception ex) {
            throw new InternalServerErrorException("Something went wrong");
        }
    }


    @DeleteMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Boolean> deleteStudent(@PathVariable int id) {
        try {
            return ResponseEntity.ok().body(studentService.deleteStudent(id));
        } catch (Exception ex) {
            throw new InternalServerErrorException("Something went wrong");
        }
    }
}




