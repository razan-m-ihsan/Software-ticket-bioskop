package view;

import dao.PromoDAO;
import model.Promo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PromoForm extends JFrame {

    private JTextField txtKodePromo, txtDeskripsi, txtDiskonPersen, txtDiskonRupiah;
    private JTextField txtTanggalMulai, txtTanggalAkhir;
    private JCheckBox chkAktif;
    private JTable table;
    private DefaultTableModel model;

    private PromoDAO dao = new PromoDAO();
    private int selectedId = -1;

    public PromoForm(AdminDashboard adminDashboard) {
        setTitle("Manajemen Promo - CinemaTix");
        setMinimumSize(new Dimension(700, 460));
        setResizable(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));

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
            adminDashboard.setVisible(true);
        });
        headerPanel.add(btnBack, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));

        // ===== FORM PANEL =====
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Input Promo"));
        formPanel.setBackground(new Color(245, 245, 245));

        txtKodePromo = new JTextField();
        txtDeskripsi = new JTextField();
        txtDiskonPersen = new JTextField();
        txtDiskonRupiah = new JTextField();
        txtTanggalMulai = new JTextField();
        txtTanggalAkhir = new JTextField();
        chkAktif = new JCheckBox("Aktif");

        formPanel.add(new JLabel("Kode Promo *"));
        formPanel.add(txtKodePromo);

        formPanel.add(new JLabel("Deskripsi"));
        formPanel.add(txtDeskripsi);

        formPanel.add(new JLabel("Diskon Persen (%)"));
        formPanel.add(txtDiskonPersen);

        formPanel.add(new JLabel("Diskon Rupiah (Rp)"));
        formPanel.add(txtDiskonRupiah);

        formPanel.add(new JLabel("Tanggal Mulai (YYYY-MM-DD) *"));
        formPanel.add(txtTanggalMulai);

        formPanel.add(new JLabel("Tanggal Akhir (YYYY-MM-DD) *"));
        formPanel.add(txtTanggalAkhir);

        formPanel.add(chkAktif);
        formPanel.add(new JLabel());

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
                "ID", "Kode Promo", "Deskripsi", "Diskon %", "Diskon Rp", "Mulai", "Akhir", "Aktif"
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

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(panel, BorderLayout.CENTER);
        add(mainPanel);

        // LOAD DATA
        loadTable();

        // ===== EVENT BUTTON =====
        btnTambah.addActionListener(e -> tambahPromo());
        btnUpdate.addActionListener(e -> updatePromo());
        btnHapus.addActionListener(e -> hapusPromo());
        btnReset.addActionListener(e -> resetForm());

        // ===== EVENT KLIK TABEL =====
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();

            if (row != -1) {
                selectedId = (int) model.getValueAt(row, 0);
                txtKodePromo.setText(model.getValueAt(row, 1).toString());
                txtDeskripsi.setText(model.getValueAt(row, 2).toString());
                txtDiskonPersen.setText(model.getValueAt(row, 3).toString());
                txtDiskonRupiah.setText(model.getValueAt(row, 4).toString());
                txtTanggalMulai.setText(model.getValueAt(row, 5).toString());
                txtTanggalAkhir.setText(model.getValueAt(row, 6).toString());
                chkAktif.setSelected(Boolean.parseBoolean(model.getValueAt(row, 7).toString()));
            }
        });
    }

    private void loadTable() {
        model.setRowCount(0);
        List<Promo> promos = dao.getAll();

        for (Promo p : promos) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getKodePromo(),
                    p.getDeskripsi(),
                    p.getDiskonPersen() > 0 ? p.getDiskonPersen() : "-",
                    p.getDiskonRupiah() > 0 ? p.getDiskonRupiah() : "-",
                    p.getTanggalMulai(),
                    p.getTanggalAkhir(),
                    p.isAktif() ? "Ya" : "Tidak"
            });
        }
    }

    private void tambahPromo() {
        if (txtKodePromo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode Promo tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (txtTanggalMulai.getText().trim().isEmpty() || txtTanggalAkhir.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tanggal mulai dan akhir harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String kodePromo = txtKodePromo.getText();
            String deskripsi = txtDeskripsi.getText();
            double diskonPersen = txtDiskonPersen.getText().isEmpty() ? 0 : Double.parseDouble(txtDiskonPersen.getText());
            double diskonRupiah = txtDiskonRupiah.getText().isEmpty() ? 0 : Double.parseDouble(txtDiskonRupiah.getText());
            String tanggalMulai = txtTanggalMulai.getText();
            String tanggalAkhir = txtTanggalAkhir.getText();
            boolean aktif = chkAktif.isSelected();

            Promo promo = new Promo(0, kodePromo, deskripsi, diskonPersen, diskonRupiah, tanggalMulai, tanggalAkhir, aktif);
            dao.insert(promo);

            JOptionPane.showMessageDialog(this, "Promo berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            loadTable();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Format diskon harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePromo() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih promo yang akan diupdate!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (txtKodePromo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode Promo tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String kodePromo = txtKodePromo.getText();
            String deskripsi = txtDeskripsi.getText();
            double diskonPersen = txtDiskonPersen.getText().isEmpty() ? 0 : Double.parseDouble(txtDiskonPersen.getText());
            double diskonRupiah = txtDiskonRupiah.getText().isEmpty() ? 0 : Double.parseDouble(txtDiskonRupiah.getText());
            String tanggalMulai = txtTanggalMulai.getText();
            String tanggalAkhir = txtTanggalAkhir.getText();
            boolean aktif = chkAktif.isSelected();

            Promo promo = new Promo(selectedId, kodePromo, deskripsi, diskonPersen, diskonRupiah, tanggalMulai, tanggalAkhir, aktif);
            dao.update(promo);

            JOptionPane.showMessageDialog(this, "Promo berhasil diupdate!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            loadTable();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Format diskon harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusPromo() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih promo yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus promo ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dao.delete(selectedId);
            JOptionPane.showMessageDialog(this, "Promo berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            loadTable();
        }
    }

    private void resetForm() {
        txtKodePromo.setText("");
        txtDeskripsi.setText("");
        txtDiskonPersen.setText("");
        txtDiskonRupiah.setText("");
        txtTanggalMulai.setText("");
        txtTanggalAkhir.setText("");
        chkAktif.setSelected(false);
        selectedId = -1;
        table.clearSelection();
    }


}