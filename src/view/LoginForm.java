package view;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;

public class LoginForm extends JFrame {

    private JTextField txtEmail;
    private JPasswordField txtPassword;

    public LoginForm() {
        setTitle("CineTix Login");
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 480));
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ===== MAIN PANEL (GRADIENT BACKGROUND) =====
        JPanel mainPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(30, 0, 0),
                        0, getHeight(), new Color(80, 0, 0)
                );

                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout()); // 🔥 CENTER FIX

        // ===== CARD PANEL =====
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(350, 350));
        card.setBackground(new Color(40, 40, 40));
        card.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 0), 2));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // ===== TITLE =====
        JLabel title = new JLabel("🎬 CineTix");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Login to your account");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(Color.LIGHT_GRAY);

        // ===== INPUT =====
        txtEmail = new JTextField();
        txtPassword = new JPasswordField();

        styleField(txtEmail, "Email");
        styleField(txtPassword, "Password");

        // ===== BUTTON LOGIN =====
        JButton btnLogin = new JButton("Login");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setBackground(new Color(0, 0, 120));
        btnLogin.setForeground(Color.YELLOW);

        btnLogin.addActionListener(e -> loginAction());

        // ===== LINK REGISTER =====
        JLabel registerLink = new JLabel("Don't have an account? Register");
        registerLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerLink.setForeground(Color.WHITE);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

        registerLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerLink.setForeground(Color.YELLOW);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerLink.setForeground(Color.WHITE);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new RegisterForm().setVisible(true);
                dispose();
            }
        });

        // ===== SPACING =====
        card.add(Box.createVerticalStrut(20));
        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(20));
        card.add(txtEmail);
        card.add(Box.createVerticalStrut(10));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(20));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(15));
        card.add(registerLink);

        mainPanel.add(card);
        add(mainPanel);
    }

    // ===== STYLE FIELD =====
    private void styleField(JTextField field, String placeholder) {
        field.setMaximumSize(new Dimension(250, 35));
        field.setBackground(new Color(20, 20, 20));
        field.setForeground(Color.GRAY);
        field.setCaretColor(Color.YELLOW);
        field.setBorder(BorderFactory.createLineBorder(Color.ORANGE));

        field.setText(placeholder);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.WHITE);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    // ===== LOGIN =====
    private void loginAction() {
    String email = txtEmail.getText().trim();
    String password = new String(txtPassword.getPassword()).trim();

    // 🔥 DEBUG (LIHAT INPUT ASLI)
    System.out.println("Email input: [" + email + "]");
    System.out.println("Password input: [" + password + "]");

    // 🔥 VALIDASI (FIX PLACEHOLDER)
    if (email.equals("Email") || password.equals("Password") 
            || email.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Isi email dan password dulu!");
        return;
    }

    UserDAO dao = new UserDAO();
    User user = dao.login(email, password);

    if (user != null) {
        JOptionPane.showMessageDialog(this, "Login berhasil sebagai " + user.getRole());

        dispose();

        if (user.getRole().equalsIgnoreCase("Admin")) {
            new AdminDashboard().setVisible(true);
        } else {
            new UserDashboard(user.getId()).setVisible(true);
        }

        } else {
            JOptionPane.showMessageDialog(this, "Login gagal!");
        }
    }
}