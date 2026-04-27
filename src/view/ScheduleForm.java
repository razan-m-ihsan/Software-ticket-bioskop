package view;

import config.DatabaseConnection;
import dao.ScheduleDAO;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ScheduleForm extends JFrame {

    private JComboBox<String> cbFilm, cbStudio;
    private JTextField txtTanggal, txtJam, txtHarga;
    private JTable table;
    private DefaultTableModel model;

    private ArrayList<Integer> filmIds   = new ArrayList<>();
    private ArrayList<Integer> studioIds = new ArrayList<>();

    private ScheduleDAO dao = new ScheduleDAO();

    public ScheduleForm(AdminDashboard adminDashboard) {
        setTitle("Manajemen Jadwal - CinemaTix");
        setMinimumSize(new Dimension(700, 440));
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel panel = new JPanel(new BorderLayout());

        // ===== HEADER WITH BACK BUTTON =====  ← NEW
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton btnBack = new JButton("← Kembali");
        btnBack.setBackground(new Color(189, 195, 199));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBack.setPreferredSize(new Dimension(100, 30));
        btnBack.addActionListener(e -> {
            dispose();
            adminDashboard.setVisible(true);
        });
        headerPanel.add(btnBack, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // ===== FORM =====
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Input Jadwal"));

        cbFilm   = new JComboBox<>();
        cbStudio = new JComboBox<>();
        txtTanggal = new JTextField();
        txtJam     = new JTextField();
        txtHarga   = new JTextField();

        form.add(new JLabel("Film"));       form.add(cbFilm);
        form.add(new JLabel("Studio"));     form.add(cbStudio);
        form.add(new JLabel("Tanggal (YYYY-MM-DD)")); form.add(txtTanggal);
        form.add(new JLabel("Jam (HH:MM:SS)"));       form.add(txtJam);
        form.add(new JLabel("Harga"));      form.add(txtHarga);

        JButton btnTambah = new JButton("Tambah");
        form.add(btnTambah);

        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{
                "ID", "Film", "Studio", "Tanggal", "Jam", "Harga"
        }, 0);

        table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        // ← CHANGED: wrap form+table in contentPanel so header stays at top
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(form,   BorderLayout.NORTH);
        contentPanel.add(scroll, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        add(panel);
        getRootPane().setDefaultButton(btnTambah);

        loadFilm();
        loadStudio();
        loadTable();

        btnTambah.addActionListener(e -> tambahJadwal());
    }

    private void loadFilm() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM movies");
            while (rs.next()) {
                filmIds.add(rs.getInt("id"));
                cbFilm.addItem(rs.getString("judul"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadStudio() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM studios");
            while (rs.next()) {
                studioIds.add(rs.getInt("id"));
                cbStudio.addItem(rs.getString("nama_studio"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadTable() {
        model.setRowCount(0);
        for (String[] row : dao.getAll()) model.addRow(row);
    }

    private void tambahJadwal() {
        try {
            int filmId   = filmIds.get(cbFilm.getSelectedIndex());
            int studioId = studioIds.get(cbStudio.getSelectedIndex());
            String tanggal = txtTanggal.getText();
            String jam     = txtJam.getText();
            double harga   = Double.parseDouble(txtHarga.getText());
            dao.insert(new model.Schedule(0, filmId, studioId, tanggal, jam, harga));
            loadTable();
            JOptionPane.showMessageDialog(this, "Jadwal berhasil ditambahkan!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Input tidak valid!");
        }
    }
}