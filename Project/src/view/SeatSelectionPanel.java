package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SeatSelectionPanel extends JPanel {

    // ── Layout constants ──────────────────────────────────────
    private static final char[]   ROWS       = {'A', 'B', 'D', 'F', 'G', 'H'};
    private static final int      COLS       = 18;
    private static final int      AISLE_AFTER = 9;   // gap between col 9 and 10
    private static final int      BTN_SIZE   = 28;
    private static final int      BTN_GAP    = 4;

    // ── Seat states ───────────────────────────────────────────
    private static final int AVAILABLE = 0;
    private static final int TAKEN_X   = 1;   // red × (booked by others)
    private static final int TAKEN_BOX = 2;   // dark brown box (booked, no X)
    private static final int SELECTED  = 3;   // green

    // state[row][col]  row 0‑5 → A B D F G H   col 0‑17 → 1‑18
    private int[][] state = new int[ROWS.length][COLS];

    // map "A7" → [rowIdx, colIdx]
    private Map<String, int[]> seatIndex = new HashMap<>();

    // Callback when confirmed
    private SeatCallback callback;

    // Selected seats list
    private List<String> selectedSeats = new ArrayList<>();

    // Harga per kursi (set from outside)
    private int hargaSatuan = 75_000;

    // Stat labels updated live
    private JTextArea txtKursiResult;
    private JLabel    lblTotal;

    public interface SeatCallback {
        void onConfirmed(List<String> seats);
    }

    // ─────────────────────────────────────────────────────────
    public SeatSelectionPanel(SeatCallback callback) {
        this.callback = callback;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(42, 42, 42));

        initDefaultTaken();
        buildUI();
    }

    public SeatSelectionPanel(Object callback2) {
        //TODO Auto-generated constructor stub
    }

    /** Call this before showing the panel to set the ticket price. */
    public void setHargaSatuan(int harga) {
        this.hargaSatuan = harga;
        refreshTotal();
    }

    // ─────────────────────────────────────────────────────────
    // DEFAULT TAKEN SEATS (matching the screenshot)
    // ─────────────────────────────────────────────────────────
    private void initDefaultTaken() {
        // TAKEN_X  → red ×
        String[] takenX = {
            "A7","A8","A9",  "A11","A12","A13",
            "B7","B8",       "B11","B12","B13"
        };
        // TAKEN_BOX → dark seat (no ×), aisle‑side gaps already handled by aisle
        // From the screenshot rows D col 9 area appears as gap (aisle), rest available.
        // We'll just mark the X ones; everything else starts AVAILABLE.
        for (String s : takenX) {
            int[] idx = seatKey(s);
            if (idx != null) state[idx[0]][idx[1]] = TAKEN_X;
        }
    }

    // ─────────────────────────────────────────────────────────
    // BUILD UI
    // ─────────────────────────────────────────────────────────
    private void buildUI() {
        // Outer border box (olive/dark border like screenshot)
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setBackground(new Color(42, 42, 42));
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(130, 110, 40), 2),
                new EmptyBorder(10, 10, 10, 10)));

        // Title
        JLabel title = new JLabel("Pemilihan Kursi", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        wrapper.add(title, BorderLayout.NORTH);

        // Center: screen + seat grid + legend
        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);

        // SCREEN bar
        JLabel screen = new JLabel("SCREEN", SwingConstants.CENTER);
        screen.setOpaque(true);
        screen.setBackground(new Color(120, 15, 15));
        screen.setForeground(Color.WHITE);
        screen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        screen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 160, 0), 2),
                new EmptyBorder(5, 0, 5, 0)));
        center.add(screen, BorderLayout.NORTH);

        // Seat grid
        center.add(buildSeatGrid(), BorderLayout.CENTER);

        // Legend
        center.add(buildLegend(), BorderLayout.SOUTH);

        wrapper.add(center, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────
    // SEAT GRID
    // ─────────────────────────────────────────────────────────
    private JPanel buildSeatGrid() {
        // Rows: label | seats 1-9 | gap | seats 10-18 | label
        // Header row: blank | 1..9 | gap | 10..18 | blank

        int totalCols = 1 + AISLE_AFTER + 1 + (COLS - AISLE_AFTER) + 1; // label+9+aisle+9+label
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill   = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.CENTER;
        g.insets = new Insets(BTN_GAP / 2, BTN_GAP / 2, BTN_GAP / 2, BTN_GAP / 2);

        // ── Column number header ──────────────────────────────
        g.gridy = 0;

        // left label placeholder
        g.gridx = 0;
        grid.add(new JLabel(""), g);

        // cols 1‑9
        for (int c = 1; c <= AISLE_AFTER; c++) {
            g.gridx = c;
            grid.add(colNumLabel(c), g);
        }

        // aisle placeholder
        g.gridx = AISLE_AFTER + 1;
        grid.add(new JLabel(""), g);

        // cols 10‑18
        for (int c = AISLE_AFTER + 1; c <= COLS; c++) {
            g.gridx = c + 1; // +1 for aisle col
            grid.add(colNumLabel(c), g);
        }

        // right label placeholder
        g.gridx = COLS + 2;
        grid.add(new JLabel(""), g);

        // ── Seat rows ─────────────────────────────────────────
        for (int r = 0; r < ROWS.length; r++) {
            final int ri = r;
            g.gridy = r + 1;

            // left row letter
            g.gridx = 0;
            grid.add(rowLetterLabel(ROWS[r]), g);

            // seats 1‑9
            for (int c = 0; c < AISLE_AFTER; c++) {
                final int ci = c;
                String key = "" + ROWS[r] + (c + 1);
                seatIndex.put(key, new int[]{r, c});
                JButton btn = makeSeatButton(r, c);
                btn.addActionListener(e -> toggleSeat(ri, ci, btn));
                g.gridx = c + 1;
                grid.add(btn, g);
            }

            // aisle gap
            g.gridx = AISLE_AFTER + 1;
            JLabel aisle = new JLabel(" ");
            aisle.setPreferredSize(new Dimension(12, BTN_SIZE));
            grid.add(aisle, g);

            // seats 10‑18
            for (int c = AISLE_AFTER; c < COLS; c++) {
                final int ci = c;
                String key = "" + ROWS[r] + (c + 1);
                seatIndex.put(key, new int[]{r, c});
                JButton btn = makeSeatButton(r, c);
                btn.addActionListener(e -> toggleSeat(ri, ci, btn));
                g.gridx = c + 2; // +2 = 1 (label) + 1 (aisle)
                grid.add(btn, g);
            }

            // right row letter
            g.gridx = COLS + 2;
            grid.add(rowLetterLabel(ROWS[r]), g);
        }

        return grid;
    }

    private JButton makeSeatButton(int r, int c) {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(BTN_SIZE, BTN_SIZE));
        btn.setMinimumSize(new Dimension(BTN_SIZE, BTN_SIZE));
        btn.setMaximumSize(new Dimension(BTN_SIZE, BTN_SIZE));
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        applySeatStyle(btn, state[r][c]);
        return btn;
    }

    // Seat colours / icons
    private void applySeatStyle(JButton btn, int seatState) {
        switch (seatState) {
            case TAKEN_X:
                btn.setBackground(new Color(120, 30, 30));
                btn.setForeground(new Color(255, 80, 80));
                btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btn.setText("✕");
                btn.setBorder(BorderFactory.createLineBorder(new Color(160, 50, 50), 1));
                btn.setEnabled(false);
                break;
            case TAKEN_BOX:
                btn.setBackground(new Color(90, 60, 30));
                btn.setForeground(new Color(90, 60, 30));
                btn.setText("");
                btn.setBorder(BorderFactory.createLineBorder(new Color(120, 80, 30), 1));
                btn.setEnabled(false);
                break;
            case SELECTED:
                btn.setBackground(new Color(40, 160, 60));
                btn.setForeground(Color.WHITE);
                btn.setText("");
                btn.setBorder(BorderFactory.createLineBorder(new Color(60, 200, 80), 1));
                btn.setEnabled(true);
                break;
            default: // AVAILABLE
                btn.setBackground(new Color(160, 130, 70));
                btn.setForeground(new Color(160, 130, 70));
                btn.setText("");
                btn.setBorder(BorderFactory.createLineBorder(new Color(190, 155, 80), 1));
                btn.setEnabled(true);
                break;
        }
    }

    // Toggle available ↔ selected
    private void toggleSeat(int r, int c, JButton btn) {
        if (state[r][c] == AVAILABLE) {
            state[r][c] = SELECTED;
            String key = "" + ROWS[r] + (c + 1);
            selectedSeats.add(key);
        } else if (state[r][c] == SELECTED) {
            state[r][c] = AVAILABLE;
            String key = "" + ROWS[r] + (c + 1);
            selectedSeats.remove(key);
        }
        applySeatStyle(btn, state[r][c]);
        refreshTotal();
    }

    // ─────────────────────────────────────────────────────────
    // LEGEND
    // ─────────────────────────────────────────────────────────
    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 2));
        legend.setOpaque(false);

        legend.add(legendItem(new Color(160, 130, 70), null,   "Tersedia"));
        legend.add(legendItem(new Color(40, 160, 60), null,    "Selected"));
        legend.add(legendItem(new Color(120, 30, 30), "✕",     "Taken"));

        return legend;
    }

    private JPanel legendItem(Color color, String icon, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);

        JLabel box = new JLabel(icon != null ? icon : "  ");
        box.setOpaque(true);
        box.setBackground(color);
        box.setForeground(icon != null ? new Color(255, 80, 80) : color);
        box.setFont(new Font("Segoe UI", Font.BOLD, 10));
        box.setPreferredSize(new Dimension(16, 16));
        box.setHorizontalAlignment(SwingConstants.CENTER);
        box.setBorder(BorderFactory.createLineBorder(color.darker(), 1));

        JLabel txt = new JLabel(label);
        txt.setForeground(Color.WHITE);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        p.add(box);
        p.add(txt);
        return p;
    }

    // ─────────────────────────────────────────────────────────
    // HEADER / COL LABELS
    // ─────────────────────────────────────────────────────────
    private JLabel colNumLabel(int n) {
        JLabel l = new JLabel(String.valueOf(n), SwingConstants.CENTER);
        l.setForeground(new Color(180, 180, 180));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l.setPreferredSize(new Dimension(BTN_SIZE, 14));
        return l;
    }

    private JLabel rowLetterLabel(char ch) {
        JLabel l = new JLabel(String.valueOf(ch), SwingConstants.CENTER);
        l.setForeground(new Color(180, 180, 180));
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setPreferredSize(new Dimension(16, BTN_SIZE));
        return l;
    }

    // ─────────────────────────────────────────────────────────
    // TOTAL REFRESH
    // ─────────────────────────────────────────────────────────
    private void refreshTotal() {
        // Notify parent via callback so UserDashboard can update txtKursi & txtTotal
        if (callback != null) callback.onConfirmed(new ArrayList<>(selectedSeats));
    }

    // ─────────────────────────────────────────────────────────
    // SEAT KEY HELPER
    // ─────────────────────────────────────────────────────────
    private int[] seatKey(String label) {
        // e.g. "A7"  → row index of 'A', col index 6 (0-based)
        char rowChar = label.charAt(0);
        int  colNum  = Integer.parseInt(label.substring(1)) - 1; // 0-based
        for (int i = 0; i < ROWS.length; i++) {
            if (ROWS[i] == rowChar) return new int[]{i, colNum};
        }
        return null;
    }

    /** Return a snapshot of currently selected seats. */
    public List<String> getSelectedSeats() {
        return Collections.unmodifiableList(selectedSeats);
    }

    /** Clear all selections (called on Reset). */
    public void resetSelections() {
        for (int r = 0; r < ROWS.length; r++) {
            for (int c = 0; c < COLS; c++) {
                if (state[r][c] == SELECTED) state[r][c] = AVAILABLE;
            }
        }
        selectedSeats.clear();
        // Rebuild UI
        removeAll();
        buildUI();
        revalidate();
        repaint();
        refreshTotal();
    }
}