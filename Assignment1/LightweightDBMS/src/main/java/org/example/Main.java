package org.example;

import java.util.Scanner;

/**
 * Main class to handle user registration, login, password recovery, and interact with the database system.
 */
public class Main {

    /**
     * Main method that drives the entire user authentication and database system.
     *
     * @param args Command-line arguments (not used in this application).
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome! Choose an option: \n1. Register \n2. Login");
        int choice = scanner.nextInt();
        scanner.nextLine();

        String username = "";
        boolean authenticated = false;

        if (choice == 1) {
            System.out.print("Enter a new Username: ");
            username = scanner.nextLine();

            System.out.print("Enter a new Password: ");
            String password = scanner.nextLine();

            System.out.print("Set a security question answer (e.g., Your favorite color?): ");
            String securityAnswer = scanner.nextLine();

            UserManager.registerUser(username, password, securityAnswer);
            System.out.println("Registration successful! Please login.");
        }

        while (!authenticated) {
            System.out.print("Enter Username: ");
            username = scanner.nextLine();

            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            String captchaQuestion = Authentication.initiateCaptchaChallenge(username);
            System.out.println("CAPTCHA: " + captchaQuestion);

            System.out.print("Enter CAPTCHA answer: ");
            String captchaInput = scanner.nextLine();

            authenticated = Authentication.authenticate(username, password, captchaInput);
        }

        System.out.print("Forgot password? (y/n): ");
        String forgotPassword = scanner.nextLine();

        if (forgotPassword.equals("y")) {
            System.out.print("Answer your security question: ");
            String answer = scanner.nextLine();

            String token = Authentication.passwordRecovery(username, answer);
            if (token != null) {
                System.out.print("Enter the recovery token: ");
                String enteredToken = scanner.nextLine();

                if (token.equals(enteredToken)) {
                    System.out.print("Enter your new password: ");
                    String newPassword = scanner.nextLine();
                    if (Authentication.verifyAndResetPassword(username, enteredToken, newPassword)) {
                        System.out.println("Password reset successfully!");
                    }
                } else {
                    System.out.println("Invalid recovery token.");
                }
            }
        }

        System.out.println("Authentication successful! Starting Database System...");
        DatabaseSystem dbSystem = new DatabaseSystem();
        dbSystem.start();

        while (true) {
            System.out.println("Enter your SQL-like command (or type 'exit' to quit): ");
            String command = scanner.nextLine();

            if ("exit".equalsIgnoreCase(command)) {
                System.out.println("Exiting Database System...");
                break;
            }


            dbSystem.processCommand(command);
        }

        scanner.close();
    }
}



