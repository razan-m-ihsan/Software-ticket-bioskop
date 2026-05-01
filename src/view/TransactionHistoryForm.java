package view;

import config.DatabaseConnection;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import util.QRCodeGenerator;

public class TransactionHistoryForm extends JFrame {

    public TransactionHistoryForm(int userId) {
        setTitle("Riwayat Pembayaran");
        setSize(850, 580);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(28, 18, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        add(root);

        JLabel title = new JLabel("Riwayat Pembayaran", SwingConstants.CENTER);
        title.setForeground(new Color(255, 215, 120));
        title.setFont(new Font("Krona One", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        root.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        int recordCount = loadTransactionCards(content, userId);
        if (recordCount == 0) {
            JLabel empty = new JLabel("Belum ada riwayat pembayaran.", SwingConstants.CENTER);
            empty.setForeground(new Color(210, 210, 210));
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            empty.setBorder(new EmptyBorder(120, 0, 120, 0));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(empty);
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        root.add(scroll, BorderLayout.CENTER);
    }

    private int loadTransactionCards(JPanel content, int userId) {
        int count = 0;
        String query = "SELECT m.judul, m.poster_url, s.tanggal_tayang, s.jam_tayang, " +
                "GROUP_CONCAT(t.nomor_kursi ORDER BY t.nomor_kursi SEPARATOR ', ') AS kursi, " +
                "t.metode_pembayaran, SUM(t.total_harga) AS total, t.status, t.waktu_transaksi " +
                "FROM transactions t " +
                "JOIN schedules s ON t.schedule_id = s.id " +
                "JOIN movies m ON s.movie_id = m.id " +
                "WHERE t.user_id = ? " +
                "GROUP BY t.user_id, t.schedule_id, t.waktu_transaksi, t.metode_pembayaran, t.status, m.judul, m.poster_url, s.tanggal_tayang, s.jam_tayang " +
                "ORDER BY t.waktu_transaksi DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LocalDate tanggal = rs.getDate("tanggal_tayang").toLocalDate();
                String jam = rs.getTime("jam_tayang").toString();
                String judul = rs.getString("judul");
                String posterUrl = rs.getString("poster_url");
                if (posterUrl == null || posterUrl.isEmpty()) {
                    posterUrl = getLocalPosterPathForTitle(judul);
                }
                JPanel card = buildCard(
                        judul,
                        posterUrl,
                        tanggal,
                        jam,
                        rs.getString("kursi"),
                        rs.getString("metode_pembayaran"),
                        rs.getDouble("total"),
                        rs.getString("status")
                );
                content.add(card);
                content.add(Box.createVerticalStrut(14));
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    private JPanel buildCard(String title, String posterPath, LocalDate tanggal, String jam, String kursi,
                              String metode, double total, String status) {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setOpaque(true);
        card.setBackground(new Color(56, 24, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 180, 44), 1),
                new EmptyBorder(12, 12, 12, 12)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel posterLabel = new JLabel();
        posterLabel.setPreferredSize(new Dimension(92, 132));
        posterLabel.setBorder(BorderFactory.createLineBorder(new Color(130, 100, 40), 1));
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        posterLabel.setOpaque(true);
        posterLabel.setBackground(new Color(40, 20, 10));
        ImageIcon posterIcon = loadPosterIcon(posterPath, 92, 132);
        if (posterIcon != null) {
            posterLabel.setIcon(posterIcon);
        } else {
            posterLabel.setText("No Image");
            posterLabel.setForeground(new Color(200, 200, 200));
            posterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
        card.add(posterLabel, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Krona One", Font.BOLD, 16));
        topRow.add(lblTitle, BorderLayout.WEST);

        JLabel lblStatus = new JLabel(statusLabel(status), SwingConstants.CENTER);
        lblStatus.setOpaque(true);
        lblStatus.setBackground(statusColor(status));
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        topRow.add(lblStatus, BorderLayout.EAST);

        center.add(topRow, BorderLayout.NORTH);

        JPanel infoGrid = new JPanel(new GridLayout(0, 2, 16, 6));
        infoGrid.setOpaque(false);
        infoGrid.add(detailLabel("Tanggal:", Color.LIGHT_GRAY));
        infoGrid.add(detailLabel(formatDate(tanggal) + "  " + jam, Color.WHITE));
        infoGrid.add(detailLabel("Kursi:", Color.LIGHT_GRAY));
        infoGrid.add(detailLabel(kursi, Color.WHITE));
        infoGrid.add(detailLabel("Metode:", Color.LIGHT_GRAY));
        infoGrid.add(detailLabel(metode, Color.WHITE));
        infoGrid.add(detailLabel("Total:", Color.LIGHT_GRAY));
        infoGrid.add(detailLabel(formatRp(total), Color.WHITE));
        center.add(infoGrid, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionRow.setOpaque(false);
        JButton btnDetail = new JButton(status.equalsIgnoreCase("Success") ? "Lihat Tiket" : "Detail");
        btnDetail.setBackground(new Color(25, 80, 200));
        btnDetail.setForeground(Color.WHITE);
        btnDetail.setFont(new Font("Krona One", Font.BOLD, 12));
        btnDetail.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDetail.setFocusPainted(false);
        btnDetail.setPreferredSize(new Dimension(120, 32));
        btnDetail.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 255), 1));
        btnDetail.addActionListener(e -> showDetailScreen(
                title, posterPath, tanggal, jam, kursi, metode, total, status));
        actionRow.add(btnDetail);
        center.add(actionRow, BorderLayout.SOUTH);

        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private void showDetailScreen(String title, String posterPath, LocalDate tanggal, String jam,
                                  String kursi, String metode, double total, String status) {
        JFrame detailFrame = new JFrame("Detail Transaksi");
        detailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        detailFrame.setSize(940, 620);
        detailFrame.setMinimumSize(new Dimension(860, 560));
        detailFrame.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(16, 10, 8));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(24, 40, 24, 40));

        JLabel headerLabel = new JLabel("Detail Transaksi");
        headerLabel.setForeground(new Color(255, 210, 140));
        headerLabel.setFont(new Font("Krona One", Font.BOLD, 34));
        header.add(headerLabel, BorderLayout.WEST);

        JButton btnClose = new JButton("Tutup");
        btnClose.setBackground(new Color(210, 80, 80));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Krona One", Font.BOLD, 14));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.setFocusPainted(false);
        btnClose.setBorder(BorderFactory.createLineBorder(new Color(180, 90, 90), 1));
        btnClose.setPreferredSize(new Dimension(110, 36));
        btnClose.addActionListener(e -> detailFrame.dispose());
        header.add(btnClose, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(30, 30));
        card.setOpaque(true);
        card.setBackground(new Color(44, 20, 12));
        card.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel posterLabel = new JLabel();
        posterLabel.setPreferredSize(new Dimension(320, 480));
        posterLabel.setOpaque(true);
        posterLabel.setBackground(new Color(28, 18, 12));
        posterLabel.setBorder(BorderFactory.createLineBorder(new Color(210, 170, 60), 2));
        ImageIcon posterIcon = loadPosterIcon(posterPath, 320, 480);
        if (posterIcon != null) {
            posterLabel.setIcon(posterIcon);
        } else {
            posterLabel.setText("No Image");
            posterLabel.setForeground(new Color(220, 220, 220));
            posterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            posterLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            posterLabel.setVerticalTextPosition(SwingConstants.CENTER);
            posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }
        card.add(posterLabel, BorderLayout.WEST);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Krona One", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 0, 18, 0));
        details.add(titleLabel);

        details.add(createInfoRow("Tanggal", formatDate(tanggal) + "  " + jam));
        details.add(createInfoRow("Kursi", kursi));
        details.add(createInfoRow("Metode", metode));
        details.add(createInfoRow("Total", formatRp(total)));
        details.add(createInfoRow("Status", statusLabel(status)));

        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(0, 24));
        details.add(spacer);

        JLabel note = new JLabel("Terima kasih telah menggunakan CineTix. Nikmati filmmu!");
        note.setForeground(new Color(210, 210, 190));
        note.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        note.setBorder(new EmptyBorder(18, 0, 0, 0));
        details.add(note);

        card.add(details, BorderLayout.CENTER);

        JPanel qrPanel = new JPanel();
        qrPanel.setOpaque(false);
        qrPanel.setLayout(new BoxLayout(qrPanel, BoxLayout.Y_AXIS));
        qrPanel.setBorder(new EmptyBorder(12, 0, 12, 0));

        JLabel qrTitle = new JLabel("QR Code Tiket");
        qrTitle.setForeground(new Color(255, 215, 120));
        qrTitle.setFont(new Font("Krona One", Font.BOLD, 18));
        qrTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrPanel.add(qrTitle);
        qrPanel.add(Box.createVerticalStrut(12));

        String qrText = "Film: " + title + "\n" +
                "Tanggal: " + formatDate(tanggal) + " " + jam + "\n" +
                "Kursi: " + kursi + "\n" +
                "Metode: " + metode + "\n" +
                "Total: " + formatRp(total) + "\n" +
                "Status: " + statusLabel(status);
        BufferedImage qrImage = QRCodeGenerator.generateQR(qrText);
        JLabel qrLabel = new JLabel();
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrLabel.setBorder(BorderFactory.createLineBorder(new Color(210, 170, 60), 2));
        if (qrImage != null) {
            qrLabel.setIcon(new ImageIcon(qrImage));
        } else {
            qrLabel.setText("QR tidak tersedia");
            qrLabel.setForeground(Color.WHITE);
            qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qrLabel.setPreferredSize(new Dimension(250, 250));
        }
        qrPanel.add(qrLabel);
        qrPanel.add(Box.createVerticalStrut(14));

        JLabel qrHint = new JLabel("Scan saat masuk ke studio");
        qrHint.setForeground(new Color(200, 200, 200));
        qrHint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        qrHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrPanel.add(qrHint);

        card.add(qrPanel, BorderLayout.EAST);

        root.add(card, BorderLayout.CENTER);

        detailFrame.add(root);
        detailFrame.setLocationRelativeTo(this);
        detailFrame.setVisible(true);
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 0, 6, 0));

        JLabel lbl = new JLabel(label + ":");
        lbl.setForeground(new Color(190, 180, 140));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        row.add(lbl, BorderLayout.WEST);

        JLabel val = new JLabel(value);
        val.setForeground(Color.WHITE);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        row.add(val, BorderLayout.EAST);

        return row;
    }

    private JLabel detailLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lbl;
    }

