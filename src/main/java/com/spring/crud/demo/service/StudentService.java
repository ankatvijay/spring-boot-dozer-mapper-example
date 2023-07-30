package com.spring.crud.demo.service;

import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service(value = "studentService")
public class StudentService implements BaseService<Student> {

    private final StudentRepository studentRepository;

    @Override
    public List<Student> getAllRecords() {
        return studentRepository.findAll();
    }

    @Override
    public Optional<Student> getRecordsById(int id) {
        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (optionalStudent.isEmpty()) {
            throw new NotFoundException("No record found with id " + id);
        }
        return optionalStudent;
    }

    @Override
    public boolean existRecordById(int id) {
        return studentRepository.existsById(id);
    }

    @Override
    public List<Student> getAllRecordsByExample(Student student) {
        Example<Student> studentExample = Example.of(student, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return studentRepository.findAll(studentExample);
    }

    @Override
    public Optional<Student> insertRecord(Student student) {
        if (Objects.nonNull(student) && Objects.nonNull(student.getId()) && studentRepository.existsById(student.getId())) {
            throw new RecordFoundException("Record already found with id " + student.getId());
        }
        return Optional.of(studentRepository.save(student));
    }

    @Override
    public List<Student> insertBulkRecords(Iterable<Student> students) {
        return studentRepository.saveAll(students);
    }

    @Override
    public Optional<Student> updateRecord(int id, Student student) {
        if (id > 0 && Objects.nonNull(student) && Objects.nonNull(student.getId())) {
            if (id == student.getId()) {
                if (existRecordById(id)) {
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
    public boolean deleteRecordById(int id) {
        if (existRecordById(id)) {
            studentRepository.deleteById(id);
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public void deleteAllRecords() {
        studentRepository.deleteAll();
    }

}
