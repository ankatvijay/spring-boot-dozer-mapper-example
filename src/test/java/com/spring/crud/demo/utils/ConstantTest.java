package com.spring.crud.demo.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


class ConstantTest {

    @Test
    void testAllStaticContants(){
        Assertions.assertThat(Constant.DATE_FORMAT).isEqualTo("dd-MM-yyyy");
        Assertions.assertThat(Constant.DATE_TIME_FORMAT).isEqualTo("dd-MM-yyyy HH:mm:ss");
    }

    @Test
    void testALlInstanceContants(){
        Constant constant = new Constant();
        Assertions.assertThat(constant.DATE_FORMAT).isEqualTo("dd-MM-yyyy");
        Assertions.assertThat(constant.DATE_TIME_FORMAT).isEqualTo("dd-MM-yyyy HH:mm:ss");
    }
}