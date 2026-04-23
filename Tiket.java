public class Tiket {
    private Film film;
    private int jumlah;

    public Tiket(Film film, int jumlah) {
        this.film = film;
        this.jumlah = jumlah;
    }

    public int hitungTotal() {
        return film.getHarga() * jumlah;
    }

    public void tampilkanStruk() {
        System.out.println("=== STRUK PEMESANAN ===");
        System.out.println("Film   : " + film.getJudul());
        System.out.println("Genre  : " + film.getGenre());
        System.out.println("Jumlah : " + jumlah);
        System.out.println("Total  : " + hitungTotal());
    }
}