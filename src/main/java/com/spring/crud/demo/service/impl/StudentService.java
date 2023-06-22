package com.spring.crud.demo.service.impl;

import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.repository.StudentRepository;
import com.spring.crud.demo.service.IStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service(value = "studentService")
public class StudentService implements IStudentService {

    private final StudentRepository studentRepository;

    @Override
    public List<Student> findAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Optional<Student> findStudentById(int id) {
        return studentRepository.findById(id);
    }

    @Override
    public Optional<Student> findStudentByRollNo(int rollNo) {
        return studentRepository.findByRollNo(rollNo);
    }

    @Override
    public List<Student> findStudentsByExample(Student student) {
        Example<Student> studentExample = Example.of(student, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return studentRepository.findAll(studentExample);
    }

    @Override
    public Optional<Student> saveStudent(Student student) {
        if (Objects.nonNull(student) && Objects.nonNull(student.getId()) && studentRepository.existsById(student.getId())) {
            throw new RecordFoundException("Record already found with id " + student.getId());
        }
        return Optional.of(studentRepository.save(student));
    }

    @Override
    public Optional<Student> updateStudent(int id, Student student) {
        if (id > 0 && Objects.nonNull(student) && Objects.nonNull(student.getId())) {
            if (id == student.getId().intValue()) {
                if (studentRepository.existsById(id)) {
                    return Optional.of(studentRepository.save(student));
                }
                throw new NotFoundException("No record found with id " + id);
            } else {
                throw new InternalServerErrorException("Update Record id: " + id + " not equal to payload id: " + student.getId());
            }
        } else {
            throw new NullPointerException("Payload record id is null");
        }
    }

    @Override
    public boolean deleteStudent(int id) {
        Optional<Student> optionalStudent = findStudentById(id);
        if (optionalStudent.isPresent()) {
            studentRepository.delete(optionalStudent.get());
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public void deleteAllStudent() {
        studentRepository.deleteAll();
    }

}
