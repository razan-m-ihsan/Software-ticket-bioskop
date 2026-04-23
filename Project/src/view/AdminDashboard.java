package view;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("Admin Dashboard - CinemaTix");
        setSize(500, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // PANEL UTAMA
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(30, 30, 30)); // dark mode

        // TITLE
        JLabel title = new JLabel("ADMIN DASHBOARD");
        title.setBounds(130, 30, 300, 40);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title);

        // ===== BUTTON KELOLA FILM =====
        JButton btnFilm = new JButton("Kelola Film");
        btnFilm.setBounds(150, 120, 200, 40);
        btnFilm.setBackground(new Color(52, 152, 219));
        btnFilm.setForeground(Color.WHITE);
        btnFilm.setFocusPainted(false);
        btnFilm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(btnFilm);

        // ===== BUTTON KELOLA JADWAL =====
        JButton btnJadwal = new JButton("Kelola Jadwal");
        btnJadwal.setBounds(150, 180, 200, 40);
        btnJadwal.setBackground(new Color(46, 204, 113));
        btnJadwal.setForeground(Color.WHITE);
        btnJadwal.setFocusPainted(false);
        btnJadwal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(btnJadwal);

        // ===== BUTTON KELOLA PROMO =====
        JButton btnPromo = new JButton("Kelola Promo");
        btnPromo.setBounds(150, 240, 200, 40);
        btnPromo.setBackground(new Color(155, 89, 182));
        btnPromo.setForeground(Color.WHITE);
        btnPromo.setFocusPainted(false);
        btnPromo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(btnPromo);

        // ===== BUTTON LOGOUT =====
        JButton btnLogout = new JButton("Logout");
        btnLogout.setBounds(150, 300, 200, 40);
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(btnLogout);

        // ===== EVENT =====
        btnFilm.addActionListener(e -> {
            new MovieForm().setVisible(true);
        });

        btnJadwal.addActionListener(e -> {
            new ScheduleForm().setVisible(true);
        });

        btnPromo.addActionListener(e -> {
            new PromoForm().setVisible(true);
        });

        btnLogout.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        add(panel);
    }
}