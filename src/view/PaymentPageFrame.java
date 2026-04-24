package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PaymentPageFrame extends JFrame {

    private String selectedMethod = "E-Wallet";

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
        long promoValue,
        long finalTotal,
        UserDashboard.RestoredState restoredState   // ← NEW
    ) {
        setTitle("CineTix - Payment Page");
        setSize(900, 780);
        setMinimumSize(new Dimension(720, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(40, 40, 40));
        root.setBorder(BorderFactory.createLineBorder(new Color(24, 24, 24), 2));
        add(root);

        root.add(buildPaymentPanel(
                userId, filmTitle, studio, jamTayang, seats,
                foodSummary, subtotal, promoValue, finalTotal,
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
            dispose();
            showPaymentSuccessScreen(userId, finalTotal);
        });

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

    private JLabel createLogo(String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return new JLabel();
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(img));
        } catch (Exception e) {
            return new JLabel();
        }
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
        frame.setSize(480, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(400, 340));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(30, 30, 30));
        frame.add(root);

        JPanel topStripe = new JPanel();
        topStripe.setBackground(new Color(30, 120, 60));
        topStripe.setPreferredSize(new Dimension(0, 8));
        root.add(topStripe, BorderLayout.NORTH);

        JPanel centre = new JPanel();
        centre.setBackground(new Color(30, 30, 30));
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBorder(new EmptyBorder(36, 40, 20, 40));

        JLabel iconLbl = new JLabel("\u2705", SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel headLbl = new JLabel("Pembayaran Berhasil!", SwingConstants.CENTER);
        headLbl.setForeground(new Color(60, 220, 100));
        headLbl.setFont(new Font("Krona One", Font.BOLD, 22));
        headLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Terima kasih telah menggunakan CineTix.", SwingConstants.CENTER);
        subLbl.setForeground(new Color(180, 180, 180));
        subLbl.setFont(new Font("Krona One", Font.PLAIN, 12));
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel detailCard = new JPanel(new GridLayout(2, 2, 16, 8));
        detailCard.setBackground(new Color(45, 45, 45));
        detailCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                new EmptyBorder(14, 18, 14, 18)));
        detailCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        detailCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        detailCard.add(cardLabel("Metode Pembayaran", new Color(160, 160, 160)));
        detailCard.add(cardLabel("Total Dibayar", new Color(160, 160, 160)));
        detailCard.add(cardLabel(selectedMethod, Color.WHITE));
        detailCard.add(cardLabel(formatRp(finalTotal), new Color(255, 200, 0)));

        JButton btnHome = new JButton("Kembali ke Beranda");
        btnHome.setBackground(new Color(35, 45, 130));
        btnHome.setForeground(new Color(255, 200, 0));
        btnHome.setFont(new Font("Krona One", Font.BOLD, 13));
        btnHome.setFocusPainted(false);
        btnHome.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnHome.setMaximumSize(new Dimension(260, 42));
        btnHome.setBorder(BorderFactory.createLineBorder(new Color(210, 165, 0), 2));
        btnHome.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHome.addActionListener(e -> {
            frame.dispose();
            new UserDashboard(userId).setVisible(true);
        });

        centre.add(iconLbl);
        centre.add(Box.createVerticalStrut(10));
        centre.add(headLbl);
        centre.add(Box.createVerticalStrut(6));
        centre.add(subLbl);
        centre.add(Box.createVerticalStrut(24));
        centre.add(detailCard);
        centre.add(Box.createVerticalStrut(28));
        centre.add(btnHome);

        root.add(centre, BorderLayout.CENTER);

        JPanel botStripe = new JPanel();
        botStripe.setBackground(new Color(30, 120, 60));
        botStripe.setPreferredSize(new Dimension(0, 8));
        root.add(botStripe, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JLabel cardLabel(String text, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(color);
        lbl.setFont(new Font("Krona One", Font.BOLD, 13));
        return lbl;
    }

}