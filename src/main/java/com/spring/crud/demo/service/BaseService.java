package com.spring.crud.demo.service;

import java.util.List;
import java.util.Optional;

public interface BaseService<T> {

    // Select
    List<T> getAllRecords();
    Optional<T> getRecordsById(int id);

    boolean existRecordById(int id);
    List<T> getAllRecordsByExample(T entity);

    // Insert
    Optional<T> insertRecord(T entity);
    List<T> insertBulkRecords(Iterable<T> entities);

    // Update
    Optional<T> updateRecord(int id, T entity);

    // Delete
    boolean deleteRecordById(int id);
    void deleteAllRecords();
}
