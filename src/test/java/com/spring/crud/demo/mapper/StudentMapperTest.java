package com.spring.crud.demo.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.dto.StudentDTO;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootTest
class StudentMapperTest {

    @Autowired
    private StudentMapper studentMapper;
    private static final File file = FileLoader.getFileFromResource("students.json");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeFactory typeFactory = objectMapper.getTypeFactory();
    private static Student student;
    private static StudentDTO studentDTO;


    @BeforeAll
    static void init() throws IOException {
        List<Student> students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        student = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        List<StudentDTO> studentDTOs = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, StudentDTO.class));
        studentDTO = studentDTOs.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(StudentDTO::new);
    }

    @Test
    void convertFromDtoToEntity() {
        // Given

        // When
        Student actualStudent = studentMapper.convertFromDtoToEntity(studentDTO);

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent.getRollNo()).isEqualTo(studentDTO.getRollNo());
        Assertions.assertThat(actualStudent.getFirstName()).isEqualTo(studentDTO.getFirstName());
        Assertions.assertThat(actualStudent.getLastName()).isEqualTo(studentDTO.getLastName());
        Assertions.assertThat(actualStudent.getDateOfBirth().format(DateTimeFormatter.ofPattern(Constant.DATE_FORMAT))).isEqualTo(studentDTO.getDateOfBirth());
        Assertions.assertThat(actualStudent.getMarks()).isEqualTo(studentDTO.getMarks());

    }

    @Test
    void convertFromEntityToDto() {
        // Given

        // When
        StudentDTO actualStudentDTO = studentMapper.convertFromEntityToDto(student);

        // Then
        Assertions.assertThat(actualStudentDTO).isNotNull();
        Assertions.assertThat(actualStudentDTO.getRollNo()).isEqualTo(student.getRollNo());
        Assertions.assertThat(actualStudentDTO.getFirstName()).isEqualTo(student.getFirstName());
        Assertions.assertThat(actualStudentDTO.getLastName()).isEqualTo(student.getLastName());
        Assertions.assertThat(actualStudentDTO.getDateOfBirth()).isEqualTo(student.getDateOfBirth().format(DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)));
        Assertions.assertThat(actualStudentDTO.getMarks()).isEqualTo(student.getMarks());

    }
}