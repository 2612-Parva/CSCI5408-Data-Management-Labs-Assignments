package org.example;

import java.security.*;
import java.util.*;

/**
 * Manages user credentials, security questions, and password hashing.
 */
public class UserManager {
    private static final Map<String, String> users = new HashMap<>();
    private static final Map<String, String> securityQuestions = new HashMap<>();
    /**
     * Hashes a password using SHA-256.
     *
     * @param password The plaintext password.
     * @return The hashed password.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password.", e);
        }
    }

    /**
     * Registers a new user.
     *
     * @param username The username.
     * @param password The password.
     * @param securityAnswer The answer to the security question.
     */
    public static void registerUser(String username, String password, String securityAnswer) {
        if (users.containsKey(username)) {
            System.out.println("User already exists.");
            return;
        }
        users.put(username, hashPassword(password));
        securityQuestions.put(username, securityAnswer);
        System.out.println("User registered successfully.");
    }

    /**
     * Verifies if a username and password match.
     *
     * @param username The username.
     * @param password The plaintext password.
     * @return True if credentials match, false otherwise.
     */
    public static boolean isValidUser(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(hashPassword(password));
    }

    /**
     * Verifies the security answer for password recovery.
     *
     * @param username The username.
     * @param answer The provided answer.
     * @return True if correct, false otherwise.
     */
    public static boolean verifySecurityAnswer(String username, String answer) {
        String storedAnswer = securityQuestions.get(username);
        if (storedAnswer == null) {
            System.out.println("No security answer found for the user.");
            return false;
        }
        return storedAnswer.equals(answer);
    }

    /**
     * Resets the password for a user.
     *
     * @param username The username.
     * @param newPassword The new password to be set.
     */
    public static void resetPassword(String username, String newPassword) {
        users.put(username, hashPassword(newPassword));
        System.out.println("Password has been reset successfully.");
    }
}

