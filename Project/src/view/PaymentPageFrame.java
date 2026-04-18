package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PaymentPageFrame extends JFrame {

    private final List<String> movieTitles;
    private final List<String> moviePosters;
    private final int selectedMovieIndex;

    private JLabel posterPreview;
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
            long finalTotal
    ) {
        this.movieTitles = movieTitles;
        this.moviePosters = moviePosters;
        this.selectedMovieIndex = selectedMovieIndex;

        setTitle("CineTix - Payment Page");
        setSize(980, 750);
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(40, 40, 40));
        root.setBorder(BorderFactory.createLineBorder(new Color(24, 24, 24), 2));
        add(root);

        root.add(buildHeader(cinemaName), BorderLayout.NORTH);
        root.add(buildCenter(
                userId, filmTitle, studio, jamTayang, seats,
                foodSummary, subtotal, promoValue, finalTotal), BorderLayout.CENTER);
    }

    private JPanel buildHeader(String cinemaName) {
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setBackground(new Color(30, 30, 30));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("CineTix", SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Krona One", Font.BOLD, 26));

        JLabel lblSub = new JLabel("Pilih Cinema", SwingConstants.CENTER);
        lblSub.setForeground(new Color(215, 215, 215));
        lblSub.setFont(new Font("Krona One", Font.BOLD, 12));

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setOpaque(false);
        top.add(lblTitle);
        top.add(lblSub);

        JPanel cinemaRow = new JPanel(new BorderLayout(8, 0));
        cinemaRow.setBackground(new Color(52, 52, 52));
        cinemaRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(78, 78, 78)),
                new EmptyBorder(8, 12, 8, 12)));

        JLabel pin = new JLabel("📍");
        pin.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JLabel cinemaLabel = new JLabel(cinemaName);
        cinemaLabel.setForeground(Color.WHITE);
        cinemaLabel.setFont(new Font("Krona One", Font.BOLD, 14));

        JLabel arrow = new JLabel("⌄");
        arrow.setForeground(Color.WHITE);
        arrow.setFont(new Font("Krona One", Font.BOLD, 18));

        cinemaRow.add(pin, BorderLayout.WEST);
        cinemaRow.add(cinemaLabel, BorderLayout.CENTER);
        cinemaRow.add(arrow, BorderLayout.EAST);

        header.add(top, BorderLayout.NORTH);
        header.add(cinemaRow, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildCenter(
            int userId,
            String filmTitle,
            String studio,
            String jamTayang,
            List<String> seats,
            String foodSummary,
            long subtotal,
            long promoValue,
            long finalTotal
    ) {
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(new Color(42, 42, 42));

        content.add(buildLeftPanel(), BorderLayout.WEST);
        content.add(buildPaymentPanel(
                userId, filmTitle, studio, jamTayang, seats,
                foodSummary, subtotal, promoValue, finalTotal), BorderLayout.CENTER);
        return content;
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBackground(new Color(42, 42, 42));
        panel.setBorder(new EmptyBorder(14, 10, 10, 6));

        JLabel lbl = new JLabel("Sedang Tayang", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Krona One", Font.BOLD, 15));
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        panel.add(lbl, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (String title : movieTitles) {
            model.addElement(title);
        }

        JList<String> filmList = new JList<>(model);
        filmList.setBackground(new Color(36, 36, 36));
        filmList.setForeground(Color.WHITE);
        filmList.setFixedCellHeight(92);
        filmList.setSelectionBackground(new Color(50, 88, 168));
        filmList.setCellRenderer(new MovieCellRenderer());

        if (selectedMovieIndex >= 0 && selectedMovieIndex < model.size()) {
            filmList.setSelectedIndex(selectedMovieIndex);
        }

        JScrollPane scroll = new JScrollPane(filmList);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel posterBox = new JPanel(new BorderLayout(0, 5));
        posterBox.setBackground(new Color(42, 42, 42));
        posterBox.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel posterTitle = new JLabel("Poster Preview", SwingConstants.CENTER);
        posterTitle.setForeground(Color.WHITE);
        posterTitle.setFont(new Font("Krona One", Font.BOLD, 12));

        posterPreview = new JLabel();
        posterPreview.setPreferredSize(new Dimension(120, 175));
        posterPreview.setBackground(new Color(25, 25, 25));
        posterPreview.setOpaque(true);
        posterPreview.setBorder(BorderFactory.createLineBorder(new Color(68, 68, 68)));
        posterPreview.setHorizontalAlignment(SwingConstants.CENTER);

        posterBox.add(posterTitle, BorderLayout.NORTH);
        posterBox.add(posterPreview, BorderLayout.CENTER);
        panel.add(posterBox, BorderLayout.SOUTH);

        loadPoster(selectedMovieIndex);
        filmList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadPoster(filmList.getSelectedIndex());
            }
        });

        return panel;
    }

    private JPanel buildPaymentPanel(
            int userId,
            String filmTitle,
            String studio,
            String jamTayang,
            List<String> seats,
            String foodSummary,
            long subtotal,
            long promoValue,
            long finalTotal
    ) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(42, 42, 42));
        panel.setBorder(new EmptyBorder(14, 10, 10, 14));

        JLabel title = new JLabel("Payment Page", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Krona One", Font.BOLD, 15));
        panel.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(buildSummaryCard(
                filmTitle, studio, jamTayang, seats, foodSummary, subtotal, promoValue, finalTotal));
        body.add(Box.createVerticalStrut(20));

        JLabel methodTitle = new JLabel("Pilih Metode Pembayaran");
        methodTitle.setForeground(Color.WHITE);
        methodTitle.setFont(new Font("Krona One", Font.BOLD, 15));
        methodTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(methodTitle);
        body.add(Box.createVerticalStrut(14));

        JPanel methodRow = new JPanel(new GridLayout(1, 3, 16, 0));
        methodRow.setOpaque(false);
        methodRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel wallet = createMethodCard("E-Wallet", "DANA   GOPAY   OVO");
        JPanel card = createMethodCard("Kartu debit/Credit", "VISA   MASTERCARD");
        JPanel bank = createMethodCard("Transfer Bank", "BANK");
        wallet.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 220, 80), 3),
                new EmptyBorder(8, 8, 10, 8)));

        methodRow.add(wallet);
        methodRow.add(card);
        methodRow.add(bank);
        body.add(methodRow);
        body.add(Box.createVerticalStrut(22));

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
            new UserDashboard(userId);
        });

        JButton btnPay = new JButton("💳 Pay Now ( Bayar Sekarang )");
        btnPay.setBackground(new Color(35, 45, 130));
        btnPay.setForeground(new Color(255, 200, 0));
        btnPay.setFont(new Font("Krona One", Font.BOLD, 14));
        btnPay.setFocusPainted(false);
        btnPay.setPreferredSize(new Dimension(250, 40));
        btnPay.setBorder(BorderFactory.createLineBorder(new Color(210, 165, 0), 2));
        btnPay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPay.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Pembayaran berhasil! ✅\n"
                        + "Metode : " + selectedMethod + "\n"
                        + "Total  : " + formatRp(finalTotal),
                "Pembayaran",
                JOptionPane.INFORMATION_MESSAGE
        ));

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(new Color(42, 42, 42));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        buttonPanel.add(btnBack, BorderLayout.WEST);
        buttonPanel.add(btnPay, BorderLayout.EAST);

        body.add(buttonPanel);
        body.add(Box.createVerticalStrut(22));

        JLabel lblLogout = new JLabel("Log out");
        lblLogout.setForeground(new Color(100, 160, 255));
        lblLogout.setFont(new Font("Krona One", Font.PLAIN, 12));
        lblLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new LoginForm().setVisible(true);
            }
        });
        body.add(lblLogout);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(body, BorderLayout.NORTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JLabel title = new JLabel("Ringkasan Pesanan");
        title.setForeground(new Color(255, 225, 180));
        title.setFont(new Font("Krona One", Font.PLAIN, 14));
        card.add(title, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(0, 2, 20, 4));
        details.setOpaque(false);

        List<String> sortedSeats = new ArrayList<>(seats);
        Collections.sort(sortedSeats);
        String seatText = sortedSeats.isEmpty() ? "-" : String.join(", ", sortedSeats);
        String foodText = (foodSummary == null || foodSummary.isBlank()) ? "-" : foodSummary;
        String promoText = promoValue > 0
                ? "PromoOpening (-" + formatRpNoPrefix(promoValue) + ")"
                : "-";

        details.add(detailLabel("Film: " + filmTitle, Color.WHITE, Font.PLAIN));
        details.add(detailLabel("Makanan & Minuman: " + foodText, Color.WHITE, Font.PLAIN));
        details.add(detailLabel("Studio: " + studio, Color.WHITE, Font.PLAIN));
        details.add(detailLabel("Kursi: " + seatText, Color.WHITE, Font.PLAIN));
        details.add(detailLabel("Jam Tayang: " + jamTayang, Color.WHITE, Font.PLAIN));
        details.add(new JLabel());

        JLabel promoLbl = detailLabel("Promo Terpakai : " + promoText,
                new Color(0, 220, 40), Font.PLAIN);

        JPanel detailWrapper = new JPanel(new BorderLayout(0, 8));
        detailWrapper.setOpaque(false);
        detailWrapper.add(details, BorderLayout.NORTH);
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
        footer.add(bottom, BorderLayout.CENTER);

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
        all.add(footer, BorderLayout.CENTER);
        all.add(totalRow, BorderLayout.SOUTH);
        card.add(all, BorderLayout.CENTER);

        return card;
    }

    private JLabel detailLabel(String text, Color color, int fontStyle) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(new Font("Krona One", fontStyle, 15));
        return lbl;
    }

    private JPanel createMethodCard(String title, String logosText) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(new Color(64, 10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(new Color(210, 165, 0), 1.4f, 3f),
                new EmptyBorder(8, 8, 10, 8)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Krona One", Font.BOLD, 14));

        JLabel logo = new JLabel(logosText, SwingConstants.CENTER);
        logo.setForeground(new Color(255, 200, 0));
        logo.setFont(new Font("Krona One", Font.BOLD, 13));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(logo, BorderLayout.CENTER);

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

    private void loadPoster(int idx) {
        if (posterPreview == null) return;
        if (idx < 0 || idx >= moviePosters.size()) {
            posterPreview.setIcon(null);
            return;
        }
        String url = moviePosters.get(idx);
        if (url == null || url.isBlank()) {
            posterPreview.setIcon(null);
            return;
        }
        new Thread(() -> {
            try {
                ImageIcon ic = new ImageIcon(new URL(url));
                Image sc = ic.getImage().getScaledInstance(120, 175, Image.SCALE_SMOOTH);
                SwingUtilities.invokeLater(() -> posterPreview.setIcon(new ImageIcon(sc)));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> posterPreview.setIcon(null));
            }
        }).start();
    }

    private String formatRp(long amount) {
        return "Rp " + String.format("%,d", amount).replace(',', '.');
    }

    private String formatRpNoPrefix(long amount) {
        return String.format("%,d", amount).replace(',', '.');
    }

    private class MovieCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(true);
            panel.setBorder(new EmptyBorder(6, 8, 6, 8));
            panel.setBackground(isSelected
                    ? new Color(50, 88, 168)
                    : (index % 2 == 0 ? new Color(36, 36, 36) : new Color(42, 42, 42)));

            JLabel thumb = new JLabel();
            thumb.setPreferredSize(new Dimension(50, 72));
            thumb.setBackground(new Color(24, 24, 24));
            thumb.setOpaque(true);

            if (index < moviePosters.size()) {
                String url = moviePosters.get(index);
                if (url != null && !url.isBlank()) {
                    try {
                        ImageIcon ic = new ImageIcon(new URL(url));
                        thumb.setIcon(new ImageIcon(
                                ic.getImage().getScaledInstance(50, 72, Image.SCALE_SMOOTH)));
                    } catch (Exception ex) {
                        thumb.setIcon(null);
                    }
                }
            }

            JLabel title = new JLabel(value.toString());
            title.setForeground(Color.WHITE);
            title.setFont(new Font("Krona One", Font.PLAIN, 13));
            title.setVerticalAlignment(SwingConstants.CENTER);

            panel.add(thumb, BorderLayout.WEST);
            panel.add(title, BorderLayout.CENTER);
            return panel;
        }
    }
}
