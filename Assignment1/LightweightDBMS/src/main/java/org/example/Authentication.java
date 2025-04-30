package org.example;

import java.security.*;
import java.util.*;

/**
 * This class handles user authentication, password recovery, CAPTCHA validation, and OTP generation.
 */
public class Authentication {
    private static final Map<String, String> userOTPs = new HashMap<>();
    private static final Map<String, Integer> captchaAnswers = new HashMap<>();
    private static final Map<String, String> userCaptchaQuestions = new HashMap<>();
    private static final Map<String, TokenInfo> recoveryTokens = new HashMap<>();

    private static final long TOKEN_EXPIRATION_TIME = 60000;

    /**
     * A helper class to hold both the recovery token and the creation time.
     */
    private static class TokenInfo {
        String token;
        long creationTime;

        /**
         * Constructs a TokenInfo object.
         *
         * @param token        The recovery token.
         * @param creationTime The timestamp when the token was created.
         */
        TokenInfo(String token, long creationTime) {
            this.token = token;
            this.creationTime = creationTime;
        }
    }

    /**
     * Generates a 6-digit recovery token.
     *
     * @return A 6-digit recovery token.
     */
    public static String generateRecoveryToken() {
        SecureRandom random = new SecureRandom();
        String token = Integer.toHexString(random.nextInt()).substring(0, 6);
        return token;
    }

    /**
     * Checks if the recovery token for a user has expired.
     *
     * @param userId The user ID to check the recovery token for.
     * @return True if the token is expired, false otherwise.
     */
    public static boolean isRecoveryTokenExpired(String userId) {
        TokenInfo tokenInfo = recoveryTokens.get(userId);
        if (tokenInfo == null) return true;
        long tokenCreationTime = tokenInfo.creationTime;
        return (System.currentTimeMillis() - tokenCreationTime) > TOKEN_EXPIRATION_TIME;
    }

    /**
     * Verifies the recovery token and resets the user's password if the token is valid.
     *
     * @param userId     The user ID to reset the password for.
     * @param token      The recovery token.
     * @param newPassword The new password to set for the user.
     * @return True if the password was reset successfully, false otherwise.
     */
    public static boolean verifyAndResetPassword(String userId, String token, String newPassword) {
        TokenInfo tokenInfo = recoveryTokens.get(userId);
        if (tokenInfo == null || !tokenInfo.token.equals(token)) {
            System.out.println("Invalid or expired recovery token.");
            return false;
        }

        if (isRecoveryTokenExpired(userId)) {
            System.out.println("Token expired! You can no longer reset your password with this token.");
            return false;
        }

        UserManager.resetPassword(userId, newPassword);
        recoveryTokens.remove(userId); // Remove the token after successful reset
        return true;
    }

    /**
     * Initiates the password recovery process by verifying the user's security answer.
     *
     * @param userId The user ID to recover the password for.
     * @param answer The security answer provided by the user.
     * @return The recovery token if successful, null if the security answer is incorrect.
     */
    public static String passwordRecovery(String userId, String answer) {
        if (!UserManager.verifySecurityAnswer(userId, answer)) {
            System.out.println("Incorrect Security Answer.");
            return null;
        }

        String recoveryToken = generateRecoveryToken();
        recoveryTokens.put(userId, new TokenInfo(recoveryToken, System.currentTimeMillis()));
        System.out.println("Recovery Token: " + recoveryToken);
        System.out.println("Your token expires in 1 minute.");
        return recoveryToken;
    }

    /**
     * Authenticates a user by verifying their password and CAPTCHA input.
     *
     * @param userId      The user ID.
     * @param password    The password provided by the user.
     * @param captchaInput The CAPTCHA input provided by the user.
     * @return True if authentication is successful, false otherwise.
     */
    public static boolean authenticate(String userId, String password, String captchaInput) {
        if (!verifyCaptcha(userId, captchaInput)) {
            System.out.println("CAPTCHA verification failed!");
            return false;
        }

        if (!UserManager.isValidUser(userId, password)) {
            System.out.println("Incorrect Username or Password!");
            AuditLogger.logEvent(userId, false);
            return false;
        }

        AuditLogger.logEvent(userId, true);
        System.out.println("Authentication Successful! Welcome, " + userId);
        return true;
    }

    /**
     * Verifies the CAPTCHA input provided by the user.
     *
     * @param userId    The user ID.
     * @param userInput The CAPTCHA input provided by the user.
     * @return True if the CAPTCHA input is correct, false otherwise.
     */
    public static boolean verifyCaptcha(String userId, String userInput) {
        Integer correctAnswer = captchaAnswers.get(userId);
        if (correctAnswer == null) {
            System.out.println("CAPTCHA expired or not initialized.");
            return false;
        }

        return correctAnswer.toString().equals(userInput);
    }

    /**
     * Initiates a CAPTCHA challenge for the user.
     *
     * @param userId The user ID.
     * @return The CAPTCHA question presented to the user.
     */
    public static String initiateCaptchaChallenge(String userId) {
        int num1 = new Random().nextInt(10) + 1;
        int num2 = new Random().nextInt(10) + 1;
        int correctAnswer = num1 + num2;

        captchaAnswers.put(userId, correctAnswer);
        userCaptchaQuestions.put(userId, num1 + " + " + num2 + " = ?");

        return userCaptchaQuestions.get(userId);
    }
}


