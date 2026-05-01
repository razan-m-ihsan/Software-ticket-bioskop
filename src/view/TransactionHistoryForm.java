package view;

import config.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.sql.*;

public class TransactionHistoryForm extends JFrame {

    public TransactionHistoryForm(int userId) {

        setTitle("Riwayat Transaksi");
        setSize(750, 440);
        setMinimumSize(new Dimension(600, 380));
        setResizable(true);
        setLocationRelativeTo(null);

        String[] kolom = {"Film", "Kursi", "Metode", "Harga", "Status", "Waktu"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);
        JTable table = new JTable(model);

        try {
            Connection conn = DatabaseConnection.getConnection();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT m.judul, t.nomor_kursi, t.metode_pembayaran, t.total_harga, t.status, t.waktu_transaksi " +
                    "FROM transactions t " +
                    "JOIN schedules s ON t.schedule_id=s.id " +
                    "JOIN movies m ON s.movie_id=m.id " +
                    "WHERE t.user_id=? " +
                    "ORDER BY t.waktu_transaksi DESC"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getDouble(4),
                        rs.getString(5),
                        rs.getString(6)
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        add(new JScrollPane(table));
    }
}