package tn.esprit.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserActivityLogger {
    public static void logActivity(int userId, String action, String details) {
        String sql = "INSERT INTO user_activity_log (user_id, action, details) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to log user activity: " + e.getMessage());
        }
    }
}
