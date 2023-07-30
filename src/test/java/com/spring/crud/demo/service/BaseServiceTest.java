package com.spring.crud.demo.service;

import org.springframework.data.domain.Example;

import java.io.IOException;

public interface BaseServiceTest<T> {


    void testGivenNon_WhenGetAllRecords_ThenReturnListRecord();

    void testGivenId_WhenGetRecordsById_ThenReturnRecord();

    void testGivenRandomId_WhenGetRecordsById_ThenThrowException();

    void testGivenId_WhenExistRecordById_ThenReturnTrue();

    void testGivenRandomId_WhenExistRecordById_ThenReturnFalse();

    void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord();

    void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenReturnEmptyListRecords();

    void testGivenMultipleExample_WhenGetAllRecordsByExample_ThenReturnListRecord(Example<T> example, int count) throws IOException;

    void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord();

    void testGivenExistingRecord_WhenInsertRecord_ThenThrowException();

    void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord();

    void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException();

    void testGivenExistingRecordAndRandomId_WhenUpdateRecord_ThenThrowException();

    void testGivenRecordIdAndRecord_WhenUpdateRecord_ThenThrowException();

    void testGivenId_WhenDeleteRecord_ThenReturnTrue();

    void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse();

    void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord();

    void assertRecord(T expectedRecord, T actualRecord);
}
