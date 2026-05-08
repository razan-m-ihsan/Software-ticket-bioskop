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
    private int selectedId = -1;

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
        panel.setBackground(new Color(245, 245, 245));

        // ===== HEADER WITH BACK BUTTON =====
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
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Input Jadwal"));
        formPanel.setBackground(new Color(245, 245, 245));

        cbFilm   = new JComboBox<>();
        cbStudio = new JComboBox<>();
        txtTanggal = new JTextField();
        txtJam     = new JTextField();
        txtHarga   = new JTextField();

        formPanel.add(new JLabel("Film"));
        formPanel.add(cbFilm);
        formPanel.add(new JLabel("Studio"));
        formPanel.add(cbStudio);
        formPanel.add(new JLabel("Tanggal (YYYY-MM-DD)"));
        formPanel.add(txtTanggal);
        formPanel.add(new JLabel("Jam (HH:MM:SS)"));
        formPanel.add(txtJam);
        formPanel.add(new JLabel("Harga"));
        formPanel.add(txtHarga);

        JButton btnTambah = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus  = new JButton("Hapus");
        JButton btnReset  = new JButton("Reset");

        btnTambah.setBackground(new Color(52, 152, 219));
        btnTambah.setForeground(Color.WHITE);
        btnTambah.setFocusPainted(false);

        btnUpdate.setBackground(new Color(46, 204, 113));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);

        btnHapus.setBackground(new Color(231, 76, 60));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setFocusPainted(false);

        btnReset.setBackground(new Color(149, 165, 166));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFocusPainted(false);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(btnTambah);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnReset);

        formPanel.add(buttonPanel);

        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{
                "ID", "Film", "Studio", "Tanggal", "Jam", "Harga"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.add(formPanel, BorderLayout.NORTH);
        contentPanel.add(scroll, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        add(panel);
        getRootPane().setDefaultButton(btnTambah);

        loadFilm();
        loadStudio();
        loadTable();

        btnTambah.addActionListener(e -> tambahJadwal());
        btnUpdate.addActionListener(e -> updateJadwal());
        btnHapus.addActionListener(e -> hapusJadwal());
        btnReset.addActionListener(e -> resetForm());

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
                txtTanggal.setText(model.getValueAt(row, 3).toString());
                txtJam.setText(model.getValueAt(row, 4).toString());
                txtHarga.setText(model.getValueAt(row, 5).toString());
                cbFilm.setSelectedItem(model.getValueAt(row, 1).toString());
                cbStudio.setSelectedItem(model.getValueAt(row, 2).toString());
            }
        });
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
            resetForm();
            JOptionPane.showMessageDialog(this, "Jadwal berhasil ditambahkan!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Input tidak valid!");
        }
    }

    private void updateJadwal() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang akan diupdate!");
            return;
        }

        try {
            int filmId   = filmIds.get(cbFilm.getSelectedIndex());
            int studioId = studioIds.get(cbStudio.getSelectedIndex());
            String tanggal = txtTanggal.getText();
            String jam     = txtJam.getText();
            double harga   = Double.parseDouble(txtHarga.getText());
            dao.update(new model.Schedule(selectedId, filmId, studioId, tanggal, jam, harga));
            loadTable();
            resetForm();
            JOptionPane.showMessageDialog(this, "Jadwal berhasil diupdate!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Input tidak valid!");
        }
    }

    private void hapusJadwal() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus jadwal ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dao.delete(selectedId);
            loadTable();
            resetForm();
            JOptionPane.showMessageDialog(this, "Jadwal berhasil dihapus!");
        }
    }

    private void resetForm() {
        selectedId = -1;
        cbFilm.setSelectedIndex(0);
        cbStudio.setSelectedIndex(0);
        txtTanggal.setText("");
        txtJam.setText("");
        txtHarga.setText("");
        table.clearSelection();
    }
}