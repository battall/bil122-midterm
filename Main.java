import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import javax.mail.*;
import javax.mail.internet.*;

class Main {
  public static UyeListesi uyeListesi = new UyeListesi("uyeler.txt");
  public static MailGonderici mailGonderici = new MailGonderici();

  public static void main(String[] args) {

    Scanner scanner = new Scanner(System.in);

    // Ana menüyü göster
    int choice;
    do {
      System.out.println("Lütfen bir seçim yapınız:");
      System.out.println("1- Elit üye ekleme");
      System.out.println("2- Genel Üye ekleme");
      System.out.println("3- Mail Gönderme");
      System.out.println("0- Çıkış");
      choice = scanner.nextInt();

      // seçeneğe göre üye belirle
      switch (choice) {
        case 1:
        case 2:
          // kullanıcı seçiminie göre üye ekliyoruz
          String uyeTipi = choice == 1 ? "elit" : "genel";
          // üyenin ismini soruyoruz
          System.out.println("İsim Giriniz:");
          String isim = scanner.next();
          // üyenin soyismini soruyoruz
          System.out.println("Soyisim Giriniz:");
          String soyisim = scanner.next();
          // üyenin emailini soruyoruz
          System.out.println("Email Giriniz:");
          String email = scanner.next();
          // uyeyi uye listesine ekliyoruz
          uyeListesi.uyeEkle(new Uye(isim, soyisim, email, uyeTipi));
          break;
        case 3:
          System.out.println("Lütfen bir seçim yapınız:");
          System.out.println("1- Elit üyelere mail");
          System.out.println("2- Genel üyelere mail");
          System.out.println("3- Tüm üyelere mail");
          // kullanıcı seçimine göre mail adreslerini filtreliyoruz
          int mailGonderilecekUyeler = scanner.nextInt();
          String mailAdresleri = "";
          for (Uye uye : uyeListesi.getUyeList()) {
            switch (mailGonderilecekUyeler) {
              case 1:
                if (uye.getUyeTipi().equals("elit")) {
                  mailAdresleri += uye.getEmail() + ", ";
                }
                break;
              case 2:
                if (uye.getUyeTipi().equals("genel")) {
                  mailAdresleri += uye.getEmail() + ", ";
                }
                break;
              case 3:
                mailAdresleri += uye.getEmail() + ", ";
                break;
            }
          }
          // son virgülü siliyoruz
          mailAdresleri = mailAdresleri.substring(0, mailAdresleri.length() - 2);

          // kullanıcıyı bilgilendiriyoruz
          System.out.println("Eposta Gönderilecek mail adresleri: "+ mailAdresleri);
          // mail
          try {
            // Gerekli mail bilgilerini al
            System.out.println("Gönderen e-posta adresinizi girin: ");
            String username = scanner.next();
            System.out.println("Şifrenizi girin: ");
            String password = scanner.next();
            System.out.println("E-posta konusunu girin: ");
            String subject = scanner.next();
            System.out.println("E-posta içeriğini girin: ");
            String body = scanner.next();

            // Mail gönderici nesnesini oluştur
            mailGonderici.mailGonder(username, password, mailAdresleri, subject, body);
            System.out.println("Mail gönderildi.");
          } catch (Exception e) {
            System.out.println("Mail gönderirken hata oluştu: " + e.getMessage());
          }
          break;
        case 0:
          System.out.println("Çıkış yapılıyor...");
          break;
        default:
          System.out.println("Geçersiz seçim! Lütfen tekrar deneyin.");
      }
    } while (choice != 0);

    scanner.close();
  }
}

class MailGonderici {
  public static void mailGonder(final String username, final String password, String recipient, String subject,
      String body)
      throws MessagingException {

    // Gerekli mail ayarlarını yap
    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", "smtp-mail.outlook.com");
    props.put("mail.smtp.port", "587");

    // Mail gönderici hesabını tanımla
    Session session = Session.getInstance(props,
        new javax.mail.Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
          }
        });

    // Mail mesajını oluştur
    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress(username));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
    message.setSubject(subject);
    message.setText(body);

    // Maili gönder
    Transport.send(message);
  }
}

class Uye {
  private String isim;
  private String soyisim;
  private String email;
  private String uyeTipi;

  public Uye(String isim, String soyisim, String email, String uyeTipi) {
    this.isim = isim;
    this.soyisim = soyisim;
    this.email = email;
    this.uyeTipi = uyeTipi;
  }

  // Getters and setters

  public String getIsim() {
    return isim;
  }

  public void setIsim(String isim) {
    this.isim = isim;
  }

  public String getSoyisim() {
    return soyisim;
  }

  public void setSoyisim(String soyisim) {
    this.soyisim = soyisim;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUyeTipi() {
    return uyeTipi;
  }

  public void setUyeTipi(String uyeTipi) {
    this.uyeTipi = uyeTipi;
  }
}

class UyeListesi {
  private List<Uye> uyeList;

  public UyeListesi(String dosyaAdi) {
    uyeList = new ArrayList<>();
    // dosyadan kayıtları oku
    try {
      // dosyayı açıp okumak için BufferedReader sınıfı oluşturup FileReader sınıfını
      // argüman olarak koyuyoruz
      BufferedReader br = new BufferedReader(new FileReader(dosyaAdi));
      // line değişkenini burada tanımlıyoruz
      String line;
      // uyeTipini # kısmında geldiğinde değiştirip bu değişkende tutuyoruz
      String uyeTipi = "";
      // dosyaları satır satır okuyoruz
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) {
          continue; // boş satırları geçç
        }
        if (line.startsWith("#")) {
          if (line.equals("#ELİT ÜYELER")) {
            uyeTipi = "elit";
          }
          if (line.equals("#GENEL ÜYELER")) {
            uyeTipi = "genel";
          }
        } else {
          String[] parts = line.split("\t");
          Uye uye = new Uye(parts[0], parts[1], parts[2], uyeTipi);
          uyeList.add(uye);
        }
      }
    } catch (Exception e) {
      System.out.println("HATA: Dosya Okurken: " + e);
    }
  }

  // dosyaya uye ekle ve dosyaya yazıyoruz
  public void uyeEkle(Uye eklenecekUye) {
    uyeList.add(eklenecekUye);

    // text değişkeni tutuyoruz dosyaya yazılacak kısım tutuyoruz
    String text = "";
    text += "#ELİT ÜYELER\n";
    for (Uye uye : uyeList) {
      if (uye.getUyeTipi().equals("elit")) {
        text += uye.getIsim() + "\t" + uye.getSoyisim() + "\t" + uye.getEmail() + "\n";
      }
    }
    text += "\n#GENEL ÜYELER\n";
    for (Uye uye : uyeList) {
      if (uye.getUyeTipi().equals("genel")) {
        text += uye.getIsim() + "\t" + uye.getSoyisim() + "\t" + uye.getEmail() + "\n";
      }
    }

    // dosyayı yazıyoruz
    Path path = Paths.get("uyeler.txt");
    try {
      Files.write(path, text.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      System.err.println("Dosyayı yazarken hata " + e.getMessage());
    }
  }

  public List<Uye> getUyeList() {
    return uyeList;
  }
}