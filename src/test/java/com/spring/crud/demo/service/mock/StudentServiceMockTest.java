package com.spring.crud.demo.service.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.repository.StudentRepository;
import com.spring.crud.demo.service.BaseServiceTest;
import com.spring.crud.demo.service.StudentService;
import com.spring.crud.demo.utils.Constant;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class StudentServiceMockTest implements BaseServiceTest<Student> {

    @Mock
    private StudentRepository studentRepository;
    @InjectMocks
    private StudentService studentService;
    private static Tuple[] expectedStudents = null;
    private static List<Student> students;

    @BeforeAll
    static void init() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        File file = FileLoader.getFileFromResource("students.json");
        students = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, Student.class));
        expectedStudents = students.stream()
                .map(student -> AssertionsForClassTypes.tuple(
                        student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()
                )).toArray(Tuple[]::new);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() {
        // Given

        // When
        Mockito.when(studentRepository.findAll()).thenReturn(students);
        List<Student> students = studentService.getAllRecords();

        // Then
        Assertions.assertThat(students).isNotNull();
        Assertions.assertThat(students.size()).isGreaterThan(0);
        Assertions.assertThat(students)
                .extracting(Student::getRollNo,
                        Student::getFirstName,
                        Student::getLastName,
                        Student::getDateOfBirth,
                        Student::getMarks)
                .containsExactly(expectedStudents);
        Mockito.verify(studentRepository).findAll();
    }

    @Test
    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() {
        // Given
        int id = 12;
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        // When
        Mockito.when(studentRepository.findById(id)).thenReturn(Optional.of(expectedStudent));
        Student actualStudent = studentService.getRecordsById(id).orElseGet(Student::new);

        // Then
        assertRecord(expectedStudent, actualStudent);
        Mockito.verify(studentRepository).findById(id);
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(studentRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> studentService.getRecordsById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);
        Mockito.verify(studentRepository).findById(id);
    }

    @Test
    @Override
    public void testGivenId_WhenExistRecordById_ThenReturnTrue() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When
        Mockito.when(studentRepository.existsById(expectedStudent.getId())).thenReturn(true);
        Boolean actualStudent = studentRepository.existsById(expectedStudent.getId());

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isTrue();
    }

    @Test
    @Override
    public void testGivenRandomId_WhenExistRecordById_ThenReturnFalse() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Mockito.when(studentRepository.existsById(id)).thenReturn(false);
        Boolean actualStudent = studentRepository.existsById(id);

        // Then
        Assertions.assertThat(actualStudent).isNotNull();
        Assertions.assertThat(actualStudent).isFalse();
    }

    @Test
    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        // When
        Mockito.when(studentRepository.findAll((Example) Mockito.any())).thenReturn(List.of(expectedStudent));
        List<Student> actualStudents = studentService.getAllRecordsByExample(expectedStudent);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isNotEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(1);
        Assertions.assertThat(actualStudents.get(0)).isEqualTo(expectedStudent);
        Mockito.verify(studentRepository).findAll((Example) Mockito.any());
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenReturnEmptyListRecords() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        List<Student> students = new ArrayList<>();

        // When
        Mockito.when(studentRepository.findAll((Example) Mockito.any())).thenReturn(students);
        List<Student> actualStudents = studentService.getAllRecordsByExample(expectedStudent);

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents).isEmpty();
        Assertions.assertThat(actualStudents.size()).isEqualTo(0);
        Mockito.verify(studentRepository).findAll((Example) Mockito.any());
    }

    @ParameterizedTest
    @MethodSource(value = "generateExample")
    public void testGivenMultipleExample_WhenGetAllRecordsByExample_ThenReturnListRecord(Example<Student> example, int count) {
        // Given
        List<Student> expectedStudentes = students.stream().filter(student -> student.getDateOfBirth().equals(example.getProbe().getDateOfBirth())).toList();
        Tuple[] expectedTupleStudents = expectedStudentes.stream()
                .map(student -> AssertionsForClassTypes.tuple(student.getRollNo(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth(),
                        student.getMarks()))
                .toArray(Tuple[]::new);

        // When
        Mockito.when(studentRepository.findAll((Example) Mockito.any())).thenReturn(expectedStudentes);
        List<Student> actualStudents = studentService.getAllRecordsByExample(example.getProbe());

        // Then
        Assertions.assertThat(actualStudents).isNotNull();
        Assertions.assertThat(actualStudents.size()).isEqualTo(count);
        Assertions.assertThat(actualStudents)
                .extracting(Student::getRollNo,
                        Student::getFirstName,
                        Student::getLastName,
                        Student::getDateOfBirth,
                        Student::getMarks)
                .containsExactly(expectedTupleStudents);
        Mockito.verify(studentRepository).findAll((Example) Mockito.any());
    }

    @Test
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);

        // When
        Mockito.when(studentRepository.save(expectedStudent)).thenReturn(expectedStudent);
        Student actualStudent = studentService.insertRecord(expectedStudent).orElseGet(Student::new);

        // Then
        assertRecord(expectedStudent, actualStudent);
        Mockito.verify(studentRepository).save(expectedStudent);
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When
        Mockito.when(studentRepository.existsById(expectedStudent.getId())).thenReturn(true);
        Assertions.assertThatThrownBy(() -> studentService.insertRecord(expectedStudent))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedStudent.getId());

        // Then
        Mockito.verify(studentRepository).existsById(expectedStudent.getId());
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When
        Mockito.when(studentRepository.existsById(expectedStudent.getId())).thenReturn(true);
        Mockito.when(studentRepository.save(expectedStudent)).thenReturn(expectedStudent);
        Student actualStudent = studentService.updateRecord(expectedStudent.getId(), expectedStudent).orElseGet(Student::new);

        // Then
        assertRecord(expectedStudent, actualStudent);
        Mockito.verify(studentRepository).existsById(expectedStudent.getId());
        Mockito.verify(studentRepository).save(expectedStudent);
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Assertions.assertThatThrownBy(() -> studentService.updateRecord(id, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Payload record id is null");
    }

    @Test
    @Override
    public void testGivenExistingRecordAndRandomId_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When & Then
        Assertions.assertThatThrownBy(() -> studentService.updateRecord(id, expectedStudent))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedStudent.getId());
    }

    @Test
    @Override
    public void testGivenRecordIdAndRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When
        Mockito.when(studentRepository.existsById(expectedStudent.getId())).thenReturn(false);

        // Then
        Assertions.assertThatThrownBy(() -> studentService.updateRecord(expectedStudent.getId(), expectedStudent))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedStudent.getId());
        Mockito.verify(studentRepository).existsById(expectedStudent.getId());
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() {
        // Given
        Student expectedStudent = students.stream().filter(s -> s.getFirstName().equals("Rahul") && s.getLastName().equals("Ghadage")).findFirst().orElseGet(Student::new);
        expectedStudent.setId(25);

        // When
        Mockito.when(studentRepository.existsById(expectedStudent.getId())).thenReturn(true);
        Boolean flag = studentService.deleteRecordById(expectedStudent.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
        Mockito.verify(studentRepository).existsById(expectedStudent.getId());
    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(studentRepository.existsById(id)).thenReturn(false);
        Boolean flag = studentService.deleteRecordById(id);

        // Then
        Assertions.assertThat(flag).isFalse();
        Mockito.verify(studentRepository).existsById(id);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given

        // When
        Mockito.doNothing().when(studentRepository).deleteAll();
        studentService.deleteAllRecords();

        // Then
        Mockito.verify(studentRepository).deleteAll();
    }

    private static Stream<Arguments> generateExample() {
        Student studentWithDateOfBirth = new Student();
        studentWithDateOfBirth.setDateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)));

        return Stream.of(
                Arguments.of(Example.of(studentWithDateOfBirth, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 10)
        );
    }

    public void assertRecord(Student expectedRecord, Student actualRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getRollNo()).isEqualTo(expectedRecord.getRollNo());
        Assertions.assertThat(actualRecord.getFirstName()).isEqualTo(expectedRecord.getFirstName());
        Assertions.assertThat(actualRecord.getLastName()).isEqualTo(expectedRecord.getLastName());
        Assertions.assertThat(actualRecord.getDateOfBirth()).isEqualTo(expectedRecord.getDateOfBirth());
        Assertions.assertThat(actualRecord.getMarks()).isEqualTo(expectedRecord.getMarks());
    }
}