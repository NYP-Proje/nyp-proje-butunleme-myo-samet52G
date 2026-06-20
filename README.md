# Sinav Oturma Duzeni ve Ogrenci Yonetim Otomasyonu

Bu proje, okullarda veya üniversitelerde sınav salonları için akıllı ve dinamik oturma planları hazırlayan, aynı zamanda MS SQL Server veritabanı destekli tam fonksiyonel bir Ogrenci Yonetim Sistemi (CRUD) sunan masaüstü tabanlı bir Java Swing uygulamasıdır.

Uygulama, kopya çekilmesini engellemek amacıyla aynı bölümden öğrencilerin yan yana (komşu koltuklara) oturmasını otomatik olarak filtreleyen akıllı bir yerleşim algoritmasına sahiptir.

---

## Ozellikler

* Akilli Yerlesim Algoritmasi: Aynı bölüme ait öğrencileri yan yana getirmeyerek sınav güvenliğini maksimuma çıkarır.
* Yatay Matris Duzeni (Sutun Bazli): Kullanıcıdan alınan "Sütun (Sıra) Sayısı" parametresine göre sınıf planını anlık olarak yan yana şekillendirir. Satır doldukça otomatik olarak bir alt satıra geçer.
* Tam CRUD Yonetimi: Öğrenci ekleme, bilgileri güncelleme ve silme işlemlerini MSSQL veritabanı ile senkronize yürütür.
* Istatistik Paneli: Toplam salon kapasitesini, yerleşen öğrenci sayısını ve kalan boş koltukları anlık hesaplar.
* Kullanici Dostu Arayuz: Modern renk paletine sahip, geçişli (CardLayout) giriş ekranı ve taşmaları önleyen kaydırılabilir sınıf şeması içerir.

---

## Kullanilan Teknolojiler

* Dil: Java (JDK 8 veya üzeri)
* Arayuz Kitapligi: Java Swing & AWT
* Veritabani: Microsoft SQL Server (MSSQL)
* Baglanti Surucusu: JDBC (Microsoft JDBC Driver for SQL Server)

---

## Kurulum ve Calistirma

### 1. Veritabanı Yapılandırması
Projenin çalışabilmesi için MSSQL üzerinde SinavSistemi adında bir veritabanı ve içerisinde Ogrenciler tablosu bulunmalıdır. SQL Server Management Studio (SSMS) üzerinde aşağıdaki sorguyu çalıştırarak tabloyu hazır hale getirebilirsiniz:

```sql
CREATE DATABASE SinavSistemi;
GO

USE SinavSistemi;
GO

CREATE TABLE Ogrenciler (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    adSoyad NVARCHAR(100) NOT NULL,
    bolum NVARCHAR(100) NOT NULL
);