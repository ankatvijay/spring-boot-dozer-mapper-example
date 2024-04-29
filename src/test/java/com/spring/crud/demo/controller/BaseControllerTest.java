package com.spring.crud.demo.controller;

public interface BaseControllerTest<T, R> {

    void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws Exception;

    void testGivenNon_WhenGetAllRecords_ThenThrowException() throws Exception;

    void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws Exception;

    void testGivenRandomId_WhenGetRecordsById_ThenThrowException() throws Exception;

    void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws Exception;

    void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenThrowException() throws Exception;

    void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() throws Exception;

    void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() throws Exception;

    void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() throws Exception;

    void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() throws Exception;

    void testGivenId_WhenDeleteRecord_ThenReturnTrue() throws Exception;

    void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() throws Exception;

    void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() throws Exception;

    void assertRecord(T expectedRecord, R actualRecord);
}
