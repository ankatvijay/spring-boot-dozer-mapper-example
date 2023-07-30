package com.spring.crud.demo.repository;

import org.springframework.data.domain.Example;

import java.io.IOException;

public interface BaseRepositoryTest<T> {


    void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws IOException;

    void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws IOException;

    void testGivenRandomId_WhenGetRecordsById_ThenReturnEmpty();

    void testGivenId_WhenExistRecordById_ThenReturnTrue() throws IOException;

    void testGivenRandomId_WhenExistRecordById_ThenReturnFalse();

    void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws IOException;

    void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenReturnEmptyListRecords();

    void testGivenMultipleExample_WhenGetAllRecordsByExample_ThenReturnListRecord(Example<T> example, int count) throws IOException;

    void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord();

    void testGivenExistingRecordAndUpdate_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException;

    void testGivenIdAndUpdatedRecord_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException;

    void testGivenId_WhenDeleteRecord_ThenReturnFalse() throws IOException;

    void testGivenRandomId_WhenDeleteRecord_ThenThrowException();

    void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord();

    void assertRecord(T expectedRecord, T actualRecord);
}
