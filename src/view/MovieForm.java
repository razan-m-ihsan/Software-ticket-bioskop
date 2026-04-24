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
        setSize(800, 520);
        setMinimumSize(new Dimension(650, 420));
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));

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
            new AdminDashboard().setVisible(true);
        });
        headerPanel.add(btnBack, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // ===== FORM =====
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Input Film"));

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

        JButton btnTambah = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus  = new JButton("Hapus");

        formPanel.add(btnTambah);
        formPanel.add(btnUpdate);
        formPanel.add(btnHapus);

        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{
                "ID", "Judul", "Durasi", "Genre", "Rating"
        }, 0);

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // ← CHANGED: wrap form+table in contentPanel so header stays at top
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.add(formPanel,  BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        add(panel);

        loadTable();

        btnTambah.addActionListener(e -> tambahFilm());
        btnUpdate.addActionListener(e -> updateFilm());
        btnHapus.addActionListener(e  -> hapusFilm());

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

    private void loadTable() {
        model.setRowCount(0);
        List<Movie> list = dao.getAll();
        for (Movie m : list) {
            model.addRow(new Object[]{
                    m.getId(), m.getJudul(), m.getDurasi(), m.getGenre(), m.getRatingUsia()
            });
        }
    }

    private void tambahFilm() {
        try {
            Movie m = new Movie();
            m.setJudul(txtJudul.getText());
            m.setSinopsis(txtSinopsis.getText());
            m.setDurasi(Integer.parseInt(txtDurasi.getText()));
            m.setGenre(txtGenre.getText());
            m.setRatingUsia(txtRating.getText());
            dao.insert(m);
            loadTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Film berhasil ditambahkan!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Input tidak valid!");
        }
    }

    private void updateFilm() {
        if (selectedId == -1) { JOptionPane.showMessageDialog(this, "Pilih data dulu!"); return; }
        try {
            Movie m = new Movie();
            m.setId(selectedId);
            m.setJudul(txtJudul.getText());
            m.setSinopsis(txtSinopsis.getText());
            m.setDurasi(Integer.parseInt(txtDurasi.getText()));
            m.setGenre(txtGenre.getText());
            m.setRatingUsia(txtRating.getText());
            dao.update(m);
            loadTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Film berhasil diupdate!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Input tidak valid!");
        }
    }

    private void hapusFilm() {
        int row = table.getSelectedRow();
        if (row != -1) {
            dao.delete((int) model.getValueAt(row, 0));
            loadTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Film berhasil dihapus!");
        } else {
            JOptionPane.showMessageDialog(this, "Pilih data dulu!");
        }
    }

    private void clearForm() {
        txtJudul.setText(""); txtSinopsis.setText("");
        txtDurasi.setText(""); txtGenre.setText(""); txtRating.setText("");
        selectedId = -1;
    }
}