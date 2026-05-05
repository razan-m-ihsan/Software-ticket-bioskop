package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import config.DatabaseConnection;
import util.QRCodeGenerator;

public class PaymentPageFrame extends JFrame {

    private String selectedMethod = "E-Wallet";
    private String selectedMethodType = "ewallet";

    public PaymentPageFrame(
        int userId,
        String cinemaName,
        List<String> movieTitles,
        List<String> moviePosters,
        int selectedMovieIndex,
        String filmTitle,
        String studio,
        String jamTayang,
        List<String> seats,
        String foodSummary,
        long subtotal,
        long seatTotal,
        long promoValue,
        long finalTotal,
        UserDashboard.RestoredState restoredState   // ← NEW
    ) {
        setTitle("CineTix - Payment Page");
        setMinimumSize(new Dimension(720, 640));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(40, 40, 40));
        root.setBorder(BorderFactory.createLineBorder(new Color(24, 24, 24), 2));
        add(root);

        root.add(buildPaymentPanel(
                userId, filmTitle, studio, jamTayang, seats,
                foodSummary, subtotal, seatTotal, promoValue, finalTotal,
                restoredState),   // ← NEW
                BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    // ── Main payment panel ────────────────────────────────────────────────────
        private JPanel buildPaymentPanel(
            int userId,
            String filmTitle,
            String studio,
            String jamTayang,
            List<String> seats,
            String foodSummary,
            long subtotal,
            long seatTotal,
            long promoValue,
            long finalTotal,
            UserDashboard.RestoredState restoredState   // ← NEW
    ) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(42, 42, 42));
        panel.setBorder(new EmptyBorder(14, 20, 10, 20));

