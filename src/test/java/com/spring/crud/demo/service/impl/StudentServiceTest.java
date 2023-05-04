package com.spring.crud.demo.service.impl;

import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.repository.StudentRepository;
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

import java.time.LocalDate;
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
        Optional<Student> optionalSpiderMan = HelperUtil.studentSupplier.get().stream().filter(superHero -> superHero.getFirstName().equals("Spider Man")).findFirst();

        // When
        Mockito.when(studentRepository.findById(id)).thenReturn(optionalSpiderMan);
        Optional<Student> actualSuperHero = studentService.findStudentById(id);

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isNotEmpty();
        Assertions.assertThat(actualSuperHero.get()).isEqualTo(optionalSpiderMan.get());
        Mockito.verify(studentRepository).findById(id);
    }

    @Test
    void testGivenRandomId_WhenFindSuperHeroById_ThenReturnRecord() {
        // Given
        Integer id = RandomUtils.nextInt();
        Optional<Student> optionalSpiderMan = Optional.empty();

        // When
        Mockito.when(studentRepository.findById(id)).thenReturn(optionalSpiderMan);
        Optional<Student> actualSuperHero = studentService.findStudentById(id);

        // Then
        Assertions.assertThat(actualSuperHero).isEmpty();
        Mockito.verify(studentRepository).findById(id);
    }

    @Test
    void testGivenExample_WhenFindSuperHerosByExample_ThenReturnRecords() {

    }

    @Test
    void testGivenSuperHero_WhenSaveSuperHero_ThenReturnNewSuperHero() {
        // Given
        Student superHero = Student.builder().rollNo(25).firstName("Wade").lastName("Deadpool").dateOfBirth(LocalDate.now()).marks(28f).build();

        // When
        Mockito.when(studentRepository.save(superHero)).thenReturn(superHero);
        Optional<Student> actualSuperHero = studentService.saveStudent(superHero);

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isNotEmpty();
        Assertions.assertThat(actualSuperHero.get().getRollNo()).isEqualTo(25);
        Assertions.assertThat(actualSuperHero.get().getFirstName()).isEqualTo("Deadpool");
        Assertions.assertThat(actualSuperHero.get().getLastName()).isEqualTo("Street fighter");
        Assertions.assertThat(actualSuperHero.get().getDateOfBirth()).isEqualTo(LocalDate.now());
        Assertions.assertThat(actualSuperHero.get().getMarks()).isEqualTo(45f);
        Mockito.verify(studentRepository).save(superHero);
    }

    @Test
    void testGivenExistingSuperHero_WhenSaveSuperHero_ThenThrowError() {
        // Given
        Student superHero = Student.builder().rollNo(25).firstName("Wade").lastName("Deadpool").dateOfBirth(LocalDate.now()).marks(28f).build();

        // When
        Mockito.when(studentRepository.existsById(superHero.getId())).thenReturn(true);
        Assertions.assertThatThrownBy(() -> studentService.saveStudent(superHero))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + superHero.getId());

        // Then
        Mockito.verify(studentRepository).existsById(superHero.getId());
    }

    @Test
    void testGivenExistingSuperHero_WhenUpdateSuperHero_ThenReturnUpdatedSuperHero() {
        // Given
        Student superHero = Student.builder().rollNo(25).firstName("Wade").lastName("Deadpool").dateOfBirth(LocalDate.now()).marks(28f).build();

        // When
        Mockito.when(studentRepository.existsById(superHero.getId())).thenReturn(true);
        Mockito.when(studentRepository.save(superHero)).thenReturn(superHero);
        Optional<Student> actualSuperHero = studentService.updateStudent(superHero.getId(), superHero);

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isNotEmpty();
        Assertions.assertThat(actualSuperHero.get().getId()).isEqualTo(15);
        Assertions.assertThat(actualSuperHero.get().getRollNo()).isEqualTo(25);
        Assertions.assertThat(actualSuperHero.get().getFirstName()).isEqualTo("Deadpool");
        Assertions.assertThat(actualSuperHero.get().getLastName()).isEqualTo("Street fighter");
        Assertions.assertThat(actualSuperHero.get().getDateOfBirth()).isEqualTo(LocalDate.now());
        Assertions.assertThat(actualSuperHero.get().getMarks()).isEqualTo(45f);
        Mockito.verify(studentRepository).existsById(superHero.getId());
        Mockito.verify(studentRepository).save(superHero);
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
        Student superHero = Student.builder().rollNo(25).firstName("Wade").lastName("Deadpool").dateOfBirth(LocalDate.now()).marks(28f).build();

        // When & Then
        Assertions.assertThatThrownBy(() -> studentService.updateStudent(id, superHero))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + superHero.getId());
    }

    @Test
    void testGivenSuperHeroAndId_WhenUpdateSuperHero_ThenThrowError() {
        // Given
        Student superHero = Student.builder().rollNo(25).firstName("Wade").lastName("Deadpool").dateOfBirth(LocalDate.now()).marks(28f).build();

        // When
        Mockito.when(studentRepository.existsById(superHero.getId())).thenReturn(false);

        // Then
        Assertions.assertThatThrownBy(() -> studentService.updateStudent(superHero.getId(), superHero))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + superHero.getId());
        Mockito.verify(studentRepository).existsById(superHero.getId());
    }

    @Test
    void testGiveId_WhenDeleteSuperHero_ThenReturnTrue() {
        // Given
        Student superHero = Student.builder().rollNo(25).firstName("Wade").lastName("Deadpool").dateOfBirth(LocalDate.now()).marks(28f).build();

        // When
        Mockito.when(studentRepository.findById(superHero.getId())).thenReturn(Optional.of(superHero));
        Boolean flag = studentService.deleteStudent(superHero.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
        Mockito.verify(studentRepository).findById(superHero.getId());
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