package com.spring.crud.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.function.Consumer;
import java.util.function.Function;

public class BaseSetUp<T, R> implements Consumer<T>, Function<T, R> {

    private String url;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    public BaseSetUp(String url, MockMvc mockMvc, ObjectMapper objectMapper) {
        this.url = url;
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Override
    public void accept(T t) {
        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(t)))
                    .andDo(MockMvcResultHandlers.log())
                    .andExpect(MockMvcResultMatchers.status().isCreated());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public R apply(T t) {
        try {
            String str = mockMvc.perform(MockMvcRequestBuilders.post(url)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(t)))
                    .andDo(MockMvcResultHandlers.log())
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            return (R) objectMapper.readValue(str,  t.getClass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}