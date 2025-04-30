package org.example;

import java.util.*;

/**
 * This class handles various types of database queries like CREATE, SELECT, INSERT, DELETE, UPDATE, etc.
 */
public class Query {

    private PersistentStorage storage;

    /**
     * Constructor initializes the Query class with the storage system.
     *
     * @param storage The storage system to interact with.
     */
    public Query(PersistentStorage storage) {
        this.storage = storage;
    }

    /**
     * Handles the 'CREATE TABLE' query.
     *
     * @param tableName The name of the table to create.
     */
    public void createTable(String tableName) {
        storage.createTable(tableName);
        System.out.println("Table " + tableName + " created successfully.");
    }

    /**
     * Handles the 'SHOW TABLES' query.
     * This displays all the table names present in the database.
     */
    public void showTables() {
        System.out.println("Tables in the database:");
        Set<String> tableNames = storage.getTables();
        if (tableNames.isEmpty()) {
            System.out.println("No tables found in the database.");
        } else {
            for (String tableName : tableNames) {
                System.out.println(tableName);
            }
        }
    }

    /**
     * Handles the 'SELECT' query to retrieve data.
     *
     * @param tableName The table to select from.
     * @return
     */
    public List<Map<String, String>> selectData(String tableName) {
        List<Map<String, String>> rows = storage.getTableData(tableName);
        if (rows.isEmpty()) {
            System.out.println("No data found in the table " + tableName);
        } else {
            for (Map<String, String> row : rows) {
                System.out.println(row);
            }
        }
        return rows;
    }

    /**
     * Handles the 'INSERT INTO' query.
     *
     * @param tableName The name of the table to insert into.
     * @param row The row of data to insert.
     */
    public void insertData(String tableName, Map<String, String> row) {
        storage.insertRow(tableName, row);
    }
}


