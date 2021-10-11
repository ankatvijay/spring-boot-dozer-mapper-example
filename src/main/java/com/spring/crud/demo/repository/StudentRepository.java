package com.spring.crud.demo.repository;

import com.spring.crud.demo.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    Optional<Student> findByRollNo(int rollNo);

    Optional<Student> findByFirstName(String firstName);

    List<Student> findByFirstNameIgnoreCase(String firstName);

    List<Student> findByLastNameIgnoreCase(String lastName);

    List<Student> findByFirstNameLike(String firstName);

    List<Student> findByMarksGreaterThanEqual(float marks);
}
