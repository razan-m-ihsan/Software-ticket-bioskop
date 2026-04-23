package model;

public class Promo {
    private int id;
    private String kodePromo;
    private String deskripsi;
    private double diskonPersen;
    private double diskonRupiah;
    private String tanggalMulai;
    private String tanggalAkhir;
    private boolean aktif;

    public Promo() {}

    public Promo(int id, String kodePromo, String deskripsi, double diskonPersen, double diskonRupiah,
                 String tanggalMulai, String tanggalAkhir, boolean aktif) {
        this.id = id;
        this.kodePromo = kodePromo;
        this.deskripsi = deskripsi;
        this.diskonPersen = diskonPersen;
        this.diskonRupiah = diskonRupiah;
        this.tanggalMulai = tanggalMulai;
        this.tanggalAkhir = tanggalAkhir;
        this.aktif = aktif;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKodePromo() { return kodePromo; }
    public void setKodePromo(String kodePromo) { this.kodePromo = kodePromo; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public double getDiskonPersen() { return diskonPersen; }
    public void setDiskonPersen(double diskonPersen) { this.diskonPersen = diskonPersen; }

    public double getDiskonRupiah() { return diskonRupiah; }
    public void setDiskonRupiah(double diskonRupiah) { this.diskonRupiah = diskonRupiah; }

    public String getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(String tanggalMulai) { this.tanggalMulai = tanggalMulai; }

    public String getTanggalAkhir() { return tanggalAkhir; }
    public void setTanggalAkhir(String tanggalAkhir) { this.tanggalAkhir = tanggalAkhir; }

    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }
}