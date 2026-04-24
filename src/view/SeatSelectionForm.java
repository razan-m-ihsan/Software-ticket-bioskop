package view;

import config.DatabaseConnection;
import util.QRCodeGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.ArrayList;

public class SeatSelectionForm extends JFrame {

    private ArrayList<String> selectedSeats = new ArrayList<>();
    private ArrayList<String> bookedSeats = new ArrayList<>();

    private int scheduleId;
    private int userId;
    private double harga;

    private JLabel lblTotal;

    public SeatSelectionForm(int scheduleId, int userId, double harga) {
        this.scheduleId = scheduleId;
        this.userId = userId;
        this.harga = harga;

        setTitle("CinemaTix - Pilih Kursi");
        setSize(750, 580);
        setMinimumSize(new Dimension(620, 480));
        setResizable(true);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.BLACK);

        JLabel screen = new JLabel("LAYAR BIOSKOP", JLabel.CENTER);
        screen.setOpaque(true);
        screen.setBackground(Color.DARK_GRAY);
        screen.setForeground(Color.WHITE);
        screen.setPreferredSize(new Dimension(600, 40));
        main.add(screen, BorderLayout.NORTH);

        loadBookedSeats();

        JPanel grid = new JPanel(new GridLayout(5, 8, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        grid.setBackground(Color.BLACK);

        char row = 'A';

        for (int i = 0; i < 5; i++) {
            for (int j = 1; j <= 8; j++) {

                String seat = row + "" + j;
                JButton btn = new JButton(seat);

                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);

                if (bookedSeats.contains(seat)) {
                    btn.setBackground(Color.GRAY);
                    btn.setEnabled(false);
                } else {
                    btn.setBackground(Color.GREEN);

                    btn.addActionListener(e -> toggleSeat(btn, seat));
                }

                grid.add(btn);
            }
            row++;
        }

        main.add(grid, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(2,1));
        bottom.setBackground(Color.BLACK);

        lblTotal = new JLabel("Total: Rp 0", JLabel.CENTER);
        lblTotal.setForeground(Color.WHITE);

        JButton bayar = new JButton("Confirm & Pay");
        bayar.setBackground(Color.ORANGE);

        bayar.addActionListener(e -> {
            if (selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pilih kursi dulu!");
                return;
            }

            saveTransaction();
        });

        bottom.add(lblTotal);
        bottom.add(bayar);

        main.add(bottom, BorderLayout.SOUTH);

        add(main);
    }

    private void loadBookedSeats() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT nomor_kursi FROM transactions WHERE schedule_id=?"
            );
            ps.setInt(1, scheduleId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                bookedSeats.add(rs.getString("nomor_kursi"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleSeat(JButton btn, String seat) {
        if (selectedSeats.contains(seat)) {
            selectedSeats.remove(seat);
            btn.setBackground(Color.GREEN);
        } else {
            selectedSeats.add(seat);
            btn.setBackground(Color.RED);
        }

        updateTotal();
    }

    private void updateTotal() {
        double total = selectedSeats.size() * harga;
        lblTotal.setText(String.format("Total: Rp %,.0f", total));
    }

    private void saveTransaction() {
        try {
            Connection conn = DatabaseConnection.getConnection();

            for (String seat : selectedSeats) {

                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO transactions(user_id,schedule_id,nomor_kursi,total_harga,metode_pembayaran,status) VALUES(?,?,?,?,?,?)"
                );

                ps.setInt(1, userId);
                ps.setInt(2, scheduleId);
                ps.setString(3, seat);
                ps.setDouble(4, harga);
                ps.setString(5, "QRIS");
                ps.setString(6, "SUCCESS");

                ps.executeUpdate();
            }

            // QR
            String qrText = "CinemaTix\nSeats: " + selectedSeats +
                    "\nSchedule: " + scheduleId;

            BufferedImage qr = QRCodeGenerator.generateQR(qrText);

            JOptionPane.showMessageDialog(this,
                    new JLabel(new ImageIcon(qr)),
                    "E-Ticket",
                    JOptionPane.PLAIN_MESSAGE);

            // Save file
            java.io.FileWriter w = new java.io.FileWriter("ticket.txt");
            w.write("CinemaTix Ticket\nSeats: " + selectedSeats);
            w.close();

            dispose();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}