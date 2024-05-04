package com.spring.crud.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dozer.DozerBeanMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@RequiredArgsConstructor
@Slf4j
@SpringBootApplication
@EnableJpaRepositories
public class SpringBootH2CRUDApplication {

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

	/*
	@Bean
	public ObjectMapper getObjectMapper(Jackson2ObjectMapperBuilder builder) {
		ObjectMapper objectMapper = builder.createXmlMapper(false).build();
		objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.registerModule(new Jdk8Module());
		return objectMapper;
	}
	*/

	/*
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
	*/
}
