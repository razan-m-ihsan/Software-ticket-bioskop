package dao;

import config.DatabaseConnection;
import model.Schedule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {

    public void insert(Schedule s) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "INSERT INTO schedules (movie_id, studio_id, tanggal_tayang, jam_tayang, harga) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setInt(1, s.getMovieId());
            ps.setInt(2, s.getStudioId());
            ps.setString(3, s.getTanggal());
            ps.setString(4, s.getJam());
            ps.setDouble(5, s.getHarga());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getAll() {
        List<String[]> list = new ArrayList<>();

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT s.id, m.judul, st.nama_studio, s.tanggal_tayang, s.jam_tayang, s.harga " +
                    "FROM schedules s " +
                    "JOIN movies m ON s.movie_id = m.id " +
                    "JOIN studios st ON s.studio_id = st.id";

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("id"),
                        rs.getString("judul"),
                        rs.getString("nama_studio"),
                        rs.getString("tanggal_tayang"),
                        rs.getString("jam_tayang"),
                        rs.getString("harga")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}