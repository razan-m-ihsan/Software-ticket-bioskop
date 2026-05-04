package model;

public class Movie {
    private int id;
    private String judul;
    private String sinopsis;
    private int durasi;
    private String genre;
    private String ratingUsia;
    private String posterUrl;

    public Movie() {}

    public Movie(int id, String judul, String sinopsis, int durasi, String genre, String ratingUsia, String posterUrl) {
        this.id = id;
        this.judul = judul;
        this.sinopsis = sinopsis;
        this.durasi = durasi;
        this.genre = genre;
        this.ratingUsia = ratingUsia;
        this.posterUrl = posterUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public String getSinopsis() { return sinopsis; }
    public void setSinopsis(String sinopsis) { this.sinopsis = sinopsis; }

    public int getDurasi() { return durasi; }
    public void setDurasi(int durasi) { this.durasi = durasi; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getRatingUsia() { return ratingUsia; }
    public void setRatingUsia(String ratingUsia) { this.ratingUsia = ratingUsia; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
}