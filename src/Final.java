import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Final {
    private static final String VERI_DOSYASI = "ogrenciler.dat";

    public static void main(String[] args) {
        System.out.println("=== SINAV OTURMA DUZENI SISTEMI BASLADI ===");

        veritabaniniHazirla();

        List<Ogrenci> ogrenciListesi = veritabanindanOgrencileriCek();

        // Varsayılan sınıf düzeni (Öğrenci sayısına göre dinamik büyüyecek)
        int sutunlar = 3;
        int satirlar = (int) Math.ceil((double) ogrenciListesi.size() / sutunlar);
        if (satirlar < 3) satirlar = 3; // Minimum 3 satırlık matris

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
            ilkOgrenciler.add(new Ogrenci(2L, "Emre Belozoglu", "Yazilim"));
            ilkOgrenciler.add(new Ogrenci(3L, "Ahmet Faruk Urgen", "Yazilim"));
            ilkOgrenciler.add(new Ogrenci(4L, "Emine Kirici", "Donanim"));
            ilkOgrenciler.add(new Ogrenci(5L, "Fatma Sever", "Donanim"));
            ilkOgrenciler.add(new Ogrenci(6L, "Selin Kara", "Siber"));
            ilkOgrenciler.add(new Ogrenci(7L, "Burak Sen", "Siber"));

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dosya))) {
                oos.writeObject(ilkOgrenciler);
                System.out.println("[Sistem]: Ilk veriler basariyla 'dat' dosyasina yazildi.");
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
        Ogrenci[][] sinifMatrisi = null;
        boolean basariliDuzen = false;
        int maxDeneme = 2000;
        int deneme = 0;

        while (deneme < maxDeneme && !basariliDuzen) {
            sinifMatrisi = new Ogrenci[satir][sutun];
            List<Ogrenci> yerlesecekler = new ArrayList<>(list);
            Collections.shuffle(yerlesecekler);
            basariliDuzen = true;

            for (int i = 0; i < satir; i++) {
                for (int j = 0; j < sutun; j++) {
                    if (yerlesecekler.isEmpty()) break;

                    boolean uygunBulundu = false;
                    for (int k = 0; k < yerlesecekler.size(); k++) {
                        Ogrenci aday = yerlesecekler.get(k);

                        if (konumGuvenliMi(sinifMatrisi, i, j, aday.getBolum())) {
                            sinifMatrisi[i][j] = aday;
                            yerlesecekler.remove(k);
                            uygunBulundu = true;
                            break;
                        }
                    }

                    if (!uygunBulundu) {
                        basariliDuzen = false;
                        break;
                    }
                }
                if (!basariliDuzen) break;
            }
            deneme++;
        }


        if (!basariliDuzen) {
            sinifMatrisi = new Ogrenci[satir][sutun];
            List<Ogrenci> yerlesecekler = new ArrayList<>(list);
            Collections.shuffle(yerlesecekler);

            for (int i = 0; i < satir; i++) {
                for (int j = 0; j < sutun; j++) {
                    if (yerlesecekler.isEmpty()) break;

                    boolean uygunBulundu = false;
                    for (int k = 0; k < yerlesecekler.size(); k++) {
                        Ogrenci aday = yerlesecekler.get(k);
                        if (konumGuvenliMi(sinifMatrisi, i, j, aday.getBolum())) {
                            sinifMatrisi[i][j] = aday;
                            yerlesecekler.remove(k);
                            uygunBulundu = true;
                            break;
                        }
                    }
                    if (!uygunBulundu) {
                        sinifMatrisi[i][j] = yerlesecekler.remove(0);
                    }
                }
            }
        }


        System.out.println("\n================================== OLUSTURULAN SINAV PLANI ==================================");
        for (int i = 0; i < satir; i++) {
            System.out.printf("%d. Sira -> \t", (i + 1));
            for (int j = 0; j < sutun; j++) {
                Ogrenci o = sinifMatrisi[i][j];
                if (o != null) {
                    System.out.printf("[%-20s (%-7s)]\t", o.getAdSoyad(), o.getBolum());
                } else {
                    System.out.printf("[%-20s (%-7s)]\t", "BOS KOLTUK", "---");
                }
            }
            System.out.println();
        }
        System.out.println("=============================================================================================");


        istatistikleriYazdir(sinifMatrisi, list.size());
    }


    private static boolean konumGuvenliMi(Ogrenci[][] matris, int r, int c, String bolum) {
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int nr = r + dr[i];
            int nc = c + dc[i];

            if (nr >= 0 && nr < matris.length && nc >= 0 && nc < matris[0].length) {
                if (matris[nr][nc] != null && matris[nr][nc].getBolum().equalsIgnoreCase(bolum)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void istatistikleriYazdir(Ogrenci[][] matris, int toplamOgrenci) {
        int toplamKoltuk = matris.length * matris[0].length;
        int bosKoltuk = toplamKoltuk - toplamOgrenci;

        System.out.println("\n=== SINAV YERLESIM ISTATISTIKLERI ===");
        System.out.println("-> Toplam Sira/Satir Sayisi : " + matris.length);
        System.out.println("-> Sira Basina Koltuk (Sutun): " + matris[0].length);
        System.out.println("-> Toplam Kapasite           : " + toplamKoltuk);
        System.out.println("-> Yerlesen Ogrenci Sayisi   : " + toplamOgrenci);
        System.out.println("-> Kalan Bos Koltuk Sayisi   : " + bosKoltuk);
        System.out.println("=====================================\n");
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