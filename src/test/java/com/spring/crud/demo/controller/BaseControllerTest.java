package com.spring.crud.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

public interface BaseControllerTest<T, R> {


    void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws Exception;

    void testGivenNon_WhenGetAllRecords_ThenThrowException();

    void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws IOException;

    void testGivenRandomId_WhenGetRecordsById_ThenThrowException();

    void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws IOException;

    void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenThrowException() throws JsonProcessingException;

    void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() throws IOException;

    void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() throws IOException;

    void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException;

    void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException();

    void testGivenId_WhenDeleteRecord_ThenReturnTrue() throws IOException;

    void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse();

    void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord();

    void assertRecord(T expectedRecord, R actualRecord);
}
