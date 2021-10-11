package com.spring.crud.demo.mapper;

import com.spring.crud.demo.dto.StudentDTO;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.utils.Constant;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@RequiredArgsConstructor
@Component(value = "studentMapper")
public class StudentMapper implements Serializable {

    private final DozerBeanMapper dozerBeanMapper;

    public Student convertFromDtoToEntity(StudentDTO studentDTO) {
        Student student = dozerBeanMapper.map(studentDTO, Student.class);
        student.setDateOfBirth(studentDTO.getDateOfBirth() != null ? LocalDate.parse(studentDTO.getDateOfBirth(), DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)) : null);
        return student;
    }

    public StudentDTO convertFromEntityToDto(Student student) {
        StudentDTO studentDTO = dozerBeanMapper.map(student, StudentDTO.class);
        studentDTO.setDateOfBirth(student.getDateOfBirth() != null ? student.getDateOfBirth().format(DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)) : null);
        return studentDTO;
    }
}