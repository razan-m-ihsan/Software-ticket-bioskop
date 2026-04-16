package dao;

import config.DatabaseConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public boolean register(User user) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        System.out.println("Database: " + conn.getCatalog());

        String query = "INSERT INTO users (nama, email, password, role) VALUES (?, ?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, user.getNama());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPassword());
        ps.setString(4, user.getRole());

        int result = ps.executeUpdate();

        return result > 0;

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

    public User login(String email, String password) {
        User user = null;

        try {
            Connection conn = DatabaseConnection.getConnection();

            String query = "SELECT * FROM users WHERE email=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, password);

            System.out.println("Query jalan dengan:");
            System.out.println("Email: [" + email + "]");
            System.out.println("Password: [" + password + "]");

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("USER KETEMU");
                user = new User();
                user.setId(rs.getInt("id"));
                user.setNama(rs.getString("nama"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
            }
            else {
                System.out.println("USER TIDAK KETEMU");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }
}