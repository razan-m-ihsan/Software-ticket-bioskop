package view;

import dao.MovieDAO;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Movie;

public class MovieForm extends JFrame {

    private JTextField txtJudul, txtDurasi, txtGenre, txtRating, txtPoster;
    private JTextArea txtSinopsis;
    private JTable table;
    private DefaultTableModel model;

    private MovieDAO dao = new MovieDAO();
    private int selectedId = -1;

    public MovieForm(AdminDashboard adminDashboard) {
        setTitle("Manajemen Film - CinemaTix");
        setMinimumSize(new Dimension(650, 420));
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

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
            adminDashboard.setVisible(true);
        });
        headerPanel.add(btnBack, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // ===== FORM =====
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Input Film"));

        txtJudul = new JTextField();
        txtSinopsis = new JTextArea(3, 20);
        txtDurasi = new JTextField();
        txtGenre = new JTextField();
        txtRating = new JTextField();
        txtPoster = new JTextField();

        JButton btnTambah = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus  = new JButton("Hapus");
        JButton btnBrowsePoster = new JButton("Pilih Poster");

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

        formPanel.add(new JLabel("Poster"));
        JPanel posterPanel = new JPanel(new BorderLayout(6, 0));
        posterPanel.add(txtPoster, BorderLayout.CENTER);
        posterPanel.add(btnBrowsePoster, BorderLayout.EAST);
        formPanel.add(posterPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPanel.add(btnTambah);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnHapus);

        formPanel.add(buttonPanel);
        formPanel.add(new JLabel());

        btnBrowsePoster.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Pilih File Poster");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Gambar Poster", "jpg", "jpeg", "png", "gif"));
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                txtPoster.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{
                "ID", "Judul", "Durasi", "Genre", "Rating", "Poster"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

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
        getRootPane().setDefaultButton(btnTambah);

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
                    txtPoster.setText(model.getValueAt(row, 5) != null ? model.getValueAt(row, 5).toString() : "");
            }
        });
    }

    private void loadTable() {
        model.setRowCount(0);
        List<Movie> list = dao.getAll();
        for (Movie m : list) {
            model.addRow(new Object[]{
                    m.getId(), m.getJudul(), m.getDurasi(), m.getGenre(), m.getRatingUsia(), m.getPosterUrl()
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
            String posterUrl = storePosterToAssets(txtPoster.getText());
            m.setPosterUrl(posterUrl);
            txtPoster.setText(posterUrl);
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
            String posterUrl = storePosterToAssets(txtPoster.getText());
            m.setPosterUrl(posterUrl);
            txtPoster.setText(posterUrl);
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
        txtPoster.setText("");
        selectedId = -1;
    }

    private String storePosterToAssets(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return "";
        }

        String normalized = posterPath.trim();
        File sourceFile = new File(normalized);
        File assetsDir = new File("src/assets");
        if (!assetsDir.exists()) {
            assetsDir.mkdirs();
        }

        try {
            if (!sourceFile.exists()) {
                if (normalized.startsWith("assets/") || normalized.startsWith("assets\\")) {
                    return normalized.replace('\\', '/');
                }
                if (normalized.startsWith("src" + File.separator + "assets")) {
                    return normalized.substring(4).replace('\\', '/');
                }
                return normalized;
            }

            Path sourcePath = sourceFile.toPath().toAbsolutePath();
            Path assetsPath = assetsDir.toPath().toAbsolutePath();
            if (sourcePath.startsWith(assetsPath)) {
                Path relative = assetsPath.relativize(sourcePath);
                return "assets/" + relative.toString().replace('\\', '/');
            }

            String filename = sourceFile.getName();
            int dotIndex = filename.lastIndexOf('.');
            String baseName = dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
            String extension = dotIndex > 0 ? filename.substring(dotIndex) : "";
            String safeBase = baseName.replaceAll("[^a-zA-Z0-9-_]", "_");
            String targetName = safeBase + "_" + System.currentTimeMillis() + extension;
            Path targetPath = assetsPath.resolve(targetName);
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "assets/" + targetName;
        } catch (Exception ex) {
            ex.printStackTrace();
            return normalized;
        }
    }
}