    private String statusLabel(String status) {
        if (status == null) return "Tidak Diketahui";
        switch (status.toLowerCase()) {
            case "success": return "Selesai";
            case "pending": return "Menunggu";
            case "failed": return "Gagal";
            default: return status;
        }
    }

    private Color statusColor(String status) {
        if (status == null) return new Color(120, 120, 120);
        switch (status.toLowerCase()) {
            case "success": return new Color(26, 179, 90);
            case "pending": return new Color(255, 178, 0);
            case "failed": return new Color(210, 60, 60);
            default: return new Color(120, 120, 120);
        }
    }

    private String formatRp(double value) {
        return "Rp " + String.format("%,.0f", value).replace(',', '.');
    }

    private String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
        return date.format(formatter);
    }

    private ImageIcon loadPosterIcon(String path, int width, int height) {
        if (path == null || path.isEmpty()) return null;
        try {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                ImageIcon icon = new ImageIcon(new URL(path));
                return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
            String normalized = path.startsWith("/") ? path.substring(1) : path;
            URL resource = getClass().getResource("/" + normalized);
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
            File file = new File(path);
            if (!file.exists()) {
                String relative = normalized.startsWith("assets/") ? normalized.substring(7) : normalized;
                file = new File("src/assets", relative);
            }
            if (file.exists()) {
                Image image = ImageIO.read(file);
                return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String getLocalPosterPathForTitle(String title) {
        if (title == null) return "";
        switch (title.trim()) {
            case "Avengers":
                return "assets/avengers.jpg";
            case "El Camino: A Breaking Bad Movie":
                return "assets/el_camino.jpg";
            case "AMBALANGKUNG THE MOVIE":
                return "assets/ambalangkung.jpg";
            case "Project Hail Mary":
                return "assets/project_hail_mary.jpg";
            default:
                return "";
        }
    }
}
