package com.spring.crud.demo.service;


import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.model.emp.Employee;

import java.util.List;
import java.util.Optional;

public interface IStudentService {
	
	List<Student> findAllStudents();

	Optional<Student> findStudentById(int id);

	Optional<Student> findStudentByRollNo(int rollNo);

	List<Student> findStudentsByExample(Student student);

	Optional<Student> saveStudent(Student student);

	Optional<Student> updateStudent(int id, Student student);

	boolean deleteStudent(int id);

}
