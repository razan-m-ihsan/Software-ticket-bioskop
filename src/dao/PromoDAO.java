package dao;

import config.DatabaseConnection;
import model.Promo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromoDAO {

    // 🔹 CREATE
    public void insert(Promo promo) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "INSERT INTO promos (kode_promo, deskripsi, diskon_persen, diskon_rupiah, tanggal_mulai, tanggal_akhir, aktif) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, promo.getKodePromo());
            ps.setString(2, promo.getDeskripsi());
            ps.setDouble(3, promo.getDiskonPersen());
            ps.setDouble(4, promo.getDiskonRupiah());
            ps.setString(5, promo.getTanggalMulai());
            ps.setString(6, promo.getTanggalAkhir());
            ps.setBoolean(7, promo.isAktif());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 READ ALL
    public List<Promo> getAll() {
        List<Promo> list = new ArrayList<>();

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM promos";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                Promo p = new Promo(
                        rs.getInt("id"),
                        rs.getString("kode_promo"),
                        rs.getString("deskripsi"),
                        rs.getDouble("diskon_persen"),
                        rs.getDouble("diskon_rupiah"),
                        rs.getString("tanggal_mulai"),
                        rs.getString("tanggal_akhir"),
                        rs.getBoolean("aktif")
                );
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // 🔹 READ BY ID
    public Promo getById(int id) {
        Promo promo = null;

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM promos WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                promo = new Promo(
                        rs.getInt("id"),
                        rs.getString("kode_promo"),
                        rs.getString("deskripsi"),
                        rs.getDouble("diskon_persen"),
                        rs.getDouble("diskon_rupiah"),
                        rs.getString("tanggal_mulai"),
                        rs.getString("tanggal_akhir"),
                        rs.getBoolean("aktif")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return promo;
    }

    // 🔹 READ ACTIVE PROMOS
    public List<Promo> getActivePromos() {
        List<Promo> list = new ArrayList<>();

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM promos WHERE aktif = true AND tanggal_mulai <= CURRENT_DATE AND tanggal_akhir >= CURRENT_DATE";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                Promo p = new Promo(
                        rs.getInt("id"),
                        rs.getString("kode_promo"),
                        rs.getString("deskripsi"),
                        rs.getDouble("diskon_persen"),
                        rs.getDouble("diskon_rupiah"),
                        rs.getString("tanggal_mulai"),
                        rs.getString("tanggal_akhir"),
                        rs.getBoolean("aktif")
                );
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // 🔹 UPDATE
    public void update(Promo promo) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "UPDATE promos SET kode_promo = ?, deskripsi = ?, diskon_persen = ?, diskon_rupiah = ?, tanggal_mulai = ?, tanggal_akhir = ?, aktif = ? WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, promo.getKodePromo());
            ps.setString(2, promo.getDeskripsi());
            ps.setDouble(3, promo.getDiskonPersen());
            ps.setDouble(4, promo.getDiskonRupiah());
            ps.setString(5, promo.getTanggalMulai());
            ps.setString(6, promo.getTanggalAkhir());
            ps.setBoolean(7, promo.isAktif());
            ps.setInt(8, promo.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 DELETE
    public void delete(int id) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "DELETE FROM promos WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}