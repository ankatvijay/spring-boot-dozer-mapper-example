package com.spring.crud.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.repository.EmployeeRepository;
import com.spring.crud.demo.repository.StudentRepository;
import com.spring.crud.demo.repository.SuperHeroRepository;
import com.spring.crud.demo.utils.HelperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dozer.DozerBeanMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@SpringBootApplication
@EnableJpaRepositories
public class SpringBootH2CRUDApplication {
	private final StudentRepository studentRepository;

	private final SuperHeroRepository superHeroRepository;

	private final EmployeeRepository employeeRepository;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootH2CRUDApplication.class, args);
	}

	@Bean
	public DozerBeanMapper getDozerBeanMapper() {
		return new DozerBeanMapper();
	}

	@Bean
	public ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}

	@Bean
	CommandLineRunner runner() {
		return args -> {
			List<Student> students = studentRepository.findAll();
				if (students.isEmpty()) {
					log.info("******* Inserting Students to DB *******");
					studentRepository.saveAll(HelperUtil.studentSupplier.get());
				} else {
					log.info("******* Students stored in DB Size :: {}", students.size());
					log.info("******* Students stored in DB :: {}", students);
				}

			List<SuperHero> superHeroes = superHeroRepository.findAll();
			if (superHeroes.isEmpty()) {
				log.info("******* Inserting Super heroes to DB *******");
				superHeroRepository.saveAll(HelperUtil.superHeroesSupplier.get());
			} else {
				log.info("******* Super heroes stored in DB Size :: {}", superHeroes.size());
				log.info("******* Super heroes stored in DB :: {}", superHeroes);
			}


			List<Employee> employees = employeeRepository.findAll();
			if (employees.isEmpty()) {
				log.info("******* Inserting Employees to DB *******");
				employeeRepository.saveAll(HelperUtil.employeeSupplier.get());
			} else {
				log.info("******* Employees stored in DB Size :: {}", employees.size());
				log.info("******* Employees stored in DB :: {}", employees);
			}
		};
	}

}
