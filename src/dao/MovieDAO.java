package dao;

import config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Movie;

public class MovieDAO {

    // 🔹 CREATE
    public void insert(Movie movie) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "INSERT INTO movies (judul, sinopsis, durasi, genre, rating_usia, poster_url) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, movie.getJudul());
            ps.setString(2, movie.getSinopsis());
            ps.setInt(3, movie.getDurasi());
            ps.setString(4, movie.getGenre());
            ps.setString(5, movie.getRatingUsia());
            ps.setString(6, movie.getPosterUrl());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 READ
    public List<Movie> getAll() {
        List<Movie> list = new ArrayList<>();

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM movies";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                Movie m = new Movie(
                        rs.getInt("id"),
                        rs.getString("judul"),
                        rs.getString("sinopsis"),
                        rs.getInt("durasi"),
                        rs.getString("genre"),
                        rs.getString("rating_usia"),
                        rs.getString("poster_url")
                );
                list.add(m);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // 🔹 UPDATE
    public void update(Movie movie) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "UPDATE movies SET judul=?, sinopsis=?, durasi=?, genre=?, rating_usia=?, poster_url=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, movie.getJudul());
            ps.setString(2, movie.getSinopsis());
            ps.setInt(3, movie.getDurasi());
            ps.setString(4, movie.getGenre());
            ps.setString(5, movie.getRatingUsia());
            ps.setString(6, movie.getPosterUrl());
            ps.setInt(7, movie.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 DELETE
    public void delete(int id) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "DELETE FROM movies WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}