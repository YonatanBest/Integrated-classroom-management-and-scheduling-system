package com.classroom.util;

import java.sql.*;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:sqlite:classroom.db";

    public static void initializeDatabase() {
        Connection conn = null;
        try {
            conn = getConnection();
            createTables(conn);
            insertDefaultAdmin(conn);
            conn.commit(); // Commit the changes
            conn.close();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback on error
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            System.err.println("Failed to initialize database: " + e.getMessage());
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        conn.setAutoCommit(false); // Enable transaction management
        return conn;
    }

    private static void createTables(Connection conn) throws SQLException {
        String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        full_name TEXT NOT NULL,
                        user_type TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        program_type TEXT,
                        assigned_room TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """;

        String createCoursesTable = """
                    CREATE TABLE IF NOT EXISTS Courses (
                        course_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        course_name TEXT NOT NULL,
                        course_code TEXT UNIQUE NOT NULL,
                        description TEXT,
                        credit_hours INTEGER NOT NULL
                    )
                """;

        // In the createTables method, update the Schedule table creation:
        String createScheduleTable = """
                    CREATE TABLE IF NOT EXISTS Schedule (
                        schedule_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        course_id INTEGER NOT NULL,
                        instructor_id INTEGER NOT NULL,
                        day_of_week TEXT NOT NULL,
                        start_time TEXT NOT NULL,
                        end_time TEXT NOT NULL,
                        room TEXT NOT NULL,
                        program_type TEXT NOT NULL,
                        FOREIGN KEY (course_id) REFERENCES Courses(course_id),
                        FOREIGN KEY (instructor_id) REFERENCES Users(user_id),
                        CHECK (start_time < end_time),
                        CHECK (program_type IN ('Regular', 'Evening'))
                    )
                """;

        String createEnrollmentsTable = """
                    CREATE TABLE IF NOT EXISTS Enrollments (
                        enrollment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        student_id INTEGER NOT NULL,
                        course_id INTEGER NOT NULL,
                        enrollment_date TEXT NOT NULL,
                        FOREIGN KEY (student_id) REFERENCES Users(user_id),
                        FOREIGN KEY (course_id) REFERENCES Courses(course_id),
                        UNIQUE(student_id, course_id)
                    )
                """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createCoursesTable);
            stmt.execute(createScheduleTable);
            stmt.execute(createEnrollmentsTable);
        }
    }

    private static void insertDefaultAdmin(Connection conn) throws SQLException {
        String checkAdmin = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        String insertAdmin = """
                    INSERT INTO users (username, password, full_name, user_type, email)
                    VALUES ('admin', 'admin123', 'System Administrator', 'coordinator', 'admin@example.com')
                """;

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(checkAdmin);
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute(insertAdmin);
            }
        }
    }
}