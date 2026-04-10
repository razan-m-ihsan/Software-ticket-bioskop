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
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));

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
        JButton btnHapus = new JButton("Hapus");

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

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);

        // LOAD DATA
        loadTable();

        // ===== EVENT BUTTON =====
        btnTambah.addActionListener(e -> tambahFilm());
        btnUpdate.addActionListener(e -> updateFilm());
        btnHapus.addActionListener(e -> hapusFilm());

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

    // ===== UPDATE =====
    private void updateFilm() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data dulu!");
            return;
        }

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

    // ===== DELETE =====
    private void hapusFilm() {
        int row = table.getSelectedRow();

        if (row != -1) {
            int id = (int) model.getValueAt(row, 0);

            dao.delete(id);
            loadTable();
            clearForm();

            JOptionPane.showMessageDialog(this, "Film berhasil dihapus!");
        } else {
            JOptionPane.showMessageDialog(this, "Pilih data dulu!");
        }
    }

    // ===== CLEAR FORM =====
    private void clearForm() {
        txtJudul.setText("");
        txtSinopsis.setText("");
        txtDurasi.setText("");
        txtGenre.setText("");
        txtRating.setText("");
        selectedId = -1;
    }
}