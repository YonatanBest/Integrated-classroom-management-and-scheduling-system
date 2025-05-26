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
            initializeDefaultResources(conn);
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

        String createResourcesTable = """
                    CREATE TABLE IF NOT EXISTS Resources (
                        resource_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        room TEXT NOT NULL,
                        resource_type TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CHECK (status IN ('Available', 'In Use', 'Maintenance'))
                    )
                """;

        String createScheduleResourcesTable = """
                    CREATE TABLE IF NOT EXISTS ScheduleResources (
                        schedule_id INTEGER NOT NULL,
                        resource_id INTEGER NOT NULL,
                        quantity_needed INTEGER NOT NULL,
                        FOREIGN KEY (schedule_id) REFERENCES Schedule(schedule_id),
                        FOREIGN KEY (resource_id) REFERENCES Resources(resource_id),
                        PRIMARY KEY (schedule_id, resource_id)
                    )
                """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createCoursesTable);
            stmt.execute(createScheduleTable);
            stmt.execute(createEnrollmentsTable);
            stmt.execute(createResourcesTable);
            stmt.execute(createScheduleResourcesTable);
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

    private static void initializeDefaultResources(Connection conn) throws SQLException {
        // First check if resources already exist
        String checkResources = "SELECT COUNT(*) FROM Resources";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(checkResources);
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Resources already initialized
            }
        }

        // Create a pool of 50 projectors and 50 connectors
        String insertResource = """
                    INSERT INTO Resources (room, resource_type, quantity, status)
                    VALUES (?, ?, ?, 'Available')
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(insertResource)) {
            // Add projectors to the pool
            pstmt.setString(1, "POOL"); // Use "POOL" to indicate these are shared resources
            pstmt.setString(2, "Projector");
            pstmt.setInt(3, 50);
            pstmt.executeUpdate();

            // Add connectors to the pool
            pstmt.setString(1, "POOL");
            pstmt.setString(2, "Connector");
            pstmt.setInt(3, 50);
            pstmt.executeUpdate();
        }
    }
}