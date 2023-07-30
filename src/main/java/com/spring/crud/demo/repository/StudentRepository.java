package com.spring.crud.demo.repository;

import com.spring.crud.demo.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository(value = "studentRepository")
public interface StudentRepository extends JpaRepository<Student, Integer> {

    Optional<Student> findByRollNo(int rollNo);

    Optional<Student> findByFirstName(String firstName);

    Optional<Student> findByFirstNameIgnoreCase(String firstName);

    Optional<Student> findByLastNameIgnoreCase(String lastName);

    List<Student> findByFirstNameLike(String firstName);

    List<Student> findByMarksGreaterThanEqual(float marks);
}
