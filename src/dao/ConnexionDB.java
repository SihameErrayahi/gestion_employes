package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionDB {

	private static final String URL = "jdbc:mysql://localhost:3306/gestion_employes";
    private static final String USER = "root";
    private static final String PASSWORD = "sihame1313"; // ton mot de passe MySQL ici

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
}