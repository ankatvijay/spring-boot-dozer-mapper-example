package com.spring.crud.demo.mapper;

import com.spring.crud.demo.dto.StudentDTO;
import com.spring.crud.demo.model.Student;
import lombok.RequiredArgsConstructor;
import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component(value = "studentMapper")
public class StudentMapper implements BaseMapper<Student, StudentDTO> {

    private final DozerBeanMapper dozerBeanMapper;

    @Override
    public Student convertFromDtoToEntity(StudentDTO studentDTO) {
        Student student = dozerBeanMapper.map(studentDTO, Student.class);
        student.setDateOfBirth(studentDTO.getDateOfBirth() != null ? LocalDate.parse(studentDTO.getDateOfBirth(), dateFormatter) : null);
        return student;
    }

    @Override
    public StudentDTO convertFromEntityToDto(Student student) {
        StudentDTO studentDTO = dozerBeanMapper.map(student, StudentDTO.class);
        studentDTO.setDateOfBirth(student.getDateOfBirth() != null ? student.getDateOfBirth().format(dateFormatter) : null);
        return studentDTO;
    }
}