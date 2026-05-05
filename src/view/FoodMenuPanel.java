package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.*;

public class FoodMenuPanel extends JPanel {

    // ── Callback ───────────────────────────────────────────────────────────────
    public interface FoodCallback {
        /** Called whenever any item quantity changes. */
        void onOrderChanged(java.util.List<FoodItem> orderedItems, long foodTotal);
    }

    // ── Data model ─────────────────────────────────────────────────────────────
    public static class FoodItem {
        public final String category;
        public final String name;
        public final long   price;
        public final String imagePath;   // path to image file in assets folder
        int quantity = 0;

        FoodItem(String cat, String name, long price, String imagePath) {
            this.category = cat; this.name = name; this.price = price; this.imagePath = imagePath;
        }
        public String getDisplayName() { return name; }
    }

    // ── Master item list ───────────────────────────────────────────────────────
    private static final java.util.List<FoodItem> ALL_ITEMS = new ArrayList<>();
    static {
        ALL_ITEMS.add(new FoodItem("POPCORN",    "Popcorn Caramel\nLarge",  35_000, "assets/popcorn_caramel.png"));
        ALL_ITEMS.add(new FoodItem("POPCORN",    "Popcorn Regular",         25_000, "assets/popcorn_regular.png"));
        ALL_ITEMS.add(new FoodItem("POPCORN",    "Popcorn Butter",          28_000, "assets/popcorn_butter.png"));
        ALL_ITEMS.add(new FoodItem("MINUMAN",    "Soda Jumbo",              20_000, "assets/soda_jumbo.png"));
        ALL_ITEMS.add(new FoodItem("MINUMAN",    "Air Mineral Botol",       10_000, "assets/aqua_botol.png"));
        ALL_ITEMS.add(new FoodItem("MINUMAN",    "Jus Jeruk",               18_000, "assets/jus_jeruk.png"));
        ALL_ITEMS.add(new FoodItem("PAKET HEMAT","Paket Couple",            95_000, "assets/paket_couple.png"));
        ALL_ITEMS.add(new FoodItem("PAKET HEMAT","Paket Family",            175_000, "assets/paket_family.jpg"));
    }

    // ── State ──────────────────────────────────────────────────────────────────
    private String      activeCategory = "SEMUA";
    private FoodCallback callback;

    // keep item quantities in sync across category switches
    private final Map<String, Integer> quantities = new LinkedHashMap<>();

    // card panel for the item grid
    private JPanel itemGridPanel;

    // ── Colours ────────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(42, 42, 42);
    private static final Color CARD_BG     = new Color(80, 18, 18);
    private static final Color CARD_BD     = new Color(130, 90, 20);
    private static final Color TAB_ACTIVE  = new Color(200, 155, 0);
    private static final Color TAB_INACT   = new Color(60, 60, 60);
    private static final Color QTY_BG      = new Color(160, 115, 0);
    private static final Color QTY_BTN_BG  = new Color(120, 85, 0);

