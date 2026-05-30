import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Final {
    private static final String VERI_DOSYASI = "ogrenciler.dat";

    public static void main(String[] args) {
        System.out.println("=== SINAV OTURMA DUZENI SISTEMI BAŞLADI ===");

        veritabaniniHazirla();

        List<Ogrenci> ogrenciListesi = veritabanindanOgrencileriCek();

        int satirlar = 4;
        int sutunlar = 3;

        if (!ogrenciListesi.isEmpty()) {
            oturmaPlaniOlustur(ogrenciListesi, satirlar, sutunlar);
        } else {
            System.out.println("HATA: Ogrenci verisi bulunamadi!");
        }
    }

    private static void veritabaniniHazirla() {
        File dosya = new File(VERI_DOSYASI);

        if (!dosya.exists()) {
            List<Ogrenci> ilkOgrenciler = new ArrayList<>();
            ilkOgrenciler.add(new Ogrenci(1L, "Samet Gencel", "Yazilim"));
            ilkOgrenciler.add(new Ogrenci(2L, "Emre Belezoğlu", "Yazilim"));
            ilkOgrenciler.add(new Ogrenci(3L, "Ahmet Faruk Ürgen", "Yazilim"));
            ilkOgrenciler.add(new Ogrenci(4L, "Elif Yağmur", "Donanim"));
            ilkOgrenciler.add(new Ogrenci(5L, "Fatma Sever", "Donanim"));
            ilkOgrenciler.add(new Ogrenci(6L, "Selin Kara", "Siber"));
            ilkOgrenciler.add(new Ogrenci(7L, "Burak Sen", "Siber"));

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dosya))) {
                oos.writeObject(ilkOgrenciler);
            } catch (IOException e) {
                System.out.println("Hata: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Ogrenci> veritabanindanOgrencileriCek() {
        List<Ogrenci> liste = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(VERI_DOSYASI))) {
            liste = (List<Ogrenci>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Hata: " + e.getMessage());
        }
        return liste;
    }

    public static void oturmaPlaniOlustur(List<Ogrenci> list, int satir, int sutun) {
        Ogrenci[][] sinifMatrisi = new Ogrenci[satir][sutun];
        List<Ogrenci> yerlesecekler = new ArrayList<>(list);
        Collections.shuffle(yerlesecekler);

        for (int i = 0; i < satir; i++) {
            for (int j = 0; j < sutun; j++) {
                if (yerlesecekler.isEmpty()) break;

                boolean uygunBulundu = false;
                for (int k = 0; k < yerlesecekler.size(); k++) {
                    Ogrenci aday = yerlesecekler.get(k);
                    boolean kopyaRiski = false;

                    if (j > 0 && sinifMatrisi[i][j - 1] != null && sinifMatrisi[i][j - 1].getBolum().equalsIgnoreCase(aday.getBolum())) {
                        kopyaRiski = true;
                    }
                    if (i > 0 && sinifMatrisi[i - 1][j] != null && sinifMatrisi[i - 1][j].getBolum().equalsIgnoreCase(aday.getBolum())) {
                        kopyaRiski = true;
                    }

                    if (!kopyaRiski) {
                        sinifMatrisi[i][j] = aday;
                        yerlesecekler.remove(k);
                        uygunBulundu = true;
                        break;
                    }
                }

                if (!uygunBulundu && !yerlesecekler.isEmpty()) {
                    sinifMatrisi[i][j] = yerlesecekler.remove(0);
                }
            }
        }

        System.out.println("\n====================== OLUŞTURULAN SINAV PLANI ======================");
        for (int i = 0; i < satir; i++) {
            System.out.print((i + 1) + ". Sira -> \t");
            for (int j = 0; j < sutun; j++) {
                Ogrenci o = sinifMatrisi[i][j];
                if (o != null) {
                    System.out.printf("[%s (%s)]\t", o.getAdSoyad(), o.getBolum());
                } else {
                    System.out.print("[BOS KOLTUK]\t");
                }
            }
            System.out.println();
        }
        System.out.println("=====================================================================");
    }
}

class Ogrenci implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String adSoyad;
    private String bolum;

    public Ogrenci(Long id, String adSoyad, String bolum) {
        this.id = id;
        this.adSoyad = adSoyad;
        this.bolum = bolum;
    }

    public Long getId() { return id; }
    public String getAdSoyad() { return adSoyad; }
    public String getBolum() { return bolum; }
}