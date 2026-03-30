public class Film {
    private String judul;
    private String genre;
    private int harga;

    public Film(String judul, String genre, int harga) {
        this.judul = judul;
        this.genre = genre;
        this.harga = harga;
    }

    public String getJudul() {
        return judul;
    }

    public String getGenre() {
        return genre;
    }

    public int getHarga() {
        return harga;
    }
}