package com.spring.crud.demo.utils;

import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.model.emp.Address;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class HelperUtil {

    private HelperUtil() {
    }


    public static Supplier<List<Student>> studentSupplier = () ->
            Arrays.asList(
                    Student.builder().rollNo(1).firstName("Binay").lastName("Gurung").marks(300.0f).dateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT))).build(),
                    Student.builder().rollNo(2).firstName("Rahul").lastName("Ghadage").marks(950.0f).dateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT))).build(),
                    Student.builder().rollNo(3).firstName("Sunny").lastName("Deol").marks(500.0f).dateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT))).build(),
                    Student.builder().rollNo(4).firstName("Salman").lastName("Khan").marks(600.0f).dateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT))).build(),
                    Student.builder().rollNo(5).firstName("Aamir").lastName("Khan").marks(700.0f).dateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT))).build(),
                    Student.builder().rollNo(6).firstName("Shahrukh").lastName("Khan").marks(800.0f).dateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT))).build(),
                    Student.builder().rollNo(7).firstName("Ranbir").lastName("Kapoor").marks(900.0f).dateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT))).build(),
                    Student.builder().rollNo(8).firstName("Ranveer").lastName("Singh").marks(800.0f).dateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT))).build(),
                    Student.builder().rollNo(9).firstName("Akshay").lastName("Kumar").marks(900.0f).dateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT))).build(),
                    Student.builder().rollNo(10).firstName("Ajay").lastName("Devgan").marks(800.0f).dateOfBirth(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT))).build()
            );


    public static Supplier<List<SuperHero>> superHeroesSupplier = () ->
            Arrays.asList(
                    SuperHero.builder().name("Wade").superName("Deadpool").profession("Street fighter").age(28).canFly(false).build(),
                    SuperHero.builder().name("Bruce").superName("Hulk").profession("Doctor").age(50).canFly(false).build(),
                    SuperHero.builder().name("Steve").superName("Captain America").profession("Solder").age(120).canFly(false).build(),
                    SuperHero.builder().name("Tony").superName("Iron Man").profession("Business man").age(45).canFly(true).build(),
                    SuperHero.builder().name("Peter").superName("Spider Man").profession("Student").age(21).canFly(true).build()
            );


    public static Supplier<List<Employee>> employeeSupplier = () -> {

        Address rahulAddress = new Address();
        rahulAddress.setId(1);
        rahulAddress.setStreetAddress("RS road");
        rahulAddress.setCity("Pune");
        rahulAddress.setState("Maharashtra");
        rahulAddress.setCountry("India");
        rahulAddress.setPostalCode("411018");

        PhoneNumber rahulsNo = new PhoneNumber();
        rahulsNo.setId(1);
        rahulsNo.setType("Mobile");
        rahulsNo.setNumber("1234567890");

        Employee rahul = new Employee();
        rahul.setId(1);
        rahul.setFirstName("Rahul");
        rahul.setLastName("Ghadage");
        rahul.setAge(28);
        rahul.setNoOfChildrens(0);
        rahul.setDateOfJoining(LocalDateTime.parse("01-01-2000 01:01:01", DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)));
        rahul.setSpouse(true);
        rahul.setAddress(rahulAddress);
        rahul.setPhoneNumbers(Arrays.asList(rahulsNo));
        rahul.setHobbies(Arrays.asList("Coding", "Reading"));

        rahulAddress.setEmployee(rahul);
        rahulsNo.setEmployee(rahul);

        //********************************//
        Address aryanAddress = new Address();
        aryanAddress.setId(1);
        aryanAddress.setStreetAddress("A road");
        aryanAddress.setCity("Pune");
        aryanAddress.setState("Maharashtra");
        aryanAddress.setCountry("India");
        aryanAddress.setPostalCode("411018");

        PhoneNumber aryansNumber = new PhoneNumber();
        aryansNumber.setId(1);
        aryansNumber.setType("Mobile");
        aryansNumber.setNumber("1234555555");

        Employee aryan = new Employee();
        aryan.setId(1);
        aryan.setFirstName("Aryan");
        aryan.setLastName("Ghadage");
        aryan.setAge(28);
        aryan.setNoOfChildrens(0);
        aryan.setDateOfJoining(LocalDateTime.parse("01-01-2000 01:01:01", DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)));
        aryan.setSpouse(true);
        aryan.setAddress(aryanAddress);
        aryan.setPhoneNumbers(Arrays.asList(aryansNumber));
        aryan.setHobbies(Arrays.asList("Dancing", "Cooking"));

        aryanAddress.setEmployee(aryan);
        aryansNumber.setEmployee(aryan);
        return Arrays.asList(rahul, aryan);
    };
}