        JLabel title = new JLabel("Payment Page", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Krona One", Font.BOLD, 15));
        panel.add(title, BorderLayout.NORTH);

        // ── Centre: summary + payment method (scrollable) ─────────────────────
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(buildSummaryCard(
                filmTitle, studio, jamTayang, seats, foodSummary, subtotal, promoValue, finalTotal));
        body.add(Box.createVerticalStrut(24));

        JLabel methodTitle = new JLabel("Pilih Metode Pembayaran");
        methodTitle.setForeground(Color.WHITE);
        methodTitle.setFont(new Font("Krona One", Font.BOLD, 16));
        methodTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(methodTitle);
        body.add(Box.createVerticalStrut(14));

        JPanel methodRow = new JPanel(new GridLayout(1, 3, 16, 0));
        methodRow.setOpaque(false);
        methodRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JPanel wallet = createMethodCard("E-Wallet",           "ewallet");
        JPanel card   = createMethodCard("Kartu debit/Credit", "card");
        JPanel bank   = createMethodCard("Transfer Bank",      "bank");

        // E-Wallet selected by default
        wallet.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 220, 80), 3),
                new EmptyBorder(8, 8, 10, 8)));

        methodRow.add(wallet);
        methodRow.add(card);
        methodRow.add(bank);
        body.add(methodRow);

        panel.add(body, BorderLayout.CENTER);

        // ── South: buttons always visible ────────────────────────────────────
        JButton btnBack = new JButton("Kembali");
        btnBack.setBackground(new Color(58, 58, 58));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Krona One", Font.BOLD, 13));
        btnBack.setFocusPainted(false);
        btnBack.setPreferredSize(new Dimension(120, 40));
        btnBack.setBorder(BorderFactory.createLineBorder(new Color(130, 130, 130), 1));
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            dispose();
            new UserDashboard(userId, restoredState).setVisible(true);  // ← CHANGED
        });

        JButton btnPay = new JButton("💳 Pay Now");
        btnPay.setBackground(new Color(35, 45, 130));
        btnPay.setForeground(new Color(255, 200, 0));
        btnPay.setFont(new Font("Krona One", Font.BOLD, 14));
        btnPay.setFocusPainted(false);
        btnPay.setPreferredSize(new Dimension(140, 40));
        btnPay.setBorder(BorderFactory.createLineBorder(new Color(210, 165, 0), 2));
        btnPay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPay.addActionListener(e -> {
            saveTransactions(userId, filmTitle, studio, jamTayang, seats, selectedMethod, seatTotal);
            dispose();
            showPaymentSuccessScreen(userId, finalTotal);
        });
        getRootPane().setDefaultButton(btnPay);

        JLabel lblLogout = new JLabel("Log out", SwingConstants.CENTER);
        lblLogout.setForeground(new Color(100, 160, 255));
        lblLogout.setFont(new Font("Krona One", Font.PLAIN, 12));
        lblLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new LoginForm().setVisible(true);
            }
        });

        JPanel buttonRow = new JPanel(new BorderLayout());
        buttonRow.setBackground(new Color(42, 42, 42));
        buttonRow.setBorder(new EmptyBorder(10, 0, 0, 0));
        buttonRow.add(btnBack,    BorderLayout.WEST);
        buttonRow.add(btnPay,     BorderLayout.EAST);
        buttonRow.add(lblLogout,  BorderLayout.CENTER);

        panel.add(buttonRow, BorderLayout.SOUTH);
        return panel;
    }

    // ── Summary card ──────────────────────────────────────────────────────────
    private JPanel buildSummaryCard(
            String filmTitle,
            String studio,
            String jamTayang,
            List<String> seats,
            String foodSummary,
            long subtotal,
            long promoValue,
            long finalTotal
    ) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(new Color(64, 10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(new Color(210, 165, 0), 1.4f, 3f),
                new EmptyBorder(10, 14, 10, 14)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        JLabel title = new JLabel("Ringkasan Pesanan");
        title.setForeground(new Color(255, 225, 180));
        title.setFont(new Font("Krona One", Font.PLAIN, 14));
        card.add(title, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(0, 2, 20, 4));
        details.setOpaque(false);

        List<String> sortedSeats = new ArrayList<>(seats);
        Collections.sort(sortedSeats);
        String seatText  = sortedSeats.isEmpty() ? "-" : String.join(", ", sortedSeats);
        String foodText  = (foodSummary == null || foodSummary.isEmpty()) ? "-" : foodSummary;
        String promoText = promoValue > 0
                ? "PromoOpening (-" + formatRpNoPrefix(promoValue) + ")"
                : "-";

        details.add(detailLabel("Film: "             + filmTitle, Color.WHITE, Font.PLAIN));
        details.add(detailLabel("Makanan & Minuman: "+ foodText,  Color.WHITE, Font.PLAIN));
        details.add(detailLabel("Studio: "           + studio,    Color.WHITE, Font.PLAIN));
        details.add(detailLabel("Kursi: "            + seatText,  Color.WHITE, Font.PLAIN));
        details.add(detailLabel("Jam Tayang: "       + jamTayang, Color.WHITE, Font.PLAIN));
        details.add(new JLabel());

        JLabel promoLbl = detailLabel("Promo Terpakai : " + promoText,
                new Color(0, 220, 40), Font.PLAIN);

        JPanel detailWrapper = new JPanel(new BorderLayout(0, 8));
        detailWrapper.setOpaque(false);
        detailWrapper.add(details,  BorderLayout.NORTH);
        detailWrapper.add(promoLbl, BorderLayout.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(210, 165, 0));
        detailWrapper.add(sep, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JLabel subtotalLbl = detailLabel("Subtotal (Ticket + F&B):", Color.WHITE, Font.PLAIN);
        JLabel subtotalVal = detailLabel(formatRp(subtotal), Color.WHITE, Font.PLAIN);
        subtotalVal.setHorizontalAlignment(SwingConstants.RIGHT);
        bottom.add(subtotalLbl, BorderLayout.WEST);
        bottom.add(subtotalVal, BorderLayout.EAST);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(detailWrapper, BorderLayout.NORTH);
        footer.add(bottom,        BorderLayout.CENTER);

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totalTitle = new JLabel("Total Harga Akhir");
        totalTitle.setForeground(Color.WHITE);
        totalTitle.setFont(new Font("Krona One", Font.BOLD, 18));
        JLabel totalValue = new JLabel(formatRp(finalTotal), SwingConstants.RIGHT);
        totalValue.setForeground(Color.WHITE);
        totalValue.setFont(new Font("Krona One", Font.BOLD, 18));
        totalRow.add(totalTitle, BorderLayout.WEST);
        totalRow.add(totalValue, BorderLayout.EAST);

        JPanel all = new JPanel(new BorderLayout(0, 10));
        all.setOpaque(false);
        all.add(footer,   BorderLayout.CENTER);
        all.add(totalRow, BorderLayout.SOUTH);
        card.add(all, BorderLayout.CENTER);

        return card;
    }

    // ── Method card — logos loaded on background thread ───────────────────────
    private JPanel createMethodCard(String title, String type) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(new Color(64, 10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(new Color(210, 165, 0), 1.4f, 3f),
                new EmptyBorder(8, 8, 10, 8)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Krona One", Font.BOLD, 14));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        logoPanel.setOpaque(false);

        card.add(lblTitle,  BorderLayout.NORTH);
        card.add(logoPanel, BorderLayout.CENTER);

        // Load logos asynchronously so the window opens instantly
        new Thread(() -> {
            JLabel[] logos = buildLogos(type);
            SwingUtilities.invokeLater(() -> {
                for (JLabel logo : logos) logoPanel.add(logo);
                logoPanel.revalidate();
                logoPanel.repaint();
            });
        }).start();

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedMethod = title;
                selectedMethodType = type;
                Component parent = card.getParent();
                if (parent instanceof JPanel) {
                    for (Component comp : ((JPanel) parent).getComponents()) {
                        if (comp instanceof JPanel) {
                            ((JPanel) comp).setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createDashedBorder(new Color(210, 165, 0), 1.4f, 3f),
                                    new EmptyBorder(8, 8, 10, 8)));
                        }
                    }
                }
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 220, 80), 3),
                        new EmptyBorder(8, 8, 10, 8)));
            }
        });

        return card;
    }

    /** Build logo JLabels for a given payment type (called off the EDT). */
    private JLabel[] buildLogos(String type) {
        if (type.equals("ewallet")) {
            return new JLabel[]{
                createLogo("/assets/dana.png",  70, 70),
                createLogo("/assets/gopay.png", 70, 70),
                createLogo("/assets/ovo.png",   70, 70)
            };
        } else if (type.equals("card")) {
            return new JLabel[]{ createLogo("/assets/visa_mastercard.png", 100, 100) };
        } else {
            return new JLabel[]{ createLogo("/assets/bank.png", 90, 90) };
        }
    }

    private ImageIcon loadIcon(String path, int maxWidth, int maxHeight) {
        try {
            BufferedImage img = null;
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                img = ImageIO.read(url);
            } else {
                File imageFile = new File("src" + path);
                if (imageFile.exists()) {
                    img = ImageIO.read(imageFile);
                }
            }
            if (img == null) return null;

            int origW = img.getWidth();
            int origH = img.getHeight();
            double scale = Math.min((double) maxWidth / origW, (double) maxHeight / origH);
            int targetW = (int) Math.max(1, Math.round(origW * scale));
            int targetH = (int) Math.max(1, Math.round(origH * scale));
            Image scaledImg = getScaledImage(img, targetW, targetH);
            return new ImageIcon(scaledImg);
        } catch (Exception e) {
            return null;
        }
    }

    private Image getScaledImage(BufferedImage src, int width, int height) {
        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dest.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, width, height, null);
        g2.dispose();
        return dest;
    }

    private JLabel createLogo(String path, int w, int h) {
        ImageIcon icon = loadIcon(path, w, h);
        return icon != null ? new JLabel(icon) : new JLabel();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JLabel detailLabel(String text, Color color, int fontStyle) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(new Font("Krona One", fontStyle, 15));
        return lbl;
    }

    private String formatRp(long amount) {
        return "Rp " + String.format("%,d", amount).replace(',', '.');
    }

    private String formatRpNoPrefix(long amount) {
        return String.format("%,d", amount).replace(',', '.');
    }

    // ── Payment Success Screen ────────────────────────────────────────────────
    private void showPaymentSuccessScreen(int userId, long finalTotal) {
        JFrame frame = new JFrame("Pembayaran Berhasil");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(1120, 720));
        frame.setSize(1180, 760);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(30, 30, 30));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.add(root);

        JPanel centre = new JPanel();
        centre.setBackground(new Color(30, 30, 30));
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBorder(new EmptyBorder(32, 40, 32, 40));

        JLabel iconLbl = new JLabel("🛒", SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        iconLbl.setForeground(new Color(130, 220, 130));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel headLbl = new JLabel("Pembayaran Berhasil!", SwingConstants.CENTER);
        headLbl.setForeground(new Color(130, 220, 130));
        headLbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        headLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Terima kasih telah menggunakan CineTix.", SwingConstants.CENTER);
        subLbl.setForeground(new Color(190, 190, 190));
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        centre.add(iconLbl);
        centre.add(Box.createVerticalStrut(18));
        centre.add(headLbl);
        centre.add(Box.createVerticalStrut(8));
        centre.add(subLbl);
        centre.add(Box.createVerticalStrut(32));

        JPanel contentRow = new JPanel(new BorderLayout(24, 0));
        contentRow.setOpaque(false);

        JPanel paymentCard = new JPanel();
        paymentCard.setLayout(new BoxLayout(paymentCard, BoxLayout.Y_AXIS));
        paymentCard.setBackground(new Color(40, 40, 40));
        paymentCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                new EmptyBorder(28, 28, 28, 28)));

        JPanel methodPanel = new JPanel();
        methodPanel.setLayout(new BoxLayout(methodPanel, BoxLayout.X_AXIS));
        methodPanel.setOpaque(false);
        methodPanel.setPreferredSize(new Dimension(320, 80));
        methodPanel.setMaximumSize(new Dimension(320, 80));
        methodPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel methodIconLbl = new JLabel();
        methodIconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        methodIconLbl.setVerticalAlignment(SwingConstants.CENTER);
        methodIconLbl.setPreferredSize(new Dimension(80, 80));
        methodIconLbl.setOpaque(false);
        methodIconLbl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ImageIcon methodIcon = loadPaymentIcon(selectedMethodType, 72, 72);
        if (methodIcon != null) {
            methodIconLbl.setIcon(methodIcon);
            methodIconLbl.setText("");
        } else {
            methodIconLbl.setText(selectedMethod.substring(0, 1));
            methodIconLbl.setForeground(Color.WHITE);
            methodIconLbl.setFont(new Font("Segoe UI", Font.BOLD, 34));
        }

        JPanel methodInfoPanel = new JPanel();
        methodInfoPanel.setLayout(new BoxLayout(methodInfoPanel, BoxLayout.Y_AXIS));
        methodInfoPanel.setOpaque(false);
        methodInfoPanel.setBorder(new EmptyBorder(8, 0, 8, 0));
        methodInfoPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel methodLabel = new JLabel("Metode Pembayaran");
        methodLabel.setForeground(new Color(170, 170, 170));
        methodLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        methodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel methodValue = new JLabel(selectedMethod);
        methodValue.setForeground(Color.WHITE);
        methodValue.setFont(new Font("Segoe UI", Font.BOLD, 20));
        methodValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        methodInfoPanel.add(methodLabel);
        methodInfoPanel.add(Box.createVerticalStrut(6));
        methodInfoPanel.add(methodValue);

        methodPanel.add(methodIconLbl);
        methodPanel.add(Box.createHorizontalStrut(16));
        methodPanel.add(methodInfoPanel);

        paymentCard.add(methodPanel);
        paymentCard.add(Box.createVerticalStrut(20));

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(90, 90, 90));
        paymentCard.add(separator);
        paymentCard.add(Box.createVerticalStrut(24));

        JLabel totalIconLbl = new JLabel("💵", SwingConstants.CENTER);
        totalIconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        totalIconLbl.setForeground(new Color(255, 215, 0));
        totalIconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel totalLabel = new JLabel("Total Dibayar", SwingConstants.CENTER);
        totalLabel.setForeground(new Color(170, 170, 170));
        totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel totalValue = new JLabel(formatRp(finalTotal), SwingConstants.CENTER);
        totalValue.setForeground(new Color(255, 215, 0));
        totalValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        totalValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        paymentCard.add(totalIconLbl);
        paymentCard.add(Box.createVerticalStrut(14));
        paymentCard.add(totalLabel);
        paymentCard.add(Box.createVerticalStrut(6));
        paymentCard.add(totalValue);

        JPanel leftGap = new JPanel();
        leftGap.setOpaque(false);
        leftGap.setPreferredSize(new Dimension(100, 0));
        contentRow.add(leftGap, BorderLayout.WEST);
        contentRow.add(paymentCard, BorderLayout.CENTER);

        BufferedImage qr = QRCodeGenerator.generateQR(
                "CineTix\nPayment: " + selectedMethod + "\nTotal: " + formatRp(finalTotal));
        if (qr != null) {
            JPanel qrWrapper = new JPanel();
            qrWrapper.setLayout(new BoxLayout(qrWrapper, BoxLayout.Y_AXIS));
            qrWrapper.setOpaque(false);
            qrWrapper.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                    new EmptyBorder(20, 20, 20, 20)));
            qrWrapper.setPreferredSize(new Dimension(280, 340));

            JLabel qrLabel = new JLabel(new ImageIcon(qr));
            qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            qrLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            qrLabel.setOpaque(true);
            qrLabel.setBackground(Color.WHITE);

            JLabel qrInstrText = new JLabel("<html><center>Tunjukkan QR ini ke kasir untuk<br>ditukar dengan tiket fisik.</center></html>", SwingConstants.CENTER);
            qrInstrText.setForeground(new Color(190, 190, 190));
            qrInstrText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            qrInstrText.setAlignmentX(Component.CENTER_ALIGNMENT);
            qrInstrText.setBorder(new EmptyBorder(16, 0, 0, 0));

            JLabel qrFooterText = new JLabel("Pindai di Kasir.", SwingConstants.CENTER);
            qrFooterText.setForeground(new Color(180, 180, 180));
            qrFooterText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            qrFooterText.setAlignmentX(Component.CENTER_ALIGNMENT);
            qrFooterText.setBorder(new EmptyBorder(8, 0, 0, 0));

            qrWrapper.add(qrLabel);
            qrWrapper.add(qrInstrText);
            qrWrapper.add(qrFooterText);

            contentRow.add(qrWrapper, BorderLayout.EAST);
        }

        centre.add(contentRow);
        centre.add(Box.createVerticalStrut(34));

        JButton btnHome = new JButton("Kembali ke Beranda");
        btnHome.setBackground(new Color(30, 60, 140));
        btnHome.setForeground(new Color(255, 220, 70));
        btnHome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnHome.setFocusPainted(false);
        btnHome.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnHome.setMaximumSize(new Dimension(260, 44));
        btnHome.setBorder(BorderFactory.createLineBorder(new Color(120, 160, 255), 1));
        btnHome.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHome.addActionListener(e -> {
            frame.dispose();
            new UserDashboard(userId).setVisible(true);
        });

        centre.add(btnHome);
        root.add(centre, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private ImageIcon loadPaymentIcon(String methodType, int maxWidth, int maxHeight) {
        String iconPath;
        switch (methodType) {
            case "ewallet":
                iconPath = "/assets/dana.png";
                break;
            case "card":
                iconPath = "/assets/visa_mastercard.png";
                break;
            case "bank":
                iconPath = "/assets/bank.png";
                break;
            default:
                return null;
        }
        return loadIcon(iconPath, maxWidth, maxHeight);
    }

    private void saveTransactions(int userId, String filmTitle, String studio, String jamTayang,
                                  List<String> seats, String paymentMethod, long seatTotal) {
        if (seats == null || seats.isEmpty()) return;

        double seatPrice = (double) seatTotal / seats.size();
        int scheduleId = findOrCreateScheduleId(filmTitle, studio, jamTayang, seatPrice);
        if (scheduleId <= 0) {
            System.err.println("Schedule not found and could not be created for transaction history.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO transactions(user_id,schedule_id,nomor_kursi,total_harga,metode_pembayaran,status) VALUES(?,?,?,?,?,?)")) {

            for (String seat : seats) {
                ps.setInt(1, userId);
                ps.setInt(2, scheduleId);
                ps.setString(3, seat);
                ps.setDouble(4, seatPrice);
                ps.setString(5, paymentMethod);
                ps.setString(6, "Success");
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int findOrCreateScheduleId(String movieTitle, String studioLabel, String jamTayang, double price) {
        int scheduleId = findScheduleId(movieTitle, studioLabel, jamTayang);
        if (scheduleId > 0) return scheduleId;

        int movieId = findMovieId(movieTitle);
        int studioId = findStudioId(getStudioNameFromLabel(studioLabel));
        if (movieId <= 0 || studioId <= 0) return -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO schedules(movie_id, studio_id, tanggal_tayang, jam_tayang, harga) VALUES(?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, movieId);
            ps.setInt(2, studioId);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setString(4, normalizeTimeForDb(jamTayang));
            ps.setDouble(5, price);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int findMovieId(String movieTitle) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM movies WHERE judul = ? LIMIT 1")) {
            ps.setString(1, movieTitle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int findStudioId(String studioName) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM studios WHERE nama_studio = ? LIMIT 1")) {
            ps.setString(1, studioName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int findScheduleId(String movieTitle, String studioLabel, String jamTayang) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT s.id FROM schedules s " +
                    "JOIN movies m ON s.movie_id = m.id " +
                    "JOIN studios st ON s.studio_id = st.id " +
                    "WHERE m.judul = ? AND st.nama_studio = ? AND s.jam_tayang = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, movieTitle);
                ps.setString(2, getStudioNameFromLabel(studioLabel));
                ps.setString(3, normalizeTimeForDb(jamTayang));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private String getStudioNameFromLabel(String label) {
        if (label == null) return "";
        if (label.contains("Reguler 2D")) return "Reguler 2D";
        if (label.contains("Reguler 3D")) return "Reguler 3D";
        if (label.contains("Premium")) return "Premium";
        if (label.contains("IMAX")) return "IMAX";
        return label;
    }

    private String normalizeTimeForDb(String jam) {
        if (jam == null || jam.isEmpty()) return "";
        String normalized = jam.replace('.', ':').trim();
        if (normalized.length() == 5) normalized += ":00";
        return normalized;
    }

}