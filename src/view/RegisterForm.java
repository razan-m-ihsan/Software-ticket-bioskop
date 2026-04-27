package view;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;

public class RegisterForm extends JFrame {

    private JTextField txtNama;
    private JTextField txtEmail;
    private JPasswordField txtPassword;

    public RegisterForm() {
        setTitle("Register CineTix");
        setSize(900, 650);
        setMinimumSize(new Dimension(700, 540));
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

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
        mainPanel.setLayout(new GridBagLayout()); // 🔥 CENTER

        // ===== CARD PANEL =====
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(380, 420));
        card.setBackground(new Color(40, 40, 40));
        card.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 0), 2));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // ===== TITLE =====
        JLabel title = new JLabel("🎬 CineTix");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Create your account");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(Color.LIGHT_GRAY);

        // ===== INPUT =====
        txtNama = new JTextField();
        txtEmail = new JTextField();
        txtPassword = new JPasswordField();

        styleField(txtNama, "Full Name");
        styleField(txtEmail, "Email");
        styleField(txtPassword, "Password");

        // ===== BUTTON REGISTER =====
        JButton btnRegister = new JButton("Register");
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.setBackground(new Color(0, 0, 120));
        btnRegister.setForeground(Color.YELLOW);

        btnRegister.addActionListener(e -> registerAction());

        // ===== LINK LOGIN =====
        JLabel loginLink = new JLabel("Already have an account? Login");
        loginLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginLink.setForeground(Color.WHITE);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginLink.setForeground(Color.YELLOW);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginLink.setForeground(Color.WHITE);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new LoginForm().setVisible(true);
                dispose();
            }
        });

        // ===== SPACING =====
        card.add(Box.createVerticalStrut(25));
        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(20));
        card.add(txtNama);
        card.add(Box.createVerticalStrut(10));
        card.add(txtEmail);
        card.add(Box.createVerticalStrut(10));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(20));
        card.add(btnRegister);
        card.add(Box.createVerticalStrut(15));
        card.add(loginLink);

        mainPanel.add(card);
        add(mainPanel);
    }

    // ===== STYLE FIELD =====
    private void styleField(JTextField field, String placeholder) {
        field.setMaximumSize(new Dimension(260, 35));
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

    // ===== REGISTER LOGIC =====
    private void registerAction() {
        String nama = txtNama.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // Also check against placeholder values
        if (nama.isEmpty() || nama.equals("Full Name") ||
            email.isEmpty() || email.equals("Email") ||
            password.isEmpty() || password.equals("Password")) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        User user = new User();
        user.setNama(nama);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("User");

        UserDAO dao = new UserDAO();
        boolean success = dao.register(user);

        if (success) {
            JOptionPane.showMessageDialog(this, "Register berhasil 🎉");

            new LoginForm().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Register gagal!");
        }
    }
}