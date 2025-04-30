import java.sql.*;
import java.util.Scanner;

public class AcademicRegistrationSystem {

    public static void main(String[] args) {
        // Database credentials for local and remote databases
        String localDbUrl = "jdbc:mysql://localhost:3306/academicreg_local";
        String remoteDbUrl = "jdbc:mysql://34.41.59.28:3306/academic_registration";
        String localUsername = "root";
        String localPassword = "Shanti@401";
        String remoteUsername = "root";
        String remotePassword = "Shanti@401";

        Connection localConn = null;
        Connection remoteConn = null;

        try {
            // Connect to the local and remote databases
            localConn = DriverManager.getConnection(localDbUrl, localUsername, localPassword);
            remoteConn = DriverManager.getConnection(remoteDbUrl, remoteUsername, remotePassword);

            // Step A: Retrieve all course details from remote Course_catalog
            String fetchCoursesQuery = "SELECT * FROM Course_catalog";
            long startTime = System.nanoTime();
            Statement stmt = remoteConn.createStatement();
            ResultSet rs = stmt.executeQuery(fetchCoursesQuery);
            long endTime = System.nanoTime();
            long executionTimeA = endTime - startTime;
            System.out.println("Step A: Retrieval of courses from remote catalog executed in " + executionTimeA + " ns");

            // Display course details
            System.out.println("Available Courses: ");
            while (rs.next()) {
                System.out.println("Course ID: " + rs.getInt("course_id") + ", Course Name: " + rs.getString("course_name") + ", Available Seats: " + rs.getInt("available_seats"));
            }

            // Step B: Collect user input for student enrollment
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter student ID for enrollment: ");
            int studentId = scanner.nextInt();
            System.out.print("Enter course ID for enrollment: ");
            int courseId = scanner.nextInt();
            System.out.print("Enter semester: ");
            String semester = scanner.next();

            // Step B1: Validate and collect enrollment date
            String enrollmentDate = null;
            boolean validDate = false;
            while (!validDate) {
                System.out.print("Enter enrollment date (YYYY-MM-DD): ");
                enrollmentDate = scanner.next();

                // Validate date format (YYYY-MM-DD)
                if (enrollmentDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    try {
                        Date.valueOf(enrollmentDate);
                        validDate = true;
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid date format. Please enter a valid date (YYYY-MM-DD).");
                    }
                } else {
                    System.out.println("Invalid date format. Please enter a valid date (YYYY-MM-DD).");
                }
            }

            // Step C: Insert enrollment details into local Course_enrollment table
            String insertEnrollmentQuery = "INSERT INTO Course_enrollment (student_id, course_name, semester, enrollment_date) VALUES (?, ?, ?, ?)";
            startTime = System.nanoTime();
            PreparedStatement ps = localConn.prepareStatement(insertEnrollmentQuery);
            ps.setInt(1, studentId);
            ps.setString(2, getCourseName(remoteConn, courseId)); // Get course name from remote DB
            ps.setString(3, semester);
            ps.setDate(4, Date.valueOf(enrollmentDate));
            ps.executeUpdate();
            endTime = System.nanoTime();
            long executionTimeC = endTime - startTime;
            System.out.println("Step C: Insertion into local enrollment table executed in " + executionTimeC + " ns");

            // Step D: Update available seats in the remote Course_catalog table
            String updateSeatsQuery = "UPDATE Course_catalog SET available_seats = available_seats - 1 WHERE course_id = ?";
            startTime = System.nanoTime();
            ps = remoteConn.prepareStatement(updateSeatsQuery);
            ps.setInt(1, courseId);
            ps.executeUpdate();
            endTime = System.nanoTime();
            long executionTimeD = endTime - startTime;
            System.out.println("Step D: Update of seats in remote catalog executed in " + executionTimeD + " ns");

            System.out.println("Enrollment completed successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (localConn != null) localConn.close();
                if (remoteConn != null) remoteConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getCourseName(Connection remoteConn, int courseId) throws SQLException {
        String query = "SELECT course_name FROM Course_catalog WHERE course_id = ?";
        PreparedStatement ps = remoteConn.prepareStatement(query);
        ps.setInt(1, courseId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("course_name");
        } else {
            return "Course not found";
        }
    }
}