    // ─────────────────────────────────────────────────────────────────────────
    public FoodMenuPanel(FoodCallback callback) {
        this.callback = callback;
        // init all quantities to 0
        for (FoodItem item : ALL_ITEMS) quantities.put(item.name, 0);

        setLayout(new BorderLayout(0, 8));
        setBackground(BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BD, 2),
                new EmptyBorder(10, 10, 10, 10)));

        add(buildTabRow(),   BorderLayout.NORTH);

        itemGridPanel = new JPanel();
        itemGridPanel.setBackground(BG);
        JScrollPane scroll = new JScrollPane(itemGridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(10);

        add(scroll, BorderLayout.CENTER);

        refreshGrid();
    }

    // ── Category tab row ───────────────────────────────────────────────────────
    private JPanel buildTabRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setBackground(BG);

        String[] cats = {"SEMUA", "POPCORN", "MINUMAN", "PAKET HEMAT"};
        for (String cat : cats) {
            JButton tab = createTabBtn(cat);
            tab.addActionListener(e -> {
                activeCategory = cat;
                refreshGrid();
                // re-style all tabs
                for (Component c : row.getComponents()) {
                    if (c instanceof JButton) {
                        JButton b = (JButton) c;
                        boolean active = b.getText().equals(cat);
                        b.setBackground(active ? TAB_ACTIVE : TAB_INACT);
                        b.setForeground(active ? Color.BLACK : Color.WHITE);
                    }
                }
            });
            row.add(tab);
        }
        return row;
    }

    private JButton createTabBtn(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        boolean active = label.equals(activeCategory);
        btn.setBackground(active ? TAB_ACTIVE : TAB_INACT);
        btn.setForeground(active ? Color.BLACK : Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        // rounded corners via painting
        btn.setOpaque(true);
        return btn;
    }

    // ── Refresh item grid based on active category ─────────────────────────────
    private void refreshGrid() {
        itemGridPanel.removeAll();

        java.util.List<FoodItem> visible = new ArrayList<>();
        for (FoodItem item : ALL_ITEMS) {
            if (activeCategory.equals("SEMUA") || item.category.equals(activeCategory)) {
                visible.add(item);
            }
        }

        // Show up to 3 items per row; use GridLayout
        int cols = Math.min(3, visible.size());
        if (cols == 0) cols = 1;
        itemGridPanel.setLayout(new GridLayout(0, 3, 8, 8));

        for (FoodItem item : visible) {
            itemGridPanel.add(buildItemCard(item));
        }

        itemGridPanel.revalidate();
        itemGridPanel.repaint();
    }

    // ── Single food item card ──────────────────────────────────────────────────
    private JPanel buildItemCard(FoodItem item) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BD, 2),
                new EmptyBorder(10, 8, 8, 8)));

        // ── Item name ─────────────────────────────────────────
        String nameHtml = "<html><center><b>"
                + item.name.replace("\n", "<br>") + "</b></center></html>";
        JLabel lblName = new JLabel(nameHtml, SwingConstants.CENTER);
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        card.add(lblName, BorderLayout.NORTH);

        // ── Image ─────────────────────────────────────
        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(0, 80));
        lblImage.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));

        // Load image from assets folder
        ImageIcon imageIcon = loadFoodImage(item.imagePath, 80, 80);
        if (imageIcon != null) {
            lblImage.setIcon(imageIcon);
        } else {
            // Fallback to text if image not found
            lblImage.setText("No Image");
            lblImage.setForeground(Color.WHITE);
            lblImage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }

        card.add(lblImage, BorderLayout.CENTER);

        // ── Price + qty control ───────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 4));
        bottom.setBackground(CARD_BG);

        JLabel lblPrice = new JLabel(formatRp(item.price), SwingConstants.CENTER);
        lblPrice.setForeground(new Color(255, 200, 0));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bottom.add(lblPrice, BorderLayout.NORTH);

        // qty row: [–] [count] [+]
        int initQty = quantities.getOrDefault(item.name, 0);
        JLabel lblQty = new JLabel(String.valueOf(initQty), SwingConstants.CENTER);
        lblQty.setForeground(Color.WHITE);
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblQty.setPreferredSize(new Dimension(30, 24));

        JButton btnMinus = createQtyBtn("-");
        JButton btnPlus  = createQtyBtn("+");

        btnMinus.addActionListener(e -> {
            int q = quantities.getOrDefault(item.name, 0);
            if (q > 0) {
                q--;
                quantities.put(item.name, q);
                lblQty.setText(String.valueOf(q));
                fireCallback();
            }
        });
        btnPlus.addActionListener(e -> {
            int q = quantities.getOrDefault(item.name, 0) + 1;
            quantities.put(item.name, q);
            lblQty.setText(String.valueOf(q));
            fireCallback();
        });

        JPanel qtyRow = new JPanel(new BorderLayout(4, 0));
        qtyRow.setBackground(QTY_BG);
        qtyRow.setBorder(new EmptyBorder(3, 4, 3, 4));
        qtyRow.setPreferredSize(new Dimension(0, 36));
        qtyRow.add(btnMinus, BorderLayout.WEST);
        qtyRow.add(lblQty,   BorderLayout.CENTER);
        qtyRow.add(btnPlus,  BorderLayout.EAST);

        bottom.add(qtyRow, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private JButton createQtyBtn(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(QTY_BTN_BG);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setPreferredSize(new Dimension(45, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(QTY_BTN_BG.brighter()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(QTY_BTN_BG); }
        });
        return btn;
    }

    // ── Callback fire ──────────────────────────────────────────────────────────
    private void fireCallback() {
        if (callback == null) return;
        java.util.List<FoodItem> ordered = new ArrayList<>();
        long total = 0;
        for (FoodItem item : ALL_ITEMS) {
            int q = quantities.getOrDefault(item.name, 0);
            if (q > 0) {
                item.quantity = q;
                ordered.add(item);
                total += item.price * q;
            }
        }
        callback.onOrderChanged(ordered, total);
    }

    // ── Public API ─────────────────────────────────────────────────────────────
    /** Returns summary string for detail field, e.g. "Popcorn Caramel Large x2, Soda Jumbo x1" */
    public String getOrderSummary() {
        StringBuilder sb = new StringBuilder();
        for (FoodItem item : ALL_ITEMS) {
            int q = quantities.getOrDefault(item.name, 0);
            if (q > 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(item.name.replace("\n", " ")).append(" x").append(q);
            }
        }
        return sb.toString();
    }

    public long getFoodTotal() {
        long total = 0;
        for (FoodItem item : ALL_ITEMS) {
            int q = quantities.getOrDefault(item.name, 0);
            total += item.price * q;
        }
        return total;
    }

    /** Reset all quantities to 0. */
    public void resetOrder() {
        for (String key : quantities.keySet()) quantities.put(key, 0);
        for (FoodItem item : ALL_ITEMS) item.quantity = 0;
        refreshGrid();
        fireCallback();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private String formatRp(long amount) {
        return "Rp " + String.format("%,d", amount).replace(',', '.');
    }

    private ImageIcon loadFoodImage(String imagePath, int width, int height) {
        try {
            // Try to load from assets folder first
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                BufferedImage img = ImageIO.read(imageFile);
                Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }

            // Try to load as resource from classpath
            URL resourceUrl = getClass().getResource("/" + imagePath);
            if (resourceUrl != null) {
                BufferedImage img = ImageIO.read(resourceUrl);
                Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }

            // Try relative to src/assets
            File relativeFile = new File("src/" + imagePath);
            if (relativeFile.exists()) {
                BufferedImage img = ImageIO.read(relativeFile);
                Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }

        } catch (Exception e) {
            // Image loading failed, will use fallback
            System.err.println("Failed to load image: " + imagePath);
        }
        return null;
    }
}