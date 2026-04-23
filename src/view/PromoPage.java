package view;

import dao.PromoDAO;
import model.Promo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PromoPage extends JDialog {

    private JList<String> promoList;
    private DefaultListModel<String> listModel;
    private List<Promo> promos;
    private Promo selectedPromo = null;

    public PromoPage(Frame parent) {
        super(parent, "Pilih Promo", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initComponents();
        loadPromos();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(40, 40, 40));

        // Title
        JLabel title = new JLabel("🎟 Promo Tersedia", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainPanel.add(title, BorderLayout.NORTH);

        // Promo List
        listModel = new DefaultListModel<>();
        promoList = new JList<>(listModel);
        promoList.setBackground(new Color(50, 50, 50));
        promoList.setForeground(Color.WHITE);
        promoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        promoList.setFixedCellHeight(60);
        promoList.setCellRenderer(new PromoCellRenderer());

        JScrollPane scrollPane = new JScrollPane(promoList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(40, 40, 40));

        JButton btnPilih = new JButton("Pilih Promo");
        btnPilih.setBackground(new Color(0, 120, 0));
        btnPilih.setForeground(Color.WHITE);
        btnPilih.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = promoList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    selectedPromo = promos.get(selectedIndex);
                    JOptionPane.showMessageDialog(PromoPage.this,
                        "Promo '" + selectedPromo.getKodePromo() + "' dipilih!\n" +
                        "Diskon: " + (selectedPromo.getDiskonPersen() > 0 ?
                            selectedPromo.getDiskonPersen() + "%" :
                            "Rp " + String.format("%,.0f", selectedPromo.getDiskonRupiah())),
                        "Promo Dipilih", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(PromoPage.this,
                        "Silakan pilih promo terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        JButton btnBatal = new JButton("Batal");
        btnBatal.setBackground(new Color(120, 0, 0));
        btnBatal.setForeground(Color.WHITE);
        btnBatal.addActionListener(e -> dispose());

        buttonPanel.add(btnPilih);
        buttonPanel.add(btnBatal);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadPromos() {
        PromoDAO promoDAO = new PromoDAO();
        // For debugging, use getAll() instead of getActivePromos()
        promos = promoDAO.getAll(); // Changed from getActivePromos()

        listModel.clear();
        if (promos.isEmpty()) {
            listModel.addElement("<html><i>Tidak ada promo tersedia</i></html>");
        } else {
            for (Promo promo : promos) {
                String status = promo.isAktif() ? "Aktif" : "Tidak Aktif";
                String display = "<html><b>" + promo.getKodePromo() + "</b> [" + status + "]<br/>" +
                               promo.getDeskripsi() + "<br/>" +
                               "Diskon: " + (promo.getDiskonPersen() > 0 ?
                                   promo.getDiskonPersen() + "%" :
                                   "Rp " + String.format("%,.0f", promo.getDiskonRupiah())) +
                               " | Berlaku: " + promo.getTanggalMulai() + " - " + promo.getTanggalAkhir() +
                               "</html>";
                listModel.addElement(display);
            }
        }
    }

    public Promo getSelectedPromo() {
        return selectedPromo;
    }

    // Custom cell renderer for better display
    private class PromoCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            setText((String) value);
            setBorder(new EmptyBorder(5, 10, 5, 10));

            if (isSelected) {
                setBackground(new Color(0, 100, 200));
            } else {
                setBackground(index % 2 == 0 ? new Color(45, 45, 45) : new Color(50, 50, 50));
            }

            return this;
        }
    }
}