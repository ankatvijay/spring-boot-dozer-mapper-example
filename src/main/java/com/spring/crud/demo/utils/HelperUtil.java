package com.spring.crud.demo.utils;

import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.model.emp.Address;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.model.emp.PhoneNumber;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class HelperUtil {

    private HelperUtil() {
    }


    public static Supplier<List<Student>> studentSupplier = () ->
            Arrays.asList(
                    new Student(1, "Binay", "Gurung", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 300.0f),
                    new Student(2, "Rahul", "Ghadage", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 950.0f),
                    new Student(3, "Sunny", "Deol", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 500.0f),
                    new Student(4, "Salman", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 600.0f),
                    new Student(5, "Aamir", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 700.0f),
                    new Student(6, "Shahrukh", "Khan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 800.0f),
                    new Student(7, "Ranbir", "Kapoor", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 900.0f),
                    new Student(8, "Ranveer", "Singh", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 800.0f),
                    new Student(9, "Akshay", "Kumar", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 900.0f),
                    new Student(10, "Ajay", "Devgan", LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)), 800.0f)
            );

    public static Supplier<List<SuperHero>> superHeroesSupplier = () ->
            Arrays.asList(
                    new SuperHero("Wade", "Deadpool", "Street fighter", 28, false),
                    new SuperHero("Bruce", "Hulk", "Doctor", 50, false),
                    new SuperHero("Steve", "Captain America", "Solder", 120, false),
                    new SuperHero("Tony", "Iron Man", "Business man", 45, true),
                    new SuperHero("Peter", "Spider Man", "Student", 21, true)
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
        rahul.setPhoneNumbers(List.of(rahulsNo));
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
        aryan.setSpouse(false);
        aryan.setAddress(aryanAddress);
        aryan.setPhoneNumbers(List.of(aryansNumber));
        aryan.setHobbies(Arrays.asList("Dancing", "Cooking"));

        aryanAddress.setEmployee(aryan);
        aryansNumber.setEmployee(aryan);
        return Arrays.asList(rahul, aryan);
    };
}
