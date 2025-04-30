package org.example;

import java.util.*;

/**
 * This class represents a basic in-memory index to manage metadata for faster access.
 */
public class InMemoryIndex {

    private Map<String, Set<String>> tableIndex;

    public InMemoryIndex() {
        this.tableIndex = new HashMap<>();
    }

    /**
     * Adds a column to the index for a specific table.
     *
     * @param tableName The name of the table.
     * @param columnName The name of the column to add.
     */
    public void addColumnToIndex(String tableName, String columnName) {
        tableIndex.computeIfAbsent(tableName, k -> new HashSet<>()).add(columnName);
    }

    /**
     * Retrieves the columns of a specific table.
     *
     * @param tableName The name of the table.
     * @return A set of column names.
     */
    public Set<String> getColumns(String tableName) {
        return tableIndex.getOrDefault(tableName, new HashSet<>());
    }

    /**
     * Checks if a column exists in a specific table.
     *
     * @param tableName The name of the table.
     * @param columnName The name of the column.
     * @return true if the column exists, false otherwise.
     */
    public boolean hasColumn(String tableName, String columnName) {
        Set<String> columns = tableIndex.get(tableName);
        return columns != null && columns.contains(columnName);
    }

    /**
     * Validates the columns requested in the SELECT query.
     *
     * @param tableName The name of the table.
     * @param requestedColumns A set of columns requested in the SELECT query.
     * @return true if all requested columns exist, false otherwise.
     */
    public boolean validateColumns(String tableName, Set<String> requestedColumns) {
        Set<String> existingColumns = getColumns(tableName);
        return existingColumns.containsAll(requestedColumns);
    }
}

