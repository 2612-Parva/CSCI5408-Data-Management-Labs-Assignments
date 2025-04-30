
package org.example;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import java.io.*;
import java.util.*;

/**
 * This class handles persistent storage for the database system.
 */
public class PersistentStorage {

    private static final String STORAGE_PATH = "database.json";
    private Map<String, List<Map<String, String>>> tables;

    /**
     * Constructor to initialize the storage system.
     */
    public PersistentStorage() {
        this.tables = new HashMap<>();
        ensureStorageFileExists();
        loadDataFromFile();
    }

    /**
     * Ensures the storage file exists, creates it if not.
     */
    private void ensureStorageFileExists() {
        File file = new File(STORAGE_PATH);
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("{}");
                writer.close();
                System.out.println("Created new database file: " + STORAGE_PATH);
            } catch (IOException e) {
                System.err.println("Error creating database file: " + e.getMessage());
            }
        }
    }

    /**
     * Loads data from the persistent storage file (JSON) into memory.
     */
    private void loadDataFromFile() {
        try (FileReader reader = new FileReader(STORAGE_PATH)) {
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(reader);
            for (Object key : data.keySet()) {
                String tableName = (String) key;
                JSONArray rows = (JSONArray) data.get(tableName);
                List<Map<String, String>> tableData = new ArrayList<>();
                for (Object row : rows) {
                    JSONObject rowData = (JSONObject) row;
                    Map<String, String> rowMap = new HashMap<>();
                    for (Object colKey : rowData.keySet()) {
                        rowMap.put((String) colKey, (String) rowData.get(colKey));
                    }
                    tableData.add(rowMap);
                }
                tables.put(tableName, tableData);
            }
            System.out.println("Database loaded successfully.");
        } catch (Exception e) {
            System.err.println("Error loading database file: " + e.getMessage());
        }
    }

    /**
     * Saves the current state of the tables to the persistent storage file.
     */
    public void saveDataToFile() {
        JSONObject data = new JSONObject();
        for (Map.Entry<String, List<Map<String, String>>> entry : tables.entrySet()) {
            String tableName = entry.getKey();
            JSONArray rows = new JSONArray();
            for (Map<String, String> row : entry.getValue()) {
                JSONObject rowData = new JSONObject();
                rowData.putAll(row);
                rows.add(rowData);
            }
            data.put(tableName, rows);
        }

        try (FileWriter file = new FileWriter(STORAGE_PATH)) {
            file.write(data.toJSONString());
            System.out.println("Database saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving database file: " + e.getMessage());
        }
    }

    /**
     * Adds a new table to the storage.
     *
     * @param tableName The name of the table.
     */
    public void createTable(String tableName) {
        if (!tables.containsKey(tableName)) {
            tables.put(tableName, new ArrayList<>());
            saveDataToFile(); // Save changes after creating table
            System.out.println("Table '" + tableName + "' created.");
        } else {
            System.out.println("Table '" + tableName + "' already exists.");
        }
    }

    /**
     * Gets the data for a table.
     *
     * @param tableName The name of the table.
     * @return The table data.
     */
    public List<Map<String, String>> getTableData(String tableName) {
        return tables.getOrDefault(tableName, new ArrayList<>());
    }

    /**
     * Gets all the table names in the database.
     *
     * @return A set of table names.
     */
    public Set<String> getTables() {
        return tables.keySet();
    }

    /**
     * Inserts a new row into the specified table.
     *
     * @param tableName The name of the table.
     * @param row The row data to insert.
     */
    public void insertRow(String tableName, Map<String, String> row) {
        tables.computeIfAbsent(tableName, k -> new ArrayList<>()).add(row);
        saveDataToFile();
        System.out.println("Row inserted into '" + tableName + "' successfully.");
    }

    public void deleteRows(String tableName, String condition) {
    }

    public void updateRows(String tableName, String[] columnValuePairs, String condition) {
    }
}

