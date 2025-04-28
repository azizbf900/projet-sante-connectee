package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private static final String URL = "jdbc:mysql://localhost:3306/vitalink"; // change selon ton nom de base
    private static final String USER = "root";
    private static final String PASSWORD = ""; // ou "root" si tu es sur Mac, MAMP...

    private static Connection connection;

    private MyDatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion à la base de données établie.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base : " + e.getMessage());
        }
    }

    public static Connection getCon() {
        if (connection == null) {
            new MyDatabase();
        }
        return connection;
    }
}
