package org.example;

import java.util.*;


public class DatabaseSystem {

    private PersistentStorage storage;
    private Query queryHandler;
    private InMemoryIndex index;
    private TransactionManager transactionManager;
    private ConcurrencyControl concurrencyControl;
    private String currentDatabase;
    private Map<String, Map<String, List<Map<String, Object>>>> databases;
    public DatabaseSystem() {
        storage = new PersistentStorage();
        queryHandler = new Query(storage);
        index = new InMemoryIndex();
        transactionManager = new TransactionManager(storage);
        concurrencyControl = new ConcurrencyControl();
        databases = new HashMap<>();
        currentDatabase = null;
    }

    /**
     * Starts the database system and allows users to interact with it via the console.
     */
    public void start() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Lightweight DBMS!");
        System.out.println("Type 'EXIT' to quit.");

        while (true) {
            System.out.print("\nEnter command: ");
            String command = scanner.nextLine().trim();

            if (command.isEmpty()) {
                System.out.println(" Please enter a valid command.");
                continue;
            }

            String[] parts = command.split("\\s+", 3);

            switch (parts[0].toUpperCase()) {
                case "CREATE":
                    if (parts.length < 3) {
                        System.out.println("Invalid CREATE syntax. Use: CREATE DATABASE dbname or CREATE TABLE tablename.");
                        continue;
                    }
                    if (parts[1].equalsIgnoreCase("DATABASE")) {
                        createDatabase(parts[2]);
                    } else if (parts[1].equalsIgnoreCase("TABLE")) {
                        if (currentDatabase == null) {
                            System.out.println("No database selected. Use USE <dbname> to select a database.");
                        } else {
                            createTable(parts[2]);
                        }
                    }
                    break;

                case "SHOW":
                    if (parts.length < 2 || parts[1].equalsIgnoreCase("DATABASES")) {
                        showDatabases();
                    } else if (parts[1].equalsIgnoreCase("TABLES")) {
                        showTables();
                    } else {
                        System.out.println("Invalid SHOW command.");
                    }
                    break;

                case "USE":
                    if (parts.length < 2) {
                        System.out.println("Invalid USE command. Use: USE <dbname>");
                        continue;
                    }
                    useDatabase(parts[1]);
                    break;

                case "DESCRIBE":
                    if (parts.length < 2 || currentDatabase == null) {
                        System.out.println("No database selected.");
                        continue;
                    }
                    describeTable(parts[1]);
                    break;

                case "INSERT":
                    if (parts.length < 3) {
                        System.out.println("Invalid INSERT syntax. Use: INSERT INTO <table> VALUES (...).");
                        continue;
                    }
                    processInsertCommand(parts[2]);
                    break;

                case "SELECT":
                    if (parts.length < 2) {
                        System.out.println("Invalid SELECT syntax. Use: SELECT * FROM <table>.");
                        continue;
                    }
                    processSelectCommand(parts[1]);
                    break;

                case "BEGIN":
                    if (parts.length < 2 || !parts[1].equalsIgnoreCase("TRANSACTION")) {
                        System.out.println("Invalid BEGIN TRANSACTION syntax. Use: BEGIN TRANSACTION");
                        continue;
                    }
                    transactionManager.beginTransaction();
                    System.out.println("Transaction started.");
                    break;

                case "COMMIT":
                    if (!transactionManager.isInTransaction()) {
                        System.out.println("No active transaction.");
                    } else {
                        transactionManager.commitTransaction();
                        System.out.println("Transaction committed.");
                    }
                    break;

                case "ROLLBACK":
                    if (!transactionManager.isInTransaction()) {
                        System.out.println("No active transaction.");
                    } else {
                        transactionManager.rollbackTransaction();
                        System.out.println("Transaction rolled back.");
                    }
                    break;

                case "EXIT":
                    System.out.println("Exiting the DBMS...");
                    scanner.close();
                    return;

                default:
                    System.out.println("Unknown command. Please try again.");
            }
        }
    }
    private void processSelectCommand(String selectStatement) {
        try {
            selectStatement = selectStatement.trim();

            if (!selectStatement.toUpperCase().startsWith("SELECT * FROM ")) {
                System.out.println("Invalid SELECT syntax. Use: SELECT * FROM tableName");
                return;
            }

            String tableName = selectStatement.substring(14).trim();  // 14 is the length of "SELECT * FROM "

            if (tableName.isEmpty()) {
                System.out.println("Invalid SELECT syntax. Use: SELECT * FROM tableName");
                return;
            }

            if (currentDatabase == null) {
                System.out.println("No database selected. Use: USE <dbname>");
                return;
            }

            if (!databases.get(currentDatabase).containsKey(tableName)) {
                System.out.println("Table '" + tableName + "' not found.");
                return;
            }

            List<Map<String, Object>> tableData = databases.get(currentDatabase).get(tableName);

            if (tableData.isEmpty()) {
                System.out.println("No data found in table '" + tableName + "'.");
            } else {
                System.out.println("Data in table '" + tableName + "':");
                for (Map<String, Object> row : tableData) {
                    System.out.println(row);
                }
            }
        } catch (Exception e) {
            System.out.println("Error processing SELECT command: " + e.getMessage());
        }
    }


    private void createDatabase(String dbName) {
        if (databases.containsKey(dbName)) {
            System.out.println("Database '" + dbName + "' already exists.");
            return;
        }
        databases.put(dbName, new HashMap<>());
        System.out.println("Database '" + dbName + "' created.");
    }

    private void useDatabase(String dbName) {
        if (!databases.containsKey(dbName)) {
            System.out.println("Database '" + dbName + "' not found.");
            return;
        }
        currentDatabase = dbName;
        System.out.println("Database '" + dbName + "' selected.");
    }

    private void showDatabases() {
        if (databases.isEmpty()) {
            System.out.println("No databases found.");
        } else {
            System.out.println("Databases:");
            for (String db : databases.keySet()) {
                System.out.println(db);
            }
        }
    }

    private void createTable(String tableSchema) {
        try {
            String[] parts = tableSchema.split("\\s+");
            String tableName = parts[0];

            if (!tableSchema.contains("(") || !tableSchema.contains(")")) {
                System.out.println("Syntax error in CREATE TABLE statement.");
                return;
            }

            if (databases.get(currentDatabase).containsKey(tableName)) {
                System.out.println("Table '" + tableName + "' already exists.");
            } else {
                databases.get(currentDatabase).put(tableName, new ArrayList<>());
                System.out.println("Table '" + tableName + "' created in database '" + currentDatabase + "'.");
            }
        } catch (Exception e) {
            System.out.println("Error processing CREATE TABLE command: " + e.getMessage());
        }
    }

    private void showTables() {
        if (currentDatabase == null) {
            System.out.println("No database selected.");
            return;
        }
        Map<String, List<Map<String, Object>>> tables = databases.get(currentDatabase);
        if (tables.isEmpty()) {
            System.out.println("No tables found in database '" + currentDatabase + "'.");
        } else {
            System.out.println("Tables in database '" + currentDatabase + "':");
            for (String table : tables.keySet()) {
                System.out.println(table);
            }
        }
    }

    private void describeTable(String tableName) {
        if (!databases.get(currentDatabase).containsKey(tableName)) {
            System.out.println("Table '" + tableName + "' not found.");
        } else {
            // Sample schema
            System.out.println("Columns for table '" + tableName + "':");
            System.out.println("id INT");
            System.out.println("name STRING");
        }
    }

    private void processInsertCommand(String insertStatement) {
        try {
            String[] parts = insertStatement.split("\\s+VALUES\\s+", 2);
            if (parts.length < 2) {
                System.out.println("Invalid INSERT syntax. Use: INSERT INTO tableName VALUES (value1, value2)");
                return;
            }

            String tableNamePart = parts[0].trim();
            String valuesPart = parts[1].trim();

            String tableName = tableNamePart.replace("INSERT INTO", "").trim();

            if (tableName.isEmpty()) {
                System.out.println("Invalid table name in INSERT command.");
                return;
            }

            valuesPart = valuesPart.replaceAll("[()]", "").trim(); // Remove parentheses
            String[] values = valuesPart.split(",");

            if (!databases.get(currentDatabase).containsKey(tableName)) {
                System.out.println("Table '" + tableName + "' not found.");
                return;
            }

            if (values.length != 2) {
                System.out.println("Invalid number of values. Expected 2 values for table '" + tableName + "'.");
                return;
            }

            Map<String, Object> newRow = new HashMap<>();

            newRow.put("id", Integer.parseInt(values[0].trim())); // Convert to Integer (assuming id is an integer)
            newRow.put("name", values[1].trim()); // Assuming name is a string

            databases.get(currentDatabase).get(tableName).add(newRow);

            System.out.println("Inserted values: " + Arrays.toString(values));

        } catch (Exception e) {
            System.out.println("Error processing INSERT command: " + e.getMessage());
        }
    }
    private void processUpdateCommand(String updateStatement) {
        try {
            updateStatement = updateStatement.trim();

            if (!updateStatement.toUpperCase().startsWith("UPDATE ")) {
                System.out.println("Invalid UPDATE syntax. Use: UPDATE tableName SET column = value WHERE condition");
                return;
            }

            String tableName = updateStatement.substring(7, updateStatement.indexOf(" SET")).trim();

            if (tableName.isEmpty()) {
                System.out.println("Invalid UPDATE syntax. Use: UPDATE tableName SET column = value WHERE condition");
                return;
            }

            String setClause = updateStatement.substring(updateStatement.indexOf(" SET ") + 5, updateStatement.indexOf(" WHERE")).trim();

            String whereClause = updateStatement.substring(updateStatement.indexOf(" WHERE ") + 7).trim();

            Map<String, String> columnValuePairs = parseSetClause(setClause);

            if (currentDatabase == null) {
                System.out.println("No database selected. Use: USE <dbname>");
                return;
            }

            if (!databases.get(currentDatabase).containsKey(tableName)) {
                System.out.println("Table '" + tableName + "' not found.");
                return;
            }

            List<Map<String, Object>> tableData = databases.get(currentDatabase).get(tableName);

            boolean updated = false;
            for (Map<String, Object> row : tableData) {
                if (evaluateWhereCondition(row, whereClause)) {
                    for (Map.Entry<String, String> entry : columnValuePairs.entrySet()) {
                        row.put(entry.getKey(), entry.getValue());
                    }
                    updated = true;
                }
            }

            if (updated) {
                System.out.println("Update successful.");
                storage.saveDataToFile();  // Save the updated data to persistent storage
            } else {
                System.out.println("No rows matched the WHERE condition.");
            }

        } catch (Exception e) {
            System.out.println("Error processing UPDATE command: " + e.getMessage());
        }
    }

    /**
     * Parses the SET clause of the UPDATE statement.
     * @param setClause The part after "SET" and before "WHERE".
     * @return A map of column-value pairs.
     */
    private Map<String, String> parseSetClause(String setClause) {
        Map<String, String> columnValuePairs = new HashMap<>();
        String[] pairs = setClause.split(",");
        for (String pair : pairs) {
            String[] columnValue = pair.split("=");
            if (columnValue.length == 2) {
                columnValuePairs.put(columnValue[0].trim(), columnValue[1].trim().replace("'", ""));
            }
        }
        return columnValuePairs;
    }

    /**
     * Evaluates the WHERE condition for a row.
     * @param row The row of data to check.
     * @param whereClause The WHERE condition.
     * @return true if the row satisfies the condition.
     */
    private boolean evaluateWhereCondition(Map<String, Object> row, String whereClause) {
        String[] condition = whereClause.split("=");
        if (condition.length == 2) {
            String columnName = condition[0].trim();
            String value = condition[1].trim().replace("'", "");
            return row.getOrDefault(columnName, "").equals(value);
        }
        return false;
    }



    public void processCommand(String command) {
    }
}








