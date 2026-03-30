import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        Film film1 = new Film("Avengers", "Action", 50000);
        Film film2 = new Film("Frozen", "Animation", 40000);

        System.out.println("=== DAFTAR FILM XXI ===");
        System.out.println("1. " + film1.getJudul());
        System.out.println("2. " + film2.getJudul());

        System.out.print("Pilih film (1/2): ");
        int pilih = input.nextInt();

        Film filmDipilih;

        if (pilih == 1) {
            filmDipilih = film1;
        } else {
            filmDipilih = film2;
        }

        System.out.print("Jumlah tiket: ");
        int jumlah = input.nextInt();

        Tiket tiket = new Tiket(filmDipilih, jumlah);
        tiket.tampilkanStruk();
    }
}