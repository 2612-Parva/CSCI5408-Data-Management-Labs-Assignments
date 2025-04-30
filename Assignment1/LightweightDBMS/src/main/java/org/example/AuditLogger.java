package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Handles logging of authentication events.
 */
public class AuditLogger {
    private static final List<String> logs = new ArrayList<>();

    /**
     * Logs authentication attempts.
     *
     * @param userId The user ID.
     * @param success True if login was successful, false otherwise.
     */
    public static void logEvent(String userId, boolean success) {
        String timestamp = new Date().toString();
        String ipAddress = "192.168.1." + new Random().nextInt(255);
        String logEntry = String.format("Timestamp: %s | User: %s | Success: %b | IP: %s", timestamp, userId, success, ipAddress);

        logs.add(logEntry);
        saveLogsToFile();
    }

    /**
     * Saves logs to a file in JSON format.
     */
    private static void saveLogsToFile() {
        try (FileWriter file = new FileWriter("audit_log.json")) {
            file.write("{ \"logs\": " + logs.toString() + " }");
        } catch (IOException e) {
            System.out.println("Error writing logs to file.");
        }
    }
}
