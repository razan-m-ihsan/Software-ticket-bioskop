package view;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("Admin Dashboard - CinemaTix");
        setMinimumSize(new Dimension(400, 300));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // PANEL UTAMA
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 30, 30));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        // TITLE
        JLabel title = new JLabel("ADMIN DASHBOARD", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(title);
        inner.add(Box.createVerticalStrut(40));

        // ===== BUTTONS =====
        JButton btnFilm   = createAdminButton("Kelola Film",   new Color(52, 152, 219));
        JButton btnJadwal = createAdminButton("Kelola Jadwal", new Color(46, 204, 113));
        JButton btnPromo  = createAdminButton("Kelola Promo",  new Color(155, 89, 182));
        JButton btnLogout = createAdminButton("Logout",        new Color(231, 76, 60));

        inner.add(btnFilm);   inner.add(Box.createVerticalStrut(16));
        inner.add(btnJadwal); inner.add(Box.createVerticalStrut(16));
        inner.add(btnPromo);  inner.add(Box.createVerticalStrut(16));
        inner.add(btnLogout);

        panel.add(inner);
        add(panel);

        // ===== EVENTS =====
        btnFilm.addActionListener(e -> {
            setVisible(false);
            new MovieForm(this).setVisible(true);
        });

        btnJadwal.addActionListener(e -> {
            setVisible(false);
            new ScheduleForm(this).setVisible(true);
        });

        btnPromo.addActionListener(e -> {
            setVisible(false);
            new PromoForm(this).setVisible(true);
        });

        btnLogout.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });
    }

    private JButton createAdminButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(240, 48));
        btn.setMaximumSize(new Dimension(240, 48));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}