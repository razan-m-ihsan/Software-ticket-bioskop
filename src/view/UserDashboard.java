package view;

import config.DatabaseConnection;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import model.Promo;

public class UserDashboard extends JFrame {

    private int userId;

    // ── Film list ──────────────────────────────────────────────────────────────
    private DefaultListModel<String> listModel   = new DefaultListModel<>();
    private JList<String>            filmList    = new JList<>(listModel);
    private ArrayList<Integer>       filmIds     = new ArrayList<>();
    private ArrayList<String>        filmPosters = new ArrayList<>();
    private String                   currentPosterPreviewPath = "";

    // ── Detail fields ──────────────────────────────────────────────────────────
    private JTextField txtJudul       = new JTextField();
    private JTextField txtJamTayang   = new JTextField();
    private JTextField txtStudio      = new JTextField();
    private JTextField txtKursi       = new JTextField();
    private JTextField txtMakanan     = new JTextField();   // renamed from txtHarga
    private JTextField txtTotal       = new JTextField();

    // ── Poster preview ─────────────────────────────────────────────────────────
    private JLabel lblPoster;
    private JLabel lblSynopsis = new JLabel();
    private JLabel lblGenre    = new JLabel();
    private JLabel lblDurasi   = new JLabel();
    private JLabel lblRating   = new JLabel();

    private String currentSynopsis = "-";
    private String currentGenre    = "-";
    private String currentDurasi   = "-";
    private String currentRating   = "-";

    // ── State ──────────────────────────────────────────────────────────────────
    private String       selectedStudioName = "";
    private String       selectedTime       = "";
    private String       selectedCinema     = "Cinepolis - Palembang Icon";
    private List<String> selectedSeats      = new ArrayList<>();
    private long         foodTotal          = 0;
    private String       foodSummary        = "";   // e.g. "Popcorn Caramel Large x2, ..."
    private Promo        selectedPromo      = null; // Selected promo for discount

    private static final Map<String, Set<String>> paidSeatHistory = new HashMap<>();

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
    // Harga per studio (dalam Rupiah)
    private static final int[] STUDIO_PRICES = {
        75_000,   // Reguler 2D
        85_000,   // Reguler 3D
        100_000,  // Premium
        150_000   // IMAX
    };

    // ─────────────────────────────────────────────────────────────────────────
    // ── State restoration (used when returning from PaymentPageFrame) ──────────
    public static class RestoredState {
        public final int          filmIndex;
        public final String       studioName;
        public final String       time;
        public final String       cinema;
        public final List<String> seats;
        public final long         foodTotal;
        public final String       foodSummary;
        public final Promo        promo;

        public RestoredState(int filmIndex, String studioName, String time,
                            String cinema, List<String> seats,
                            long foodTotal, String foodSummary, Promo promo) {
            this.filmIndex   = filmIndex;
            this.studioName  = studioName;
            this.time        = time;
            this.cinema      = cinema;
            this.seats       = seats;
            this.foodTotal   = foodTotal;
            this.foodSummary = foodSummary;
            this.promo       = promo;
        }
    }
    
    public UserDashboard(int userId) {
    this(userId, null);
}

    public UserDashboard(int userId, RestoredState state) {
        this.userId = userId;
        setTitle("CineTix");
        setSize(980, 750);
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

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

        // Restore previous order state if returning from payment page
        if (state != null) {
            restoreState(state);
        }

        setVisible(true);
    }

    private void restoreState(RestoredState state) {
        if (state.cinema != null && !state.cinema.isEmpty()) {
            selectedCinema = state.cinema;
        }

        // Select the film (this triggers loadDetailFilm which resets fields,
        // so we repopulate everything after)
        if (state.filmIndex >= 0 && state.filmIndex < listModel.size()) {
            filmList.setSelectedIndex(state.filmIndex);
        }

        selectedStudioName = state.studioName != null ? state.studioName : "";
        selectedTime       = state.time       != null ? state.time       : "";

        if (!selectedStudioName.isEmpty()) {
            txtStudio.setText(selectedStudioName);
            txtStudio.setForeground(Color.WHITE);
        }
        if (!selectedTime.isEmpty()) {
            txtJamTayang.setText(selectedTime);
            txtJamTayang.setForeground(Color.WHITE);
        }

        selectedSeats.clear();
        if (state.seats != null) selectedSeats.addAll(state.seats);
        updateKursiField();

        foodTotal   = state.foodTotal;
        foodSummary = state.foodSummary != null ? state.foodSummary : "";
        updateMakananField();

        selectedPromo = state.promo;

        recalcTotal();
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
        lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblPoster.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentPosterPreviewPath != null && !currentPosterPreviewPath.isEmpty()) {
                    showPosterZoom(currentPosterPreviewPath);
                }
            }
        });

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
        JButton btnHistory   = createMenuButton("🧾", "Riwayat\nPembayaran", new Color(110, 40, 175));
        JButton btnPromo     = createMenuButton("🎟", "Gunakan Promo",       new Color(0, 155, 155));

        menuBox.add(btnJamStudio); menuBox.add(btnMakanan); menuBox.add(btnReset);
        menuBox.add(btnKursi);     menuBox.add(btnHistory); menuBox.add(btnPromo);

        btnJamStudio.addActionListener(e -> showJamStudioDialog());
        btnMakanan.addActionListener(e   -> showFoodDialog());         // ← uses FoodMenuPanel
        btnKursi.addActionListener(e     -> showKursiDialog());        // ← uses SeatSelectionPanel
        btnReset.addActionListener(e     -> resetPesanan());
        btnHistory.addActionListener(e   -> new TransactionHistoryForm(userId).setVisible(true));
        btnPromo.addActionListener(e     -> showPromoDialog());

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

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
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
        // Validasi: Film harus dipilih dulu
        if (filmList.getSelectedIndex() < 0 || txtJudul.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih film terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

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

        // Create placeholder for time grid initially hidden
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(52, 52, 52));

        JPanel timeGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        timeGrid.setBackground(new Color(52, 52, 52));
        timeGrid.setVisible(false); // Hidden until studio is selected
        
        JButton[] tBtns = new JButton[TIME_SLOTS.length];
        String[] tempTime = {selectedTime};
        int[] currentStudioPrice = {HARGA_INT}; // Track current studio price

        for (int i = 0; i < TIME_SLOTS.length; i++) {
            tBtns[i] = createTimeBtn(TIME_SLOTS[i], currentStudioPrice[0]);
            timeGrid.add(tBtns[i]);
        }
        // Highlight after all buttons are added
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            if (TIME_SLOTS[i].equals(selectedTime)) highlightTime(tBtns, i);
        }

        // Placeholder message when no studio selected
        JLabel placeholderMsg = new JLabel("Pilih studio terlebih dahulu untuk melihat jam tayang", SwingConstants.CENTER);
        placeholderMsg.setForeground(new Color(150, 150, 150));
        placeholderMsg.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        centerPanel.add(placeholderMsg, BorderLayout.CENTER);
        
        // Studio selection enables jam buttons and updates prices
        for (int i = 0; i < STUDIO_TYPES.length; i++) {
            final int idx = i;
            sBtns[i].addActionListener(e -> { 
                highlightStudio(sBtns, idx); 
                tempStudio[0] = STUDIO_LABELS[idx];
                currentStudioPrice[0] = STUDIO_PRICES[idx];
                // Update price labels on all time buttons
                updateTimeBtnPrices(tBtns, STUDIO_PRICES[idx]);
                // Show time grid and enable jam buttons ketika studio dipilih
                timeGrid.setVisible(true);
                placeholderMsg.setVisible(false);
                centerPanel.removeAll();
                centerPanel.add(timeGrid, BorderLayout.CENTER);
                centerPanel.revalidate();
                centerPanel.repaint();
                for (JButton btn : tBtns) btn.setEnabled(true);
            });
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

        main.add(studioRow,    BorderLayout.NORTH);
        main.add(centerPanel,  BorderLayout.CENTER);
        main.add(btnRow,       BorderLayout.SOUTH);
        dialog.add(main);
        dialog.setVisible(true);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DIALOG 2 — PILIH KURSI  (uses SeatSelectionPanel — matches picture 1)
    // ──────────────────────────────────────────────────────────────────────────
    private String persistentSeatKey = "";

    private void showKursiDialog() {
        // Validasi: Studio dan Jam harus dipilih dulu
        if (selectedStudioName.isEmpty() || selectedTime.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih studio dan jam tayang terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Pilih Kursi", true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        String seatKey = buildSeatSelectionKey();
        if (persistentSeatPanel == null || !seatKey.equals(persistentSeatKey)) {
            java.util.List<String> takenSeats = loadTakenSeatsForCurrentSelection();
            persistentSeatPanel = new SeatSelectionPanel((SeatSelectionPanel.SeatCallback) seats -> { /* live update */ }, takenSeats);
            persistentSeatKey = seatKey;
        } else {
            persistentSeatPanel.setTakenSeats(loadTakenSeatsForCurrentSelection());
        }
        // Update harga sesuai studio yang dipilih
        persistentSeatPanel.setHargaSatuan(getStudioPrice(selectedStudioName));

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
        // Validasi: Kursi harus dipilih dulu
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih kursi terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

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
    // DIALOG 4 — PILIH PROMO
    // ──────────────────────────────────────────────────────────────────────────
    private void showPromoDialog() {
        // Validasi: Film harus dipilih dulu (promo bisa digunakan kapan saja setelah film dipilih)
        if (filmList.getSelectedIndex() < 0 || txtJudul.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih film terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PromoPage promoPage = new PromoPage(this);
        promoPage.setVisible(true);

        // If user selected a promo, apply it
        if (promoPage.getSelectedPromo() != null) {
            selectedPromo = promoPage.getSelectedPromo();
            String diskonInfo = selectedPromo.getDiskonPersen() > 0 ?
                selectedPromo.getDiskonPersen() + "%" :
                "Rp " + String.format("%,.0f", selectedPromo.getDiskonRupiah());
            JOptionPane.showMessageDialog(this,
                "Promo \"" + selectedPromo.getKodePromo() + "\" berhasil diterapkan!\n" +
                "Diskon: " + diskonInfo + "\nSilakan lanjutkan ke pembayaran.",
                "Promo Diterapkan", JOptionPane.INFORMATION_MESSAGE);
            // Recalculate total with new promo
            recalcTotal();
        }
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

    private String buildSeatSelectionKey() {
        return txtJudul.getText().trim() + "|" + selectedStudioName + "|" + selectedTime;
    }

    private java.util.List<String> loadTakenSeatsForCurrentSelection() {
        String key = buildSeatSelectionKey();
        java.util.List<String> bookings = new ArrayList<>();
        int scheduleId = findScheduleId(txtJudul.getText(), selectedStudioName, selectedTime);
        if (scheduleId > 0) {
            bookings = getBookedSeatsForSchedule(scheduleId);
        }
        if (bookings.isEmpty()) {
            bookings = SeatSelectionPanel.generatePseudoTakenSeats(key);
        }
        Set<String> paid = paidSeatHistory.getOrDefault(key, Collections.emptySet());
        bookings.addAll(paid);
        return bookings;
    }

    private void addPaidSeatsToHistory(String key, List<String> seats) {
        if (key == null || key.isEmpty() || seats == null || seats.isEmpty()) return;
        Set<String> paid = paidSeatHistory.computeIfAbsent(key, k -> new HashSet<>());
        paid.addAll(seats);
    }

    private int findScheduleId(String movieTitle, String studioLabel, String jam) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String studioName = getStudioNameFromLabel(studioLabel);
            String query = "SELECT s.id FROM schedules s " +
                    "JOIN movies m ON s.movie_id = m.id " +
                    "JOIN studios st ON s.studio_id = st.id " +
                    "WHERE m.judul = ? AND st.nama_studio = ? AND s.jam_tayang = ? LIMIT 1";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, movieTitle);
            ps.setString(2, studioName);
            ps.setString(3, normalizeTimeForDb(jam));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private java.util.List<String> getBookedSeatsForSchedule(int scheduleId) {
        java.util.List<String> seats = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT nomor_kursi FROM transactions WHERE schedule_id = ? AND status = 'SUCCESS'"
            );
            ps.setInt(1, scheduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                seats.add(rs.getString("nomor_kursi"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return seats;
    }

    private String normalizeTimeForDb(String jam) {
        if (jam == null || jam.isEmpty()) return "";
        String normalized = jam.replace('.', ':').trim();
        if (normalized.length() == 5) normalized += ":00";
        return normalized;
    }

    private String getStudioNameFromLabel(String label) {
        if (label == null) return "";
        if (label.contains("Reguler 2D")) return "Reguler 2D";
        if (label.contains("Reguler 3D")) return "Reguler 3D";
        if (label.contains("Premium")) return "Premium";
        if (label.contains("IMAX")) return "IMAX";
        return label;
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
        // Use the actual studio price, not the default price
        int studioPrice = !selectedStudioName.isEmpty() ? getStudioPrice(selectedStudioName) : 0;
        long seatTotal = (long) selectedSeats.size() * studioPrice;
        long subtotal  = seatTotal + foodTotal;

        // Apply promo discount
        long promoValue = 0;
        if (selectedPromo != null && subtotal > 0) {
            if (selectedPromo.getDiskonPersen() > 0) {
                promoValue = (long) (subtotal * selectedPromo.getDiskonPersen() / 100);
            } else if (selectedPromo.getDiskonRupiah() > 0) {
                promoValue = (long) selectedPromo.getDiskonRupiah();
            }
        }
        long grand = Math.max(0, subtotal - promoValue);

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
        selectedPromo = null; // Reset selected promo
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

        long seatTotal = (long) selectedSeats.size() * getStudioPrice(selectedStudioName);
        long subtotal  = seatTotal + foodTotal;
        long promoValue = 0;

        if (selectedPromo != null) {
            if (selectedPromo.getDiskonPersen() > 0)
                promoValue = (long) (subtotal * selectedPromo.getDiskonPersen() / 100);
            else if (selectedPromo.getDiskonRupiah() > 0)
                promoValue = (long) selectedPromo.getDiskonRupiah();
        }
        long finalTotal = Math.max(0, subtotal - promoValue);

        String seatKey = buildSeatSelectionKey();
        addPaidSeatsToHistory(seatKey, selectedSeats);

        // ← NEW: capture state snapshot so PaymentPageFrame can restore it on Kembali
        RestoredState snapshot = new RestoredState(
                filmList.getSelectedIndex(),
                selectedStudioName,
                selectedTime,
                selectedCinema,
                new ArrayList<>(selectedSeats),
                foodTotal,
                foodSummary,
                selectedPromo
        );

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
                seatTotal,
                promoValue,
                finalTotal,
                snapshot   // ← NEW
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

    // ✅ Dapatkan harga berdasarkan studio yang dipilih
    private int getStudioPrice(String studioLabel) {
        for (int i = 0; i < STUDIO_LABELS.length; i++) {
            if (STUDIO_LABELS[i].equals(studioLabel)) {
                return STUDIO_PRICES[i];
            }
        }
        return HARGA_INT; // default price
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

    private JButton createTimeBtn(String time, int price) {
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

        JLabel lblPrice = new JLabel("Rp " + String.format("%,d", price).replace(',', '.'), SwingConstants.CENTER);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPrice.setForeground(Color.WHITE);
        lblPrice.setOpaque(true);
        lblPrice.setBackground(new Color(160, 110, 0));
        lblPrice.setBorder(new EmptyBorder(2, 6, 2, 6));
        lblPrice.setName("priceLabel"); // Set name for identification

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

    /** Update price labels on all time buttons when studio changes */
    private void updateTimeBtnPrices(JButton[] timeBtns, int newPrice) {
        String priceText = "Rp " + String.format("%,d", newPrice).replace(',', '.');
        for (JButton btn : timeBtns) {
            // Find the price label inside the button and update it
            Component[] components = btn.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    updatePriceLabelRecursive((JPanel) comp, priceText);
                }
            }
        }
    }

    /** Recursively find and update price label */
    private void updatePriceLabelRecursive(JPanel panel, String priceText) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel && "priceLabel".equals(comp.getName())) {
                ((JLabel) comp).setText(priceText);
                return;
            } else if (comp instanceof JPanel) {
                updatePriceLabelRecursive((JPanel) comp, priceText);
            }
        }
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

    private void styleInfoLabel(JLabel lbl) {
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setVerticalAlignment(SwingConstants.TOP);
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel key = new JLabel(label);
        key.setForeground(new Color(205, 205, 205));
        key.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel val = new JLabel(escapeHtml(value));
        val.setForeground(new Color(235, 235, 235));
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        row.add(key, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    private void setPosterDetail(String synopsis, String genre, String durasi, String rating) {
        currentSynopsis = synopsis != null && !synopsis.isBlank() ? synopsis : "-";
        currentGenre = genre != null && !genre.isBlank() ? genre : "-";
        currentDurasi = durasi != null && !durasi.isBlank() ? durasi : "-";
        currentRating = rating != null && !rating.isBlank() ? rating : "-";

        lblSynopsis.setText(formatSynopsisHtml(currentSynopsis));
        lblGenre.setText("<html><b>Genre:</b> " + escapeHtml(currentGenre) + "</html>");
        lblDurasi.setText("<html><b>Durasi:</b> " + escapeHtml(currentDurasi) + "</html>");
        lblRating.setText("<html><b>Rating:</b> " + escapeHtml(currentRating) + "</html>");
    }

    private String formatSynopsisHtml(String text) {
        if (text == null || text.isBlank()) {
            text = "-";
        }
        return "<html><body style='white-space:pre-wrap; width:260px; font-family:Segoe UI; font-size:12px; line-height:1.4;'>"
                + escapeHtml(text)
                + "</body></html>";
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("\n", "<br>");
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
        String posterPath = filmPosters.get(idx);
        if (posterPath == null || posterPath.isEmpty()) {
            currentPosterPreviewPath = "";
            lblPoster.setIcon(null);
            return;
        }
        currentPosterPreviewPath = posterPath;
        new Thread(() -> {
            ImageIcon icon = loadPosterIcon(posterPath, 115, 155);
            SwingUtilities.invokeLater(() -> lblPoster.setIcon(icon));
        }).start();
    }

    private void showPosterZoom(String posterPath) {
        JDialog zoomDialog = new JDialog(this, "Poster", true);
        zoomDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        zoomDialog.setSize(780, 600);
        zoomDialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(25, 25, 25));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel zoomLabel = new JLabel();
        zoomLabel.setHorizontalAlignment(SwingConstants.CENTER);
        zoomLabel.setVerticalAlignment(SwingConstants.CENTER);
        zoomLabel.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));

        ImageIcon zoomIcon = loadPosterIcon(posterPath, 420, 520);
        if (zoomIcon != null) {
            zoomLabel.setIcon(zoomIcon);
        } else {
            zoomLabel.setText("No Image");
            zoomLabel.setForeground(new Color(220, 220, 220));
            zoomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        }

        JPanel detailsWrapper = new JPanel(new BorderLayout(0, 16));
        detailsWrapper.setOpaque(false);
        detailsWrapper.setBorder(new EmptyBorder(12, 16, 0, 0));
        detailsWrapper.setPreferredSize(new Dimension(300, 0));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel(txtJudul.getText());
        titleLabel.setForeground(new Color(245, 215, 110));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel subtitleLabel = new JLabel("Detail Film");
        subtitleLabel.setForeground(new Color(220, 220, 220));
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 12));
        infoPanel.setOpaque(false);
        infoPanel.add(createInfoRow("Genre", currentGenre));
        infoPanel.add(createInfoRow("Durasi", currentDurasi));
        infoPanel.add(createInfoRow("Rating", currentRating));

        JLabel synopsisTitle = new JLabel("Sinopsis");
        synopsisTitle.setForeground(new Color(245, 245, 245));
        synopsisTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel synopsisLabel = new JLabel(formatSynopsisHtml(currentSynopsis));
        synopsisLabel.setVerticalAlignment(SwingConstants.TOP);

        JTextArea synopsisArea = new JTextArea(currentSynopsis);
        synopsisArea.setLineWrap(true);
        synopsisArea.setWrapStyleWord(true);
        synopsisArea.setEditable(false);
        synopsisArea.setFocusable(false);
        synopsisArea.setOpaque(false);
        synopsisArea.setForeground(Color.WHITE);
        synopsisArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        synopsisArea.setBorder(null);
        synopsisArea.setMargin(new Insets(0, 0, 0, 0));

        JScrollPane synopsisScroll = new JScrollPane(synopsisArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        synopsisScroll.setBorder(BorderFactory.createEmptyBorder());
        synopsisScroll.getViewport().setOpaque(false);
        synopsisScroll.setOpaque(false);
        synopsisScroll.setPreferredSize(new Dimension(280, 260));

        JPanel synopsisPanel = new JPanel(new BorderLayout(0, 8));
        synopsisPanel.setOpaque(false);
        synopsisPanel.add(synopsisTitle, BorderLayout.NORTH);
        synopsisPanel.add(synopsisScroll, BorderLayout.CENTER);

        JButton btnClose = new JButton("Tutup");
        btnClose.setBackground(new Color(180, 60, 60));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> zoomDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnClose);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        centerPanel.add(synopsisPanel, BorderLayout.CENTER);

        detailsWrapper.add(titlePanel, BorderLayout.NORTH);
        detailsWrapper.add(centerPanel, BorderLayout.CENTER);
        detailsWrapper.add(buttonPanel, BorderLayout.SOUTH);

        JPanel posterContainer = new JPanel(new BorderLayout());
        posterContainer.setOpaque(false);
        posterContainer.add(zoomLabel, BorderLayout.CENTER);

        root.add(posterContainer, BorderLayout.CENTER);
        root.add(detailsWrapper, BorderLayout.EAST);

        zoomDialog.setContentPane(root);
        zoomDialog.setVisible(true);
    }

    private ImageIcon loadPosterIcon(String path, int width, int height) {
        if (path == null || path.isEmpty()) return null;
        try {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                ImageIcon ic = new ImageIcon(new URL(path));
                return new ImageIcon(ic.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }

            String normalized = path.startsWith("/") ? path.substring(1) : path;
            URL resource = getClass().getResource("/" + normalized);
            if (resource != null) {
                ImageIcon ic = new ImageIcon(resource);
                return new ImageIcon(ic.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
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
        } catch (Exception ex) {
            // ignore and return no icon
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

    // ──────────────────────────────────────────────────────────────────────────
    // DATABASE
    // ──────────────────────────────────────────────────────────────────────────
    private void loadFilm() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet  rs   = conn.createStatement().executeQuery("SELECT * FROM movies");
            while (rs.next()) {
                filmIds.add(rs.getInt("id"));
                String title = rs.getString("judul");
                listModel.addElement(title);
                String posterPath = "";
                try { posterPath = rs.getString("poster_url"); } catch (Exception ex) { /* fallback to local assets */ }
                if (posterPath == null || posterPath.isEmpty()) {
                    posterPath = getLocalPosterPathForTitle(title);
                }
                filmPosters.add(posterPath);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDetailFilm(int filmId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM movies WHERE id=?");
            ps.setInt(1, filmId);
            ResultSet rs = ps.executeQuery();
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
                selectedPromo = null;
                if (persistentSeatPanel != null) persistentSeatPanel.resetSelections();
                if (persistentFoodPanel  != null) persistentFoodPanel.resetOrder();

                String synopsis = rs.getString("sinopsis");
                String genre = rs.getString("genre");
                String durasi = rs.getString("durasi");
                String rating = rs.getString("rating_usia");
                setPosterDetail(synopsis, genre, durasi + " menit", rating);
            }
            rs.close(); ps.close(); conn.close();
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
                String posterPath = filmPosters.get(index);
                if (posterPath != null && !posterPath.isEmpty()) {
                    ImageIcon ic = loadPosterIcon(posterPath, 44, 64);
                    if (ic != null) {
                        thumb.setIcon(ic);
                    }
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