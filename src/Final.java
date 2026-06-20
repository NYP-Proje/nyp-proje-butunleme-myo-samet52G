import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Final extends JFrame {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SinavSistemi;encrypt=false;integratedSecurity=true;trustServerCertificate=true;";

    private JPanel pnlSinifGrid;
    private KartTasarim lblKapasite, lblYerlesen, lblBosKoltuk;
    private JButton btnPlanla;

    private JTextField txtAdSoyad;
    private JTextField txtBolum;
    private JTextField txtSatirSayisi;
    private JTextField txtAra; // Gelişmiş canlı arama kutusu
    private JButton btnEkle, btnGuncelle, btnSil;
    private JTable tblOgrenciListesi;
    private Long secilenOgrenciId = null;

    private JPanel pnlGiris;
    private JPanel pnlAnaIcerik;

    public Final() {
        setTitle("Sınav Oturma Düzeni ve Yönetim Otomasyonu");
        setSize(1350, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new CardLayout());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // --- GİRİŞ EKRANI ---
        pnlGiris = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(41, 50, 60), getWidth(), getHeight(), new Color(72, 85, 99));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        pnlGiris.setLayout(new GridBagLayout());

        JPanel pnlGirisIcerik = new JPanel();
        pnlGirisIcerik.setLayout(new BoxLayout(pnlGirisIcerik, BoxLayout.Y_AXIS));
        pnlGirisIcerik.setOpaque(false);

        JLabel lblGirisBaslik = new JLabel("SINAV OTURMA DÜZENİ");
        lblGirisBaslik.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblGirisBaslik.setForeground(Color.WHITE);
        lblGirisBaslik.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblGirisAltBaslik = new JLabel("Akıllı Planlama ve Öğrenci Yönetim Sistemi");
        lblGirisAltBaslik.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblGirisAltBaslik.setForeground(new Color(200, 214, 229));
        lblGirisAltBaslik.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnStart = new JButton("Sistemine Giriş Yap") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                super.paintComponent(g);
            }
        };
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnStart.setOpaque(false);
        btnStart.setContentAreaFilled(false);
        btnStart.setBorderPainted(false);
        btnStart.setForeground(Color.WHITE);
        btnStart.setFocusPainted(false);
        btnStart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStart.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnStart.setMaximumSize(new Dimension(240, 45));

        pnlGirisIcerik.add(lblGirisBaslik);
        pnlGirisIcerik.add(Box.createRigidArea(new Dimension(0, 8)));
        pnlGirisIcerik.add(lblGirisAltBaslik);
        pnlGirisIcerik.add(Box.createRigidArea(new Dimension(0, 45)));
        pnlGirisIcerik.add(btnStart);

        pnlGiris.add(pnlGirisIcerik);
        add(pnlGiris, "GirisEkrani");

        // --- ANA PANEL TASARIMI ---
        pnlAnaIcerik = new JPanel(new BorderLayout(20, 20));
        pnlAnaIcerik.setBackground(new Color(245, 246, 250));
        pnlAnaIcerik.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // SOL PANEL: Öğrenci Yönetimi ve Arama
        JPanel pnlSol = new JPanel(new BorderLayout(12, 12));
        pnlSol.setBackground(Color.WHITE);
        pnlSol.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 233, 237), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        pnlSol.setPreferredSize(new Dimension(340, 0));

        JPanel pnlForm = new JPanel(new GridLayout(4, 2, 10, 10));
        pnlForm.setBackground(Color.WHITE);

        JLabel lbl1 = new LabelTasarim("Ad Soyad:");
        txtAdSoyad = new TextAlanTasarim();
        JLabel lbl2 = new LabelTasarim("Bölüm:");
        txtBolum = new TextAlanTasarim();

        btnEkle = new ButonTasarim("Öğrenci Ekle", new Color(46, 204, 113));
        btnGuncelle = new ButonTasarim("Bilgi Güncelle", new Color(52, 152, 219));

        pnlForm.add(lbl1); pnlForm.add(txtAdSoyad);
        pnlForm.add(lbl2); pnlForm.add(txtBolum);
        pnlForm.add(btnEkle); pnlForm.add(btnGuncelle);
        pnlSol.add(pnlForm, BorderLayout.NORTH);

        // Arama Filtreleme Arayüz Grubu
        JPanel pnlAramaGrup = new JPanel(new BorderLayout(5, 5));
        pnlAramaGrup.setBackground(Color.WHITE);
        JPanel pnlAramaSatir = new JPanel(new BorderLayout(8, 0));
        pnlAramaSatir.setBackground(Color.WHITE);
        JLabel lblArama = new LabelTasarim("🔍 Listede Ara:");
        txtAra = new TextAlanTasarim();
        pnlAramaSatir.add(lblArama, BorderLayout.WEST);
        pnlAramaSatir.add(txtAra, BorderLayout.CENTER);
        pnlAramaGrup.add(pnlAramaSatir, BorderLayout.NORTH);

        tblOgrenciListesi = new JTable();
        tblOgrenciListesi.setRowHeight(28);
        tblOgrenciListesi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblOgrenciListesi.setSelectionBackground(new Color(52, 152, 219, 30));
        tblOgrenciListesi.setSelectionForeground(Color.BLACK);
        tblOgrenciListesi.setGridColor(new Color(240, 242, 245));
        JScrollPane jspOgrenciListesi = new JScrollPane(tblOgrenciListesi);
        jspOgrenciListesi.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 237)));
        pnlAramaGrup.add(jspOgrenciListesi, BorderLayout.CENTER);

        pnlSol.add(pnlAramaGrup, BorderLayout.CENTER);

        btnSil = new ButonTasarim("Seçili Öğrenciyi Veritabanından Sil", new Color(231, 76, 60));
        pnlSol.add(btnSil, BorderLayout.SOUTH);

        pnlAnaIcerik.add(pnlSol, BorderLayout.WEST);

        // MERKEZ PANEL: Planlama Kontrolleri ve Sınıf Düzeni Paneli
        JPanel pnlMerkez = new JPanel(new BorderLayout(20, 20));
        pnlMerkez.setBackground(new Color(245, 246, 250));

        JPanel pnlUst = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pnlUst.setBackground(Color.WHITE);
        pnlUst.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 237)));

        JLabel lblSatir = new LabelTasarim("Bir Kolonda Aşağı Doğru Kaç Sıra Olsun?:");
        txtSatirSayisi = new TextAlanTasarim("7");
        txtSatirSayisi.setPreferredSize(new Dimension(60, 30));
        txtSatirSayisi.setHorizontalAlignment(JTextField.CENTER);

        btnPlanla = new ButonTasarim("Sınav Oturma Planını Hazırla", new Color(52, 73, 94));
        btnPlanla.setPreferredSize(new Dimension(220, 35));

        pnlUst.add(lblSatir);
        pnlUst.add(txtSatirSayisi);
        pnlUst.add(btnPlanla);
        pnlMerkez.add(pnlUst, BorderLayout.NORTH);

        pnlSinifGrid = new JPanel(null);
        pnlSinifGrid.setBackground(new Color(245, 246, 250));

        JScrollPane jspDinamikTablo = new JScrollPane(pnlSinifGrid);
        jspDinamikTablo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jspDinamikTablo.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jspDinamikTablo.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 237)));
        jspDinamikTablo.getViewport().setBackground(new Color(245, 246, 250));
        pnlMerkez.add(jspDinamikTablo, BorderLayout.CENTER);

        pnlAnaIcerik.add(pnlMerkez, BorderLayout.CENTER);

        // ALT PANEL: İstatistik Kartları
        JPanel pnlAlt = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlAlt.setBackground(new Color(245, 246, 250));
        pnlAlt.setPreferredSize(new Dimension(0, 85));

        lblKapasite = new KartTasarim("TOPLAM KAPASİTE", "0 Sıra", new Color(52, 152, 219));
        lblYerlesen = new KartTasarim("YERLEŞEN ÖĞRENCİ", "0 Kişi", new Color(46, 204, 113));
        lblBosKoltuk = new KartTasarim("KALAN BOŞ KOLTUK", "0 Koltuk", new Color(241, 196, 15));

        pnlAlt.add(lblKapasite);
        pnlAlt.add(lblYerlesen);
        pnlAlt.add(lblBosKoltuk);
        pnlAnaIcerik.add(pnlAlt, BorderLayout.SOUTH);

        add(pnlAnaIcerik, "AnaPanel");

        // EVENT LISTENERS
        btnStart.addActionListener(e -> {
            CardLayout cl = (CardLayout) getContentPane().getLayout();
            cl.show(getContentPane(), "AnaPanel");
        });

        btnPlanla.addActionListener(e -> planiArayuzeYansit());
        btnEkle.addActionListener(e -> ogrenciEkle());
        btnGuncelle.addActionListener(e -> ogrenciGuncelle());
        btnSil.addActionListener(e -> ogrenciSil());

        // HATA DÜZELTİLDİ: changeUpdate -> changedUpdate yapıldı.
        txtAra.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { canlıFiltrele(); }
            @Override
            public void removeUpdate(DocumentEvent e) { canlıFiltrele(); }
            @Override
            public void changedUpdate(DocumentEvent e) { canlıFiltrele(); }

            private void canlıFiltrele() {
                String arananMetin = txtAra.getText().trim().toLowerCase();
                DefaultTableModel model = (DefaultTableModel) tblOgrenciListesi.getModel();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
                tblOgrenciListesi.setRowSorter(sorter);
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + arananMetin));
            }
        });

        tblOgrenciListesi.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = tblOgrenciListesi.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = tblOgrenciListesi.convertRowIndexToModel(selectedRow);
                secilenOgrenciId = (Long) tblOgrenciListesi.getModel().getValueAt(modelRow, 0);
                txtAdSoyad.setText(tblOgrenciListesi.getModel().getValueAt(modelRow, 1).toString());
                txtBolum.setText(tblOgrenciListesi.getModel().getValueAt(modelRow, 2).toString());
            }
        });

        solListeyiYenile();
    }

    private void solListeyiYenile() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Ad Soyad", "Bölüm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        List<Ogrenci> list = veritabanindanOgrencileriCek();
        for (Ogrenci o : list) {
            model.addRow(new Object[]{o.getId(), o.getAdSoyad(), o.getBolum()});
        }
        tblOgrenciListesi.setModel(model);
        if (tblOgrenciListesi.getColumnCount() > 0) {
            tblOgrenciListesi.getColumnModel().getColumn(0).setPreferredWidth(40);
        }
        txtAdSoyad.setText("");
        txtBolum.setText("");
        if (txtAra != null) txtAra.setText("");
        secilenOgrenciId = null;
    }

    private void ogrenciEkle() {
        String ad = txtAdSoyad.getText().trim();
        String bolum = txtBolum.getText().trim();
        if (ad.isEmpty() || bolum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String query = "INSERT INTO Ogrenciler (adSoyad, bolum) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, ad);
            ps.setString(2, bolum);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Öğrenci başarıyla eklendi.");
            solListeyiYenile();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ekleme Hatası: " + e.getMessage());
        }
    }

    private void ogrenciGuncelle() {
        if (secilenOgrenciId == null) {
            JOptionPane.showMessageDialog(this, "Lütfen listeden güncellenecek öğrenciyi seçin!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ad = txtAdSoyad.getText().trim();
        String bolum = txtBolum.getText().trim();
        String query = "UPDATE Ogrenciler SET adSoyad = ?, bolum = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, ad);
            ps.setString(2, bolum);
            ps.setLong(3, secilenOgrenciId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Öğrenci bilgilerileri güncellendi.");
            solListeyiYenile();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Güncelleme Hatası: " + e.getMessage());
        }
    }

    private void ogrenciSil() {
        if (secilenOgrenciId == null) {
            JOptionPane.showMessageDialog(this, "Lütfen listeden silinecek öğrenciyi seçin!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int onay = JOptionPane.showConfirmDialog(this, "Seçili öğrenciyi silmek istediğinize emin misiniz?", "Silme Onayı", JOptionPane.YES_NO_OPTION);
        if (onay != JOptionPane.YES_OPTION) return;

        String query = "DELETE FROM Ogrenciler WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setLong(1, secilenOgrenciId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Öğrenci veritabanından silindi.");
            solListeyiYenile();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Silme Hatası: " + e.getMessage());
        }
    }

    private void planiArayuzeYansit() {
        List<Ogrenci> ogrenciListesi = veritabanindanOgrencileriCek();

        if (ogrenciListesi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "HATA: Veritabanında öğrenci yok!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int satirlar;
        try {
            satirlar = Integer.parseInt(txtSatirSayisi.getText().trim());
            if (satirlar <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli bir sıra sayısı girin!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int sutunlar = (int) Math.ceil((double) ogrenciListesi.size() / satirlar);
        if (sutunlar < 3) sutunlar = 3;

        Ogrenci[][] sinifMatrisi = dikeyOturmaPlaniOlustur(ogrenciListesi, satirlar, sutunlar);

        pnlSinifGrid.removeAll();

        int sabitKoltukGenisligi = 135;
        int koltukYuksekligi = 55;
        int dikeyBosluk = 15;
        int yatayBosluk = 45;

        int tahtaYuksekligi = 50;
        int baslangicY = 15 + tahtaYuksekligi + 30;

        int toplamPanelGenislik = (sutunlar * (sabitKoltukGenisligi + yatayBosluk)) + 40;
        int toplamPanelYukseklik = baslangicY + (satirlar * (koltukYuksekligi + dikeyBosluk)) + 40;
        pnlSinifGrid.setPreferredSize(new Dimension(toplamPanelGenislik, toplamPanelYukseklik));

        JPanel pnlTahta = new JPanel(new BorderLayout());
        pnlTahta.setBackground(new Color(53, 59, 72));
        pnlTahta.setBorder(BorderFactory.createLineBorder(new Color(47, 53, 66), 2));
        JLabel lblTahta = new JLabel("SINIF TAHTASI / ÖĞRETMEN KÜRSÜSÜ YÖNÜ", SwingConstants.CENTER);
        lblTahta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTahta.setForeground(Color.WHITE);
        pnlTahta.add(lblTahta, BorderLayout.CENTER);

        int tahtaGenislik = Math.min(toplamPanelGenislik - 100, 600);
        pnlTahta.setBounds((toplamPanelGenislik - tahtaGenislik) / 2, 15, tahtaGenislik, tahtaYuksekligi);
        pnlSinifGrid.add(pnlTahta);

        int ogrenciSiraNo = 1;

        for (int j = 0; j < sutunlar; j++) {
            int xKoordinati = 30 + (j * (sabitKoltukGenisligi + yatayBosluk));

            for (int i = 0; i < satirlar; i++) {
                Ogrenci o = sinifMatrisi[i][j];
                int yKoordinati = baslangicY + (i * (koltukYuksekligi + dikeyBosluk));

                JPanel pnlKoltuk = new JPanel(new BorderLayout());
                pnlKoltuk.setBackground(Color.WHITE);
                pnlKoltuk.setBounds(xKoordinati, yKoordinati, sabitKoltukGenisligi, koltukYuksekligi);
                pnlKoltuk.setCursor(new Cursor(Cursor.HAND_CURSOR));

                JLabel lblOgrenciBilgi = new JLabel("", SwingConstants.CENTER);
                lblOgrenciBilgi.setFont(new Font("Segoe UI", Font.PLAIN, 11));

                final int gecerliSiraNo = ogrenciSiraNo;
                final Ogrenci gecerliOgrenci = o;

                if (o != null) {
                    lblOgrenciBilgi.setText("<html><center><b style='color:#2980b9; font-size:12px;'>" + ogrenciSiraNo + "</b><br><b style='color:#2c3e50;'>" + o.getAdSoyad() + "</b><br><span style='color:#95a5a6; font-size:9px;'>" + o.getBolum() + "</span></center></html>");
                    pnlKoltuk.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                            BorderFactory.createEmptyBorder(2, 2, 2, 2)
                    ));
                } else {
                    lblOgrenciBilgi.setText("<html><center><b style='color:#e67e22; font-size:11px;'>" + ogrenciSiraNo + "</b><br><span style='color:#d35400; font-size:10px;'>BOŞ</span></center></html>");
                    pnlKoltuk.setBackground(new Color(253, 242, 233));
                    pnlKoltuk.setBorder(BorderFactory.createLineBorder(new Color(245, 139, 44, 80), 1));
                }

                // Koltuk detaylarını gösteren Mouse Event Listener modülü
                pnlKoltuk.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (gecerliOgrenci != null) {
                            JOptionPane.showMessageDialog(Final.this,
                                    "🪑 Sıra No: " + gecerliSiraNo + "\n" +
                                            "👤 Öğrenci: " + gecerliOgrenci.getAdSoyad() + "\n" +
                                            "🎓 Bölüm: " + gecerliOgrenci.getBolum() + "\n" +
                                            "🆔 Sistem ID: " + gecerliOgrenci.getId(),
                                    "Sıra Detay Bilgisi",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(Final.this,
                                    "🪑 Sıra No: " + gecerliSiraNo + "\n" +
                                            "🔸 Durum: Bu koltuk şu an BOŞ durumda.",
                                    "Sıra Detay Bilgisi",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (gecerliOgrenci != null) {
                            pnlKoltuk.setBackground(new Color(240, 247, 253));
                        } else {
                            pnlKoltuk.setBackground(new Color(254, 237, 222));
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (gecerliOgrenci != null) {
                            pnlKoltuk.setBackground(Color.WHITE);
                        } else {
                            pnlKoltuk.setBackground(new Color(253, 242, 233));
                        }
                    }
                });

                pnlKoltuk.add(lblOgrenciBilgi, BorderLayout.CENTER);
                pnlSinifGrid.add(pnlKoltuk);
                ogrenciSiraNo++;
            }
        }

        int toplamKoltuk = satirlar * sutunlar;
        int bosKoltuk = toplamKoltuk - ogrenciListesi.size();

        lblKapasite.guncelle(toplamKoltuk + " Koltuk");
        lblYerlesen.guncelle(ogrenciListesi.size() + " Öğrenci");
        lblBosKoltuk.guncelle(bosKoltuk + " Boş Yer");

        pnlSinifGrid.revalidate();
        pnlSinifGrid.repaint();
    }

    private List<Ogrenci> veritabanindanOgrencileriCek() {
        List<Ogrenci> liste = new ArrayList<>();
        String query = "SELECT id, adSoyad, bolum FROM Ogrenciler";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Long id = rs.getLong("id");
                String adSoyad = rs.getString("adSoyad");
                String bolum = rs.getString("bolum");
                liste.add(new Ogrenci(id, adSoyad, bolum));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "MSSQL Bağlantı Hatası: " + e.getMessage(), "Veritabanı Hatası", JOptionPane.ERROR_MESSAGE);
        }
        return liste;
    }

    public static Ogrenci[][] dikeyOturmaPlaniOlustur(List<Ogrenci> list, int satir, int sutun) {
        Ogrenci[][] sinifMatrisi = null;
        boolean basariliDuzen = false;
        int maxDeneme = 3000;
        int deneme = 0;

        while (deneme < maxDeneme && !basariliDuzen) {
            sinifMatrisi = new Ogrenci[satir][sutun];
            List<Ogrenci> yerlesecekler = new ArrayList<>(list);
            Collections.shuffle(yerlesecekler);
            basariliDuzen = true;

            for (int j = 0; j < sutun; j++) {
                for (int i = 0; i < satir; i++) {
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
            for (int j = 0; j < sutun; j++) {
                for (int i = 0; i < satir; i++) {
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
        return sinifMatrisi;
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Final ekran = new Final();
            ekran.setVisible(true);
        });
    }
}

class Ogrenci {
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

class ButonTasarim extends JButton {
    private Color anaRenk;
    private Color hoverRenk;
    private boolean isHovered = false;

    public ButonTasarim(String text, Color bg) {
        super(text);
        this.anaRenk = bg;
        this.hoverRenk = new Color(Math.max(bg.getRed() - 25, 0), Math.max(bg.getGreen() - 25, 0), Math.max(bg.getBlue() - 25, 0));

        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isHovered) g2d.setColor(hoverRenk);
        else g2d.setColor(anaRenk);

        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2d.setFont(getFont());
        g2d.drawString(getText(), x, y);
        g2d.dispose();
    }
}

class TextAlanTasarim extends JTextField {
    public TextAlanTasarim() { this(""); }
    public TextAlanTasarim(String text) {
        super(text);
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232), 1),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));
    }
}

class LabelTasarim extends JLabel {
    public LabelTasarim(String text) {
        super(text);
        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setForeground(new Color(72, 84, 96));
    }
}

class KartTasarim extends JPanel {
    private JLabel lblDeger;
    public KartTasarim(String baslik, String deger, Color solRenk) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 233, 237), 1),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));
        JPanel pnlCizgi = new JPanel();
        pnlCizgi.setBackground(solRenk);
        pnlCizgi.setPreferredSize(new Dimension(4, 0));
        add(pnlCizgi, BorderLayout.WEST);

        JPanel pnlIcerik = new JPanel(new GridLayout(2, 1, 2, 2));
        pnlIcerik.setBackground(Color.WHITE);

        JLabel lblBaslik = new JLabel(baslik);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBaslik.setForeground(new Color(136, 142, 152));
        lblDeger = new JLabel(deger);
        lblDeger.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblDeger.setForeground(new Color(47, 53, 66));

        pnlIcerik.add(lblBaslik);
        pnlIcerik.add(lblDeger);
        add(pnlIcerik, BorderLayout.CENTER);
    }
    public void guncelle(String yeniDeger) { lblDeger.setText(yeniDeger); }
}