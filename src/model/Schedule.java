package model;

public class Schedule {
    private int id;
    private int movieId;
    private int studioId;
    private String tanggal;
    private String jam;
    private double harga;

    public Schedule() {}

    public Schedule(int id, int movieId, int studioId, String tanggal, String jam, double harga) {
        this.id = id;
        this.movieId = movieId;
        this.studioId = studioId;
        this.tanggal = tanggal;
        this.jam = jam;
        this.harga = harga;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public int getStudioId() { return studioId; }
    public void setStudioId(int studioId) { this.studioId = studioId; }

    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }

    public String getJam() { return jam; }
    public void setJam(String jam) { this.jam = jam; }

    public double getHarga() { return harga; }
    public void setHarga(double harga) { this.harga = harga; }
}