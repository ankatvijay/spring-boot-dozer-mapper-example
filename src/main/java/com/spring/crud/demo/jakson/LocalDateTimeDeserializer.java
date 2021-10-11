package com.spring.crud.demo.jakson;

import com.spring.crud.demo.utils.Constant;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    protected LocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectCodec oc = parser.getCodec();
        TextNode node = (TextNode) oc.readTree(parser);
        String dateString = node.textValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT);
        return LocalDateTime.parse(dateString, formatter);
    }
}