package com.spring.crud.demo.mapper.emp;

import com.spring.crud.demo.dto.emp.AddressDTO;
import com.spring.crud.demo.model.emp.Address;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
@Component(value = "addressMapper")
public class AddressMapper implements Serializable {

    private final DozerBeanMapper dozerBeanMapper;

    public Address convertFromDtoToEntity(AddressDTO addressDTO) {
        Address address = dozerBeanMapper.map(addressDTO, Address.class);
        return address;
    }

    public AddressDTO convertFromEntityToDto(Address address) {
        AddressDTO addressDTO = dozerBeanMapper.map(address, AddressDTO.class);
        return addressDTO;
    }
}