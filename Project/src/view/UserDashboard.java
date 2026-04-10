package view;

import config.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class UserDashboard extends JFrame {
    private int userId;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> filmList = new JList<>(listModel);
    private ArrayList<Integer> filmIds = new ArrayList<>();

    private JComboBox<String> cbJadwal;
    private ArrayList<Integer> scheduleIds = new ArrayList<>();

    private JTextField txtJudul;
    private JTextField txtStudio;
    private JTextField txtHarga;

    private int selectedScheduleId = -1;

    public UserDashboard(int userId) {
        this.userId = userId;

        setTitle("CinemaTix");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(45, 45, 45));

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 30));
        header.setPreferredSize(new Dimension(1000, 70));

        JLabel title = new JLabel("🎬 CineTix", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        header.add(title, BorderLayout.CENTER);
        mainPanel.add(header, BorderLayout.NORTH);

        // ===== LEFT PANEL =====
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBackground(new Color(50, 50, 50));

        JLabel lblFilm = new JLabel("Sedang Tayang");
        lblFilm.setForeground(Color.WHITE);
        lblFilm.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        filmList.setBackground(new Color(40, 40, 40));
        filmList.setForeground(Color.WHITE);

        JScrollPane scrollFilm = new JScrollPane(filmList);

        leftPanel.add(lblFilm, BorderLayout.NORTH);
        leftPanel.add(scrollFilm, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // ===== RIGHT PANEL =====
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBackground(new Color(60, 60, 60));

        // ===== MENU =====
        JPanel menuPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        menuPanel.setBounds(50, 20, 600, 100);
        menuPanel.setBackground(new Color(120, 0, 0));

        JButton btnHistory = createButton("📜 Riwayat");
        btnHistory.addActionListener(e ->
                new TransactionHistoryForm(userId).setVisible(true)
        );
        menuPanel.add(btnHistory);

        String[] buttons = {
                "Pilih Jam & Studio",
                "Makanan & Minuman",
                "Reset Pesanan",
                "Pilih Kursi",
                "Lihat Trailer",
                "Gunakan Promo"
        };

        for (String text : buttons) {
            JButton btn = createButton(text);

            if (text.equals("Pilih Kursi")) {
                btn.addActionListener(e -> {
                    if (selectedScheduleId == -1) {
                        JOptionPane.showMessageDialog(this, "Pilih jadwal dulu!");
                        return;
                    }

                    double harga = Double.parseDouble(txtHarga.getText());

                    // ✅ FIX userId (tidak hardcode lagi)
                    new SeatSelectionForm(selectedScheduleId, userId, harga).setVisible(true);
                });
            }

            menuPanel.add(btn);
        }

        rightPanel.add(menuPanel);

        // ===== DETAIL PANEL =====
        JPanel detailPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        detailPanel.setBounds(50, 150, 600, 250);
        detailPanel.setBorder(BorderFactory.createTitledBorder("Detail Pesanan"));
        detailPanel.setBackground(new Color(70, 70, 70));

        txtJudul = new JTextField();
        txtStudio = new JTextField();
        txtHarga = new JTextField();
        cbJadwal = new JComboBox<>();

        styleField(txtJudul);
        styleField(txtStudio);
        styleField(txtHarga);

        detailPanel.add(createLabel("Judul Film"));
        detailPanel.add(txtJudul);

        detailPanel.add(createLabel("Jadwal"));
        detailPanel.add(cbJadwal);

        detailPanel.add(createLabel("Studio"));
        detailPanel.add(txtStudio);

        detailPanel.add(createLabel("Harga"));
        detailPanel.add(txtHarga);

        rightPanel.add(detailPanel);

        // ===== BUTTON BAYAR =====
        JButton btnBayar = new JButton("Confirm & Pay");
        btnBayar.setBounds(200, 420, 300, 50);
        btnBayar.setBackground(new Color(255, 200, 0));
        btnBayar.setFont(new Font("Segoe UI", Font.BOLD, 16));

        btnBayar.addActionListener(e -> {
            if (selectedScheduleId == -1) {
                JOptionPane.showMessageDialog(this, "Pilih jadwal dulu!");
                return;
            }

            JOptionPane.showMessageDialog(this, "Pembayaran berhasil 🎉");
        });

        rightPanel.add(btnBayar);

        mainPanel.add(rightPanel, BorderLayout.CENTER);
        add(mainPanel);

        // ===== LOAD DATA =====
        loadFilm();

        filmList.addListSelectionListener(e -> {
            int index = filmList.getSelectedIndex();

            if (index != -1) {
                int filmId = filmIds.get(index);
                loadDetailFilm(filmId);
                loadJadwal(filmId);
            }
        });

        cbJadwal.addActionListener(e -> {
            int index = cbJadwal.getSelectedIndex();

            if (index != -1) {
                selectedScheduleId = scheduleIds.get(index);
                loadDetailJadwal(selectedScheduleId);
            }
        });
    }

    // ===== STYLE METHODS =====
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(200, 0, 0));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(UIManager.getColor("Button.background"));
            }
        });

        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(30, 30, 30));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    // ===== DATABASE =====
    private void loadFilm() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM movies");

            while (rs.next()) {
                filmIds.add(rs.getInt("id"));
                listModel.addElement(rs.getString("judul"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDetailFilm(int filmId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM movies WHERE id=" + filmId);

            if (rs.next()) {
                txtJudul.setText(rs.getString("judul"));
                txtStudio.setText("");
                txtHarga.setText("");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadJadwal(int filmId) {
        try {
            scheduleIds.clear();
            cbJadwal.removeAllItems();

            Connection conn = DatabaseConnection.getConnection();

            String query = "SELECT s.id, s.jam_tayang, st.nama_studio, s.harga " +
                    "FROM schedules s " +
                    "JOIN studios st ON s.studio_id = st.id " +
                    "WHERE s.movie_id = " + filmId;

            ResultSet rs = conn.createStatement().executeQuery(query);

            while (rs.next()) {
                scheduleIds.add(rs.getInt("id"));

                String item = rs.getString("jam_tayang") +
                        " | " + rs.getString("nama_studio");

                cbJadwal.addItem(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDetailJadwal(int scheduleId) {
        try {
            Connection conn = DatabaseConnection.getConnection();

            String query = "SELECT s.harga, st.nama_studio " +
                    "FROM schedules s " +
                    "JOIN studios st ON s.studio_id = st.id " +
                    "WHERE s.id = " + scheduleId;

            ResultSet rs = conn.createStatement().executeQuery(query);

            if (rs.next()) {
                txtStudio.setText(rs.getString("nama_studio"));
                txtHarga.setText(rs.getString("harga"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}