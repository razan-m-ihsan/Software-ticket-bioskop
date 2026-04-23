package view;

import dao.MovieDAO;
import model.Movie;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MovieForm extends JFrame {

    private JTextField txtJudul, txtDurasi, txtGenre, txtRating;
    private JTextArea txtSinopsis;
    private JTable table;
    private DefaultTableModel model;

    private MovieDAO dao = new MovieDAO();
    private int selectedId = -1;

    public MovieForm() {
        setTitle("Manajemen Film - CinemaTix");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));

        // ===== HEADER PANEL WITH BACK BUTTON =====
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
            new AdminDashboard();
        });
        headerPanel.add(btnBack, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // ===== FORM PANEL =====
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Input Film"));
        formPanel.setBackground(new Color(245, 245, 245));

        txtJudul = new JTextField();
        txtSinopsis = new JTextArea(3, 20);
        txtDurasi = new JTextField();
        txtGenre = new JTextField();
        txtRating = new JTextField();

        formPanel.add(new JLabel("Judul"));
        formPanel.add(txtJudul);

        formPanel.add(new JLabel("Sinopsis"));
        formPanel.add(new JScrollPane(txtSinopsis));

        formPanel.add(new JLabel("Durasi"));
        formPanel.add(txtDurasi);

        formPanel.add(new JLabel("Genre"));
        formPanel.add(txtGenre);

        formPanel.add(new JLabel("Rating"));
        formPanel.add(txtRating);

        // ===== BUTTONS =====
        JButton btnTambah = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus = new JButton("Hapus");
        JButton btnReset = new JButton("Reset");

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

        // ===== TABLE PANEL =====
        model = new DefaultTableModel(new String[]{
                "ID", "Judul", "Durasi", "Genre", "Rating"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.add(formPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);
        add(panel);

        // LOAD DATA
        loadTable();

        // ===== EVENT BUTTON =====
        btnTambah.addActionListener(e -> tambahFilm());
        btnUpdate.addActionListener(e -> updateFilm());
        btnHapus.addActionListener(e -> hapusFilm());
        btnReset.addActionListener(e -> resetForm());

        // ===== EVENT KLIK TABEL =====
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();

            if (row != -1) {
                selectedId = (int) model.getValueAt(row, 0);
                txtJudul.setText(model.getValueAt(row, 1).toString());
                txtDurasi.setText(model.getValueAt(row, 2).toString());
                txtGenre.setText(model.getValueAt(row, 3).toString());
                txtRating.setText(model.getValueAt(row, 4).toString());
            }
        });
    }

    // ===== LOAD DATA =====
    private void loadTable() {
        model.setRowCount(0);
        List<Movie> list = dao.getAll();

        for (Movie m : list) {
            model.addRow(new Object[]{
                    m.getId(),
                    m.getJudul(),
                    m.getDurasi(),
                    m.getGenre(),
                    m.getRatingUsia()
            });
        }
    }

    // ===== TAMBAH =====
    private void tambahFilm() {
        try {
            if (txtJudul.getText().trim().isEmpty() || txtDurasi.getText().trim().isEmpty() ||
                txtGenre.getText().trim().isEmpty() || txtRating.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Movie m = new Movie();
            m.setJudul(txtJudul.getText());
            m.setSinopsis(txtSinopsis.getText());
            m.setDurasi(Integer.parseInt(txtDurasi.getText()));
            m.setGenre(txtGenre.getText());
            m.setRatingUsia(txtRating.getText());

            dao.insert(m);
            loadTable();
            resetForm();

            JOptionPane.showMessageDialog(this, "Film berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Durasi harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== UPDATE =====
    private void updateFilm() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih film yang akan diupdate!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (txtJudul.getText().trim().isEmpty() || txtDurasi.getText().trim().isEmpty() ||
                txtGenre.getText().trim().isEmpty() || txtRating.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Movie m = new Movie();
            m.setId(selectedId);
            m.setJudul(txtJudul.getText());
            m.setSinopsis(txtSinopsis.getText());
            m.setDurasi(Integer.parseInt(txtDurasi.getText()));
            m.setGenre(txtGenre.getText());
            m.setRatingUsia(txtRating.getText());

            dao.update(m);
            loadTable();
            resetForm();

            JOptionPane.showMessageDialog(this, "Film berhasil diupdate!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Durasi harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== DELETE =====
    private void hapusFilm() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih film yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus film ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dao.delete(selectedId);
            JOptionPane.showMessageDialog(this, "Film berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            loadTable();
        }
    }

    // ===== RESET FORM =====
    private void resetForm() {
        txtJudul.setText("");
        txtSinopsis.setText("");
        txtDurasi.setText("");
        txtGenre.setText("");
        txtRating.setText("");
        selectedId = -1;
    }
}