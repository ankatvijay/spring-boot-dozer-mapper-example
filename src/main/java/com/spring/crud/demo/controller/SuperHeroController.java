package com.spring.crud.demo.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.crud.demo.dto.ResponseDTO;
import com.spring.crud.demo.dto.SuperHeroDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.mapper.BaseMapper;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.service.SuperHeroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/super-heroes")
@RestController(value = "superHeroController")
public class SuperHeroController implements BaseController<SuperHeroDTO> {

    private final SuperHeroService superHeroService;
    private final BaseMapper<SuperHero, SuperHeroDTO> superHeroMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<List<SuperHeroDTO>> getAllRecords() {
        List<SuperHero> superHeroList = superHeroService.getAllRecords();
        if (superHeroList.isEmpty()) {
            throw new NotFoundException("No record found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(superHeroList.stream().map(superHeroMapper::convertFromEntityToDto).toList());
    }

    @Override
    public ResponseEntity<SuperHeroDTO> getRecordsById(Integer id) {
        Optional<SuperHero> optionalSuperHero = superHeroService.getRecordsById(id);
        if (optionalSuperHero.isEmpty()) {
            throw new NotFoundException("No record found with id " + id);
        }
        return ResponseEntity.status(HttpStatus.OK).body(superHeroMapper.convertFromEntityToDto(optionalSuperHero.get()));
    }

    @Override
    public ResponseEntity<List<SuperHeroDTO>> getAllRecordsByExample(SuperHeroDTO allRequestParams) throws JsonProcessingException {
        SuperHeroDTO superHeroDTO = objectMapper.convertValue(allRequestParams, SuperHeroDTO.class);
        List<SuperHero> superHeroList = superHeroService.getAllRecordsByExample(superHeroMapper.convertFromDtoToEntity(superHeroDTO));
        if (superHeroList.isEmpty()) {
            throw new NotFoundException("No record found with map " + objectMapper.writeValueAsString(superHeroDTO));
        }
        return ResponseEntity.status(HttpStatus.OK).body(superHeroList.stream().map(superHeroMapper::convertFromEntityToDto).toList());
    }

    @Override
    public ResponseEntity<SuperHeroDTO> insertRecord(@RequestBody SuperHeroDTO superHeroDTO) {
        Optional<SuperHero> optionalSuperHero = superHeroService.insertRecord(superHeroMapper.convertFromDtoToEntity(superHeroDTO));
        if (optionalSuperHero.isEmpty()) {
            throw new InternalServerErrorException("Something went wrong");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(superHeroMapper.convertFromEntityToDto(optionalSuperHero.get()));
    }

    @Override
    public ResponseEntity<SuperHeroDTO> updateRecord(Integer id, SuperHeroDTO superHeroDTO) {
        Optional<SuperHero> optionalSuperHero = superHeroService.updateRecord(id, superHeroMapper.convertFromDtoToEntity(superHeroDTO));
        if (optionalSuperHero.isEmpty()) {
            throw new InternalServerErrorException("Something went wrong");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(superHeroMapper.convertFromEntityToDto(optionalSuperHero.get()));
        /*
        try {
            Optional<SuperHero> optionalSuperHero = superHeroService.updateRecord(id, superHeroMapper.convertFromDtoToEntity(superHeroDTO));
            URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/{id}")
                    .buildAndExpand(optionalSuperHero.get().getId())
                    .toUri();
            return ResponseEntity.created(uri).body(superHeroMapper.convertFromEntityToDto(optionalSuperHero.get()));
        } catch (Exception ex) {
            throw new InternalServerErrorException("Something went wrong");
        }
        */
    }

    @Override
    public ResponseEntity<ResponseDTO> deleteRecordById(Integer id) {
        if(!superHeroService.deleteRecordById(id)){
            throw new NotFoundException("No record found with id "+id);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ResponseDTO(HttpStatus.ACCEPTED.value(), String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis()), "Record deleted with id "+id));
    }

    @Override
    public ResponseEntity<Void> deleteAllRecords() {
        superHeroService.deleteAllRecords();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
