package com.spring.crud.demo.service.impl;

import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.repository.StudentRepository;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.HelperUtil;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private static Tuple[] expectedStudents = null;

    @BeforeAll
    static void init() {
        expectedStudents = HelperUtil.studentSupplier.get().stream()
                .map(student -> AssertionsForClassTypes.tuple(
                        student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()
                )).toArray(Tuple[]::new);
    }

    @Test
    void testGivenNon_WhenFindAllSuperHeros_ThenReturnAllRecord() {
        // Given

        // When
        Mockito.when(studentRepository.findAll()).thenReturn(HelperUtil.studentSupplier.get());
        List<Student> superHeros = studentService.findAllStudents();

        // Then
        Assertions.assertThat(superHeros).isNotNull();
        Assertions.assertThat(superHeros.size()).isGreaterThan(0);
        Assertions.assertThat(superHeros)
                .extracting(Student::getRollNo,
                        Student::getFirstName,
                        Student::getLastName,
                        Student::getDateOfBirth,
                        Student::getMarks)
                .containsExactly(expectedStudents);
        Mockito.verify(studentRepository).findAll();
    }

    @Test
    void testGivenId_WhenFindSuperHeroById_ThenReturnRecord() {
        // Given
        int id = 12;
        Optional<Student> optionalRahulGhadage = HelperUtil.studentSupplier.get().stream().filter(student -> student.getFirstName().equals("Rahul") && student.getLastName().equals("Ghadage")).findFirst();

        // When
        Mockito.when(studentRepository.findById(id)).thenReturn(optionalRahulGhadage);
        Optional<Student> actualStudent = studentService.findStudentById(id);

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isNotEmpty();
        Assertions.assertThat(actualStudent.get()).isEqualTo(optionalRahulGhadage.get());
        Mockito.verify(studentRepository).findById(id);
    }

    @Test
    void testGivenRandomId_WhenFindSuperHeroById_ThenReturnRecord() {
        // Given
        Integer id = RandomUtils.nextInt();
        Optional<Student> optionalRahulGhadage = Optional.empty();

        // When
        Mockito.when(studentRepository.findById(id)).thenReturn(optionalRahulGhadage);
        Optional<Student> actualStudent = studentService.findStudentById(id);

        // Then
        Assertions.assertThat(actualStudent).isEmpty();
        Mockito.verify(studentRepository).findById(id);
    }

    @Test
    void testGivenStudent_WhenFindSuperHerosByExample_ThenReturnRecords() {
        // Given
        Student student = new Student(2,"Rahul","Ghadage",LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)),950.0f);

        // When
        Mockito.when(studentRepository.findAll((Example) Mockito.any())).thenReturn(List.of(student));
        List<Student> actualStudents = studentService.findStudentsByExample(student);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        Assertions.assertThat(actualStudents.get(0)).isEqualTo(student);
    }

    @Test
    void testGivenRandomStudent_WhenFindStudentByExample_ThenReturnRecords() {
        // Given
        Student student = new Student(2,"Rahul","Ghadage",LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)),950.0f);
        List<Student> students = new ArrayList<>();

        // When
        Mockito.when(studentRepository.findAll((Example) Mockito.any())).thenReturn(students);
        List<Student> actualStudents = studentService.findStudentsByExample(student);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isEmpty();
    }

    @Test
    void testGivenSuperHero_WhenSaveSuperHero_ThenReturnNewSuperHero() {
        // Given
        Student student = new Student(2,"Rahul","Ghadage",LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)),950.0f);

        // When
        Mockito.when(studentRepository.save(student)).thenReturn(student);
        Optional<Student> actualStudent = studentService.saveStudent(student);

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isNotEmpty();
        Assertions.assertThat(actualStudent.get().getRollNo()).isEqualTo(2);
        Assertions.assertThat(actualStudent.get().getFirstName()).isEqualTo("Rahul");
        Assertions.assertThat(actualStudent.get().getLastName()).isEqualTo("Ghadage");
        Assertions.assertThat(actualStudent.get().getMarks()).isEqualTo(950.0f);
        Assertions.assertThat(actualStudent.get().getDateOfBirth()).isEqualTo(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)));
        Mockito.verify(studentRepository).save(student);
    }

    @Test
    void testGivenExistingSuperHero_WhenSaveSuperHero_ThenThrowError() {
        // Given
        Student student = new Student(2,"Rahul","Ghadage",LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)),950.0f);
        student.setId(25);

        // When
        Mockito.when(studentRepository.existsById(student.getId())).thenReturn(true);
        Assertions.assertThatThrownBy(() -> studentService.saveStudent(student))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + student.getId());

        // Then
        Mockito.verify(studentRepository).existsById(student.getId());
    }

    @Test
    void testGivenExistingSuperHero_WhenUpdateSuperHero_ThenReturnUpdatedSuperHero() {
        // Given
        Student student = new Student(2,"Rahul","Ghadage",LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)),950.0f);
        student.setId(25);

        // When
        Mockito.when(studentRepository.existsById(student.getId())).thenReturn(true);
        Mockito.when(studentRepository.save(student)).thenReturn(student);
        Optional<Student> actualStudent = studentService.updateStudent(student.getId(), student);

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isNotEmpty();
        Assertions.assertThat(actualStudent.get().getId()).isEqualTo(25);
        Assertions.assertThat(actualStudent.get().getRollNo()).isEqualTo(2);
        Assertions.assertThat(actualStudent.get().getFirstName()).isEqualTo("Rahul");
        Assertions.assertThat(actualStudent.get().getLastName()).isEqualTo("Ghadage");
        Assertions.assertThat(actualStudent.get().getMarks()).isEqualTo(950.0f);
        Assertions.assertThat(actualStudent.get().getDateOfBirth()).isEqualTo(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)));
        Mockito.verify(studentRepository).existsById(student.getId());
        Mockito.verify(studentRepository).save(student);
    }

    @Test
    void testGivenNull_WhenUpdateSuperHero_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Assertions.assertThatThrownBy(() -> studentService.updateStudent(id, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Payload record id is null");
    }

    @Test
    void testGivenSuperHeroAndIdDifferent_WhenUpdateSuperHero_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();
        Student student = new Student(2,"Rahul","Ghadage",LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)),950.0f);
        student.setId(25);

        // When & Then
        Assertions.assertThatThrownBy(() -> studentService.updateStudent(id, student))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + student.getId());
    }

    @Test
    void testGivenSuperHeroAndId_WhenUpdateSuperHero_ThenThrowError() {
        // Given
        Student student = new Student(2,"Rahul","Ghadage",LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)),950.0f);
        student.setId(25);

        // When
        Mockito.when(studentRepository.existsById(student.getId())).thenReturn(false);

        // Then
        Assertions.assertThatThrownBy(() -> studentService.updateStudent(student.getId(), student))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + student.getId());
        Mockito.verify(studentRepository).existsById(student.getId());
    }

    @Test
    void testGiveId_WhenDeleteSuperHero_ThenReturnTrue() {
        // Given
        Student student = new Student(2,"Rahul","Ghadage",LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)),950.0f);
        student.setId(25);

        // When
        Mockito.when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
        Boolean flag = studentService.deleteStudent(student.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
        Mockito.verify(studentRepository).findById(student.getId());
    }

    @Test
    void testGiveRandomId_WhenDeleteSuperHero_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(studentRepository.findById(id)).thenReturn(Optional.empty());
        Boolean flag = studentService.deleteStudent(id);

        // Then
        Assertions.assertThat(flag).isFalse();
        Mockito.verify(studentRepository).findById(id);
    }
}