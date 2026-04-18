package view;

import config.DatabaseConnection;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class UserDashboard extends JFrame {

    private int userId;

    // ── Film list ──────────────────────────────────────────────────────────────
    private DefaultListModel<String> listModel   = new DefaultListModel<>();
    private JList<String>            filmList    = new JList<>(listModel);
    private ArrayList<Integer>       filmIds     = new ArrayList<>();
    private ArrayList<String>        filmPosters = new ArrayList<>();

    // ── Detail fields ──────────────────────────────────────────────────────────
    private JTextField txtJudul       = new JTextField();
    private JTextField txtJamTayang   = new JTextField();
    private JTextField txtStudio      = new JTextField();
    private JTextField txtKursi       = new JTextField();
    private JTextField txtMakanan     = new JTextField();   // renamed from txtHarga
    private JTextField txtTotal       = new JTextField();

    // ── Poster preview ─────────────────────────────────────────────────────────
    private JLabel lblPoster;

    // ── State ──────────────────────────────────────────────────────────────────
    private String       selectedStudioName = "";
    private String       selectedTime       = "";
    private String       selectedCinema     = "Cinepolis - Palembang Icon";
    private List<String> selectedSeats      = new ArrayList<>();
    private long         foodTotal          = 0;
    private String       foodSummary        = "";   // e.g. "Popcorn Caramel Large x2, ..."

    // ── Persistent sub-panels (keep state between dialog opens) ───────────────
    private SeatSelectionPanel persistentSeatPanel = null;
    private FoodMenuPanel      persistentFoodPanel = null;

    private static final String[] STUDIO_TYPES  = {"REGULER 2D", "REGULER 3D", "PREMIUM", "IMAX"};
    private static final String[] TIME_SLOTS    = {"10.30","12.00","13.00","15.30","18.00","20.30"};
    private static final String   HARGA_DEFAULT = "Rp 75,000";
    private static final int      HARGA_INT     = 75_000;
    private static final String[] STUDIO_LABELS = {
        "Studio 4 - Reguler 2D",
        "Studio 3 - Reguler 3D",
        "Studio 2 - Premium",
        "Studio 1 - IMAX"
    };

    // ─────────────────────────────────────────────────────────────────────────
    public UserDashboard(int userId) {
        this.userId = userId;
        setTitle("CineTix");
        setSize(980, 750);
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(40, 40, 40));
        add(root);

        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(40, 40, 40));
        root.add(content, BorderLayout.CENTER);

        content.add(buildLeftPanel(),  BorderLayout.WEST);
        content.add(buildRightPanel(), BorderLayout.CENTER);

        loadFilm();

        filmList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && filmList.getSelectedIndex() >= 0) {
                int idx = filmList.getSelectedIndex();
                loadDetailFilm(filmIds.get(idx));
                loadPoster(idx);
            }
        });

        setVisible(true);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HEADER
    // ──────────────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setBackground(new Color(30, 30, 30));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("CineTix", SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel lblSub = new JLabel("Pilih Cinema", SwingConstants.CENTER);
        lblSub.setForeground(new Color(180, 180, 180));
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(new Color(30, 30, 30));
        topRow.add(lblTitle, BorderLayout.CENTER);
        topRow.add(lblSub,   BorderLayout.SOUTH);

        JPanel cinemaRow = new JPanel(new BorderLayout(8, 0));
        cinemaRow.setBackground(new Color(52, 52, 52));
        cinemaRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                new EmptyBorder(8, 12, 8, 12)));

        JLabel pin = new JLabel("📍");
        pin.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        String[] cinemas = {"Cinepolis - Palembang Icon", "CGV Palembang", "XXI Palembang"};
        JComboBox<String> cbCinema = new JComboBox<>(cinemas);
        cbCinema.setBackground(new Color(52, 52, 52));
        cbCinema.setForeground(Color.WHITE);
        cbCinema.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cbCinema.setBorder(null);
        cbCinema.addActionListener(e -> selectedCinema = (String) cbCinema.getSelectedItem());
        selectedCinema = (String) cbCinema.getSelectedItem();

        JLabel arrow = new JLabel("⌄");
        arrow.setForeground(Color.WHITE);
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        arrow.setBorder(new EmptyBorder(0, 8, 0, 0));

        cinemaRow.add(pin,      BorderLayout.WEST);
        cinemaRow.add(cbCinema, BorderLayout.CENTER);
        cinemaRow.add(arrow,    BorderLayout.EAST);

        header.add(topRow,    BorderLayout.NORTH);
        header.add(cinemaRow, BorderLayout.SOUTH);
        return header;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // LEFT PANEL
    // ──────────────────────────────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(265, 0));
        panel.setBackground(new Color(42, 42, 42));
        panel.setBorder(new EmptyBorder(14, 10, 10, 6));

        JLabel lbl = new JLabel("Sedang Tayang", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        panel.add(lbl, BorderLayout.NORTH);

        filmList.setBackground(new Color(36, 36, 36));
        filmList.setForeground(Color.WHITE);
        filmList.setFixedCellHeight(78);
        filmList.setCellRenderer(new FilmCellRenderer());
        filmList.setSelectionBackground(new Color(50, 88, 168));

        JScrollPane scroll = new JScrollPane(filmList);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel posterBox = new JPanel(new BorderLayout(0, 5));
        posterBox.setBackground(new Color(42, 42, 42));
        posterBox.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel posterTitle = new JLabel("Poster Preview");
        posterTitle.setForeground(Color.WHITE);
        posterTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));

        lblPoster = new JLabel();
        lblPoster.setPreferredSize(new Dimension(115, 155));
        lblPoster.setBackground(new Color(25, 25, 25));
        lblPoster.setOpaque(true);
        lblPoster.setBorder(BorderFactory.createLineBorder(new Color(68, 68, 68)));
        lblPoster.setHorizontalAlignment(SwingConstants.CENTER);

        posterBox.add(posterTitle, BorderLayout.NORTH);
        posterBox.add(lblPoster,   BorderLayout.CENTER);
        panel.add(posterBox, BorderLayout.SOUTH);

        return panel;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RIGHT PANEL
    // ──────────────────────────────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(42, 42, 42));
        panel.setBorder(new EmptyBorder(14, 10, 10, 14));

        panel.add(buildMenuPesanPanel(), BorderLayout.NORTH);
        panel.add(buildDetailPesanan(), BorderLayout.CENTER);
        panel.add(buildBottomBar(),     BorderLayout.SOUTH);

        return panel;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // MENU PESAN
    // ──────────────────────────────────────────────────────────────────────────
    private JPanel buildMenuPesanPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setBackground(new Color(42, 42, 42));

        JLabel menuTitle = new JLabel("Menu Pesan", SwingConstants.CENTER);
        menuTitle.setForeground(Color.WHITE);
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        wrapper.add(menuTitle, BorderLayout.NORTH);

        JPanel menuBox = new JPanel(new GridLayout(2, 3, 8, 8));
        menuBox.setBackground(new Color(110, 8, 8));
        menuBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(140, 20, 20), 2),
                new EmptyBorder(10, 10, 10, 10)));

        JButton btnJamStudio = createMenuButton("🕐", "Pilih Jam & Studio", new Color(22, 145, 60));
        JButton btnMakanan   = createMenuButton("🍿", "Makanan &\nMinuman",  new Color(190, 130, 0));
        JButton btnReset     = createMenuButton("🔄", "Reset Pesanan",       new Color(185, 20, 20));
        JButton btnKursi     = createMenuButton("🎙", "Pilih Kursi",         new Color(40, 90, 195));
        JButton btnTrailer   = createMenuButton("🎞", "Lihat Trailer",       new Color(110, 40, 175));
        JButton btnPromo     = createMenuButton("🎟", "Gunakan Promo",       new Color(0, 155, 155));

        menuBox.add(btnJamStudio); menuBox.add(btnMakanan); menuBox.add(btnReset);
        menuBox.add(btnKursi);     menuBox.add(btnTrailer); menuBox.add(btnPromo);

        btnJamStudio.addActionListener(e -> showJamStudioDialog());
        btnMakanan.addActionListener(e   -> showFoodDialog());         // ← uses FoodMenuPanel
        btnKursi.addActionListener(e     -> showKursiDialog());        // ← uses SeatSelectionPanel
        btnReset.addActionListener(e     -> resetPesanan());
        btnTrailer.addActionListener(e   -> JOptionPane.showMessageDialog(this,
                "Fitur Lihat Trailer segera hadir!", "Info", JOptionPane.INFORMATION_MESSAGE));
        btnPromo.addActionListener(e     -> JOptionPane.showMessageDialog(this,
                "Fitur Gunakan Promo segera hadir!", "Info", JOptionPane.INFORMATION_MESSAGE));

        wrapper.add(menuBox, BorderLayout.CENTER);
        return wrapper;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DETAIL PESANAN   (renamed "Harga Satuan" → "Makanan & Minuman")
    // ──────────────────────────────────────────────────────────────────────────
    private JPanel buildDetailPesanan() {
        JPanel section = new JPanel(new BorderLayout(0, 6));
        section.setBackground(new Color(42, 42, 42));

        JLabel title = new JLabel("Detail Pesanan", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        section.add(title, BorderLayout.NORTH);

        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(new Color(50, 50, 50));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(72, 72, 72)),
                new EmptyBorder(12, 14, 14, 14)));

        for (JTextField f : new JTextField[]{txtJudul, txtJamTayang, txtStudio, txtKursi, txtMakanan, txtTotal}) {
            styleField(f);
            f.setEditable(false);
        }

        ph(txtJudul,     "");
        ph(txtJamTayang, "Pilih Jam");
        ph(txtStudio,    "Pilih Studio");
        ph(txtKursi,     "Pilih Kursi");
        ph(txtMakanan,   "-");
        ph(txtTotal,     "-");

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.insets  = new Insets(2, 4, 2, 4);

        g.gridy = 0; g.gridx = 0; box.add(lbl("Judul Film"),        g);
                     g.gridx = 1; box.add(lbl("Jam Tayang"),         g);
        g.gridy = 1; g.gridx = 0; box.add(txtJudul,                 g);
                     g.gridx = 1; box.add(txtJamTayang,              g);
        g.gridy = 2; g.gridx = 0; box.add(lbl("Studio"),            g);
                     g.gridx = 1; box.add(lbl("Kursi"),              g);
        g.gridy = 3; g.gridx = 0; box.add(txtStudio,                g);
                     g.gridx = 1; box.add(txtKursi,                  g);
        g.gridy = 4; g.gridx = 0; box.add(lbl("Makanan & Minuman"), g);   // ← renamed label
                     g.gridx = 1; box.add(lbl("Total Harga"),        g);
        g.gridy = 5; g.gridx = 0; box.add(txtMakanan,               g);   // ← renamed field
                     g.gridx = 1; box.add(txtTotal,                  g);

        section.add(box, BorderLayout.CENTER);
        return section;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // BOTTOM BAR
    // ──────────────────────────────────────────────────────────────────────────
    private JPanel buildBottomBar() {
        JPanel bottom = new JPanel(new BorderLayout(0, 4));
        bottom.setBackground(new Color(42, 42, 42));
        bottom.setBorder(new EmptyBorder(4, 0, 0, 0));

        JButton btnConfirm = new JButton();
        btnConfirm.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 10));
        btnConfirm.setBackground(new Color(35, 45, 130));
        btnConfirm.setFocusPainted(false);
        btnConfirm.setPreferredSize(new Dimension(340, 56));
        btnConfirm.setBorder(BorderFactory.createLineBorder(new Color(210, 165, 0), 2));
        btnConfirm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel("💳");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JLabel textLbl = new JLabel("Confirm & Pay");
        textLbl.setForeground(new Color(255, 200, 0));
        textLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));

        btnConfirm.add(iconLbl);
        btnConfirm.add(textLbl);

        btnConfirm.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnConfirm.setBackground(new Color(45, 58, 165)); }
            public void mouseExited(MouseEvent e)  { btnConfirm.setBackground(new Color(35, 45, 130)); }
        });

        btnConfirm.addActionListener(e -> confirmAndPay());

        JLabel lblLogout = new JLabel("Log out", SwingConstants.CENTER);
        lblLogout.setForeground(new Color(100, 160, 255));
        lblLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblLogout.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
                new LoginForm().setVisible(true);
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(new Color(42, 42, 42));
        btnRow.add(btnConfirm);

        bottom.add(btnRow,    BorderLayout.CENTER);
        bottom.add(lblLogout, BorderLayout.SOUTH);
        return bottom;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DIALOG 1 — JAM & STUDIO
    // ──────────────────────────────────────────────────────────────────────────
    private void showJamStudioDialog() {
        JDialog dialog = new JDialog(this, "Pilih Jam & Studio", true);
        dialog.setSize(530, 330);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(new Color(52, 52, 52));
        main.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel studioRow = new JPanel(new GridLayout(1, 4, 8, 0));
        studioRow.setBackground(new Color(52, 52, 52));
        JButton[] sBtns = new JButton[STUDIO_TYPES.length];
        String[] tempStudio = {selectedStudioName};

        for (int i = 0; i < STUDIO_TYPES.length; i++) {
            final int idx = i;
            sBtns[i] = createStudioBtn(STUDIO_TYPES[i]);
            studioRow.add(sBtns[i]);
        }
        // Highlight after all buttons are added
        for (int i = 0; i < STUDIO_TYPES.length; i++) {
            if (STUDIO_LABELS[i].equals(selectedStudioName)) highlightStudio(sBtns, i);
        }
        for (int i = 0; i < STUDIO_TYPES.length; i++) {
            final int idx = i;
            sBtns[i].addActionListener(e -> { highlightStudio(sBtns, idx); tempStudio[0] = STUDIO_LABELS[idx]; });
        }

        JPanel timeGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        timeGrid.setBackground(new Color(52, 52, 52));
        JButton[] tBtns = new JButton[TIME_SLOTS.length];
        String[] tempTime = {selectedTime};

        for (int i = 0; i < TIME_SLOTS.length; i++) {
            tBtns[i] = createTimeBtn(TIME_SLOTS[i]);
            timeGrid.add(tBtns[i]);
        }
        // Highlight after all buttons are added
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            if (TIME_SLOTS[i].equals(selectedTime)) highlightTime(tBtns, i);
        }
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            final int idx = i;
            tBtns[i].addActionListener(e -> { highlightTime(tBtns, idx); tempTime[0] = TIME_SLOTS[idx]; });
        }

        JButton btnOk = new JButton("Konfirmasi");
        btnOk.setBackground(new Color(45, 85, 200));
        btnOk.setForeground(Color.WHITE);
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOk.setFocusPainted(false);
        btnOk.setBorder(BorderFactory.createLineBorder(new Color(90, 130, 255)));
        btnOk.setPreferredSize(new Dimension(130, 36));
        btnOk.addActionListener(e -> {
            if (tempStudio[0].isEmpty() || tempTime[0].isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Pilih studio dan jam tayang!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedStudioName = tempStudio[0];
            selectedTime       = tempTime[0];
            syncDetailFields();
            dialog.dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(new Color(52, 52, 52));
        btnRow.add(btnOk);

        main.add(studioRow, BorderLayout.NORTH);
        main.add(timeGrid,  BorderLayout.CENTER);
        main.add(btnRow,    BorderLayout.SOUTH);
        dialog.add(main);
        dialog.setVisible(true);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DIALOG 2 — PILIH KURSI  (uses SeatSelectionPanel — matches picture 1)
    // ──────────────────────────────────────────────────────────────────────────
    private void showKursiDialog() {
        JDialog dialog = new JDialog(this, "Pilih Kursi", true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Create once; reuse on subsequent opens so selections are remembered
        if (persistentSeatPanel == null) {
            persistentSeatPanel = new SeatSelectionPanel((SeatSelectionPanel.SeatCallback) seats -> { /* live update */ });
            persistentSeatPanel.setHargaSatuan(HARGA_INT);
        }

        JButton btnOk = new JButton("Konfirmasi Kursi");
        btnOk.setBackground(new Color(45, 85, 200));
        btnOk.setForeground(Color.WHITE);
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOk.setFocusPainted(false);
        btnOk.setBorder(BorderFactory.createLineBorder(new Color(90, 130, 255)));
        btnOk.setPreferredSize(new Dimension(180, 36));
        btnOk.addActionListener(e -> {
            selectedSeats = new ArrayList<>(persistentSeatPanel.getSelectedSeats());
            updateKursiField();
            dialog.dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(new Color(42, 42, 42));
        btnRow.add(btnOk);

        JPanel main = new JPanel(new BorderLayout(0, 8));
        main.setBackground(new Color(42, 42, 42));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));
        main.add(persistentSeatPanel, BorderLayout.CENTER);
        main.add(btnRow,              BorderLayout.SOUTH);

        dialog.add(main);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DIALOG 3 — MAKANAN & MINUMAN  (uses FoodMenuPanel — matches picture 2)
    // ──────────────────────────────────────────────────────────────────────────
    private void showFoodDialog() {
        JDialog dialog = new JDialog(this, "Makanan & Minuman", true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JLabel totalLbl = new JLabel("Total Makanan: Rp 0", SwingConstants.RIGHT);
        totalLbl.setForeground(new Color(255, 200, 0));
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Create the persistent panel once; the callback updates the live total label.
        // On re-open we create a new wrapper with the fresh label reference,
        // but keep the internal quantities map by reusing the same panel.
        // We do this by keeping one instance with a mutable-callback pattern:
        // Since FoodMenuPanel takes callback in constructor, we keep a fresh panel
        // each time but restore quantities via a thin wrapper that calls resetOrder + replay.
        // Simplest correct approach: create panel each time but copy quantities from previous.

        // Keep a quantity snapshot map on the dashboard side:
        if (persistentFoodPanel == null) {
            persistentFoodPanel = new FoodMenuPanel((items, total) ->
                    totalLbl.setText("Total Makanan: " + formatRp(total)));
        } else {
            // Panel already exists with previous quantities — just refresh its label
            totalLbl.setText("Total Makanan: " + formatRp(persistentFoodPanel.getFoodTotal()));
        }

        FoodMenuPanel panel = persistentFoodPanel;

        JButton btnOk = new JButton("Tambahkan ke Pesanan");
        btnOk.setBackground(new Color(45, 85, 200));
        btnOk.setForeground(Color.WHITE);
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOk.setFocusPainted(false);
        btnOk.setBorder(BorderFactory.createLineBorder(new Color(90, 130, 255)));
        btnOk.setPreferredSize(new Dimension(210, 36));
        btnOk.addActionListener(e -> {
            foodTotal   = panel.getFoodTotal();
            foodSummary = panel.getOrderSummary();
            updateMakananField();
            recalcTotal();
            dialog.dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(new Color(42, 42, 42));
        btnRow.add(btnOk);

        JPanel bottomBar = new JPanel(new BorderLayout(0, 6));
        bottomBar.setBackground(new Color(42, 42, 42));
        bottomBar.add(totalLbl, BorderLayout.NORTH);
        bottomBar.add(btnRow,   BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout(0, 10));
        main.setBackground(new Color(42, 42, 42));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));
        main.add(panel,     BorderLayout.CENTER);
        main.add(bottomBar, BorderLayout.SOUTH);

        dialog.add(main);
        dialog.setSize(560, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FIELD UPDATE HELPERS
    // ──────────────────────────────────────────────────────────────────────────
    private void updateKursiField() {
        if (selectedSeats.isEmpty()) {
            ph(txtKursi, "Pilih Kursi");
        } else {
            List<String> sorted = new ArrayList<>(selectedSeats);
            Collections.sort(sorted);
            txtKursi.setText(String.join(", ", sorted));
            txtKursi.setForeground(Color.WHITE);
        }
        recalcTotal();
    }

    /** Update the "Makanan & Minuman" field with the food order summary. */
    private void updateMakananField() {
        if (foodSummary == null || foodSummary.isEmpty()) {
            ph(txtMakanan, "-");
        } else {
            txtMakanan.setText(foodSummary);
            txtMakanan.setForeground(Color.WHITE);
        }
    }

    private void recalcTotal() {
        long seatTotal = (long) selectedSeats.size() * HARGA_INT;
        long grand     = seatTotal + foodTotal;

        if (grand == 0 && selectedSeats.isEmpty()) {
            ph(txtTotal, "-");
        } else {
            txtTotal.setText(formatRp(grand));
            txtTotal.setForeground(Color.WHITE);
        }
    }

    private void syncDetailFields() {
        if (!selectedStudioName.isEmpty()) { txtStudio.setText(selectedStudioName); txtStudio.setForeground(Color.WHITE); }
        if (!selectedTime.isEmpty())       { txtJamTayang.setText(selectedTime);    txtJamTayang.setForeground(Color.WHITE); }
        recalcTotal();
    }

    private void resetPesanan() {
        selectedStudioName = ""; selectedTime = ""; selectedSeats.clear();
        foodTotal = 0; foodSummary = "";
        // Reset persistent sub-panels so their internal state clears too
        if (persistentSeatPanel != null) persistentSeatPanel.resetSelections();
        if (persistentFoodPanel  != null) persistentFoodPanel.resetOrder();
        ph(txtStudio,    "Pilih Studio");
        ph(txtJamTayang, "Pilih Jam");
        ph(txtKursi,     "Pilih Kursi");
        ph(txtMakanan,   "-");
        ph(txtTotal,     "-");
        JOptionPane.showMessageDialog(this, "Pesanan berhasil direset.", "Reset", JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmAndPay() {
        if (txtJudul.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih film terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE); return;
        }
        if (selectedStudioName.isEmpty() || selectedTime.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih studio dan jam tayang!", "Peringatan", JOptionPane.WARNING_MESSAGE); return;
        }
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih kursi terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE); return;
        }

        long seatTotal = (long) selectedSeats.size() * HARGA_INT;
        long subtotal = seatTotal + foodTotal;
        long promoValue = subtotal >= 665_000 ? 30_000 : 0;
        long finalTotal = Math.max(0, subtotal - promoValue);

        PaymentPageFrame paymentPage = new PaymentPageFrame(
                userId,
                selectedCinema,
                buildMovieTitleList(),
                new ArrayList<>(filmPosters),
                filmList.getSelectedIndex(),
                txtJudul.getText(),
                txtStudio.getText(),
                txtJamTayang.getText(),
                new ArrayList<>(selectedSeats),
                foodSummary,
                subtotal,
                promoValue,
                finalTotal
        );
        paymentPage.setVisible(true);
        dispose();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HIGHLIGHT HELPERS
    // ──────────────────────────────────────────────────────────────────────────
    private void highlightStudio(JButton[] btns, int sel) {
        for (int j = 0; j < btns.length; j++) {
            boolean s = (j == sel);
            btns[j].setBackground(s ? new Color(45, 85, 200) : new Color(58, 58, 58));
            btns[j].setBorder(s
                    ? BorderFactory.createLineBorder(new Color(90, 130, 255), 2)
                    : BorderFactory.createLineBorder(new Color(95, 95, 95), 1));
        }
    }

    private void highlightTime(JButton[] btns, int sel) {
        for (int j = 0; j < btns.length; j++) {
            boolean s = (j == sel);
            btns[j].setBackground(s ? new Color(170, 15, 15) : new Color(58, 58, 58));
            btns[j].setBorder(s
                    ? BorderFactory.createLineBorder(new Color(240, 60, 60), 2)
                    : BorderFactory.createLineBorder(new Color(130, 100, 0), 2));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // BUTTON FACTORIES
    // ──────────────────────────────────────────────────────────────────────────
    private JButton createMenuButton(String icon, String labelText, Color bg) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout(4, 0));
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                new EmptyBorder(8, 10, 8, 10)));

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 17));
        iconLbl.setPreferredSize(new Dimension(28, 28));

        String html = "<html><center>" + labelText.replace("\n", "<br>") + "</center></html>";
        JLabel textLbl = new JLabel(html, SwingConstants.CENTER);
        textLbl.setForeground(Color.WHITE);
        textLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));

        btn.add(iconLbl, BorderLayout.WEST);
        btn.add(textLbl, BorderLayout.CENTER);

        Color hover = new Color(
                Math.min(bg.getRed()   + 30, 255),
                Math.min(bg.getGreen() + 30, 255),
                Math.min(bg.getBlue()  + 30, 255));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private JButton createStudioBtn(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(new Color(58, 58, 58));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(new Color(95, 95, 95), 1));
        btn.setPreferredSize(new Dimension(0, 36));
        return btn;
    }

    private JButton createTimeBtn(String time) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setBackground(new Color(58, 58, 58));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(new Color(130, 100, 0), 2));
        btn.setPreferredSize(new Dimension(0, 72));

        JLabel lblTime = new JLabel(time, SwingConstants.CENTER);
        lblTime.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTime.setForeground(Color.WHITE);

        JLabel lblPrice = new JLabel(HARGA_DEFAULT, SwingConstants.CENTER);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPrice.setForeground(Color.WHITE);
        lblPrice.setOpaque(true);
        lblPrice.setBackground(new Color(160, 110, 0));
        lblPrice.setBorder(new EmptyBorder(2, 6, 2, 6));

        JPanel priceRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        priceRow.setOpaque(false);
        priceRow.add(lblPrice);

        JPanel inner = new JPanel(new BorderLayout(0, 2));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(4, 4, 4, 4));
        inner.add(lblTime,  BorderLayout.CENTER);
        inner.add(priceRow, BorderLayout.SOUTH);
        btn.add(inner, BorderLayout.CENTER);
        return btn;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ──────────────────────────────────────────────────────────────────────────
    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(34, 34, 34));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                new EmptyBorder(5, 8, 5, 8)));
    }

    private void ph(JTextField f, String text) {
        f.setText(text);
        f.setForeground(new Color(130, 130, 130));
    }

    private String formatRp(long amount) {
        return "Rp " + String.format("%,d", amount).replace(',', '.');
    }

    private List<String> buildMovieTitleList() {
        List<String> titles = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            titles.add(listModel.get(i));
        }
        return titles;
    }

    private void loadPoster(int idx) {
        if (idx < 0 || idx >= filmPosters.size()) return;
        String url = filmPosters.get(idx);
        if (url == null || url.isEmpty()) { lblPoster.setIcon(null); return; }
        new Thread(() -> {
            try {
                ImageIcon ic = new ImageIcon(new URL(url));
                Image sc = ic.getImage().getScaledInstance(115, 155, Image.SCALE_SMOOTH);
                SwingUtilities.invokeLater(() -> lblPoster.setIcon(new ImageIcon(sc)));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> lblPoster.setIcon(null));
            }
        }).start();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DATABASE
    // ──────────────────────────────────────────────────────────────────────────
    private void loadFilm() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet  rs   = conn.createStatement().executeQuery("SELECT * FROM movies");
            while (rs.next()) {
                filmIds.add(rs.getInt("id"));
                listModel.addElement(rs.getString("judul"));
                try { filmPosters.add(rs.getString("poster_url")); }
                catch (Exception ex) { filmPosters.add(""); }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDetailFilm(int filmId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet  rs   = conn.createStatement()
                    .executeQuery("SELECT * FROM movies WHERE id=" + filmId);
            if (rs.next()) {
                txtJudul.setText(rs.getString("judul"));
                txtJudul.setForeground(Color.WHITE);
                ph(txtJamTayang, "Pilih Jam");
                ph(txtStudio,    "Pilih Studio");
                ph(txtKursi,     "Pilih Kursi");
                ph(txtMakanan,   "-");
                ph(txtTotal,     "-");
                selectedStudioName = ""; selectedTime = "";
                selectedSeats.clear(); foodTotal = 0; foodSummary = "";
                if (persistentSeatPanel != null) persistentSeatPanel.resetSelections();
                if (persistentFoodPanel  != null) persistentFoodPanel.resetOrder();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FILM LIST CELL RENDERER
    // ──────────────────────────────────────────────────────────────────────────
    private class FilmCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(true);
            panel.setBorder(new EmptyBorder(6, 8, 6, 8));
            panel.setBackground(isSelected
                    ? new Color(50, 88, 168)
                    : (index % 2 == 0 ? new Color(36, 36, 36) : new Color(42, 42, 42)));

            JLabel thumb = new JLabel();
            thumb.setPreferredSize(new Dimension(44, 64));
            thumb.setBackground(new Color(24, 24, 24));
            thumb.setOpaque(true);

            if (index < filmPosters.size()) {
                String url = filmPosters.get(index);
                if (url != null && !url.isEmpty()) {
                    try {
                        ImageIcon ic = new ImageIcon(new URL(url));
                        thumb.setIcon(new ImageIcon(
                                ic.getImage().getScaledInstance(44, 64, Image.SCALE_SMOOTH)));
                    } catch (Exception ex) { /* no poster */ }
                }
            }

            JLabel title = new JLabel(value.toString());
            title.setForeground(Color.WHITE);
            title.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            title.setVerticalAlignment(SwingConstants.CENTER);

            panel.add(thumb, BorderLayout.WEST);
            panel.add(title, BorderLayout.CENTER);
            return panel;
        }
    }
}