# Canon HDMI Monitor — proyek Android

Aplikasi monitor untuk Canon EOS 600D melalui HDMI → UVC capture card → USB-C OTG Android.

## Fitur yang sudah disiapkan
- Orientasi landscape dan layar penuh imersif.
- Layar tidak tidur saat aplikasi aktif.
- Mendeteksi perangkat USB UVC dan meminta izin USB.
- Preview langsung melalui library UVC.
- Grid komposisi rule-of-thirds.
- Memprioritaskan MJPEG 1280×720 untuk mengurangi beban dan latensi.

## Catatan kompatibilitas
1. Kamera harus mengeluarkan gambar Live View melalui HDMI. Periksa pengaturan HDMI kamera dan gunakan kabel mini-HDMI ke HDMI.
2. Capture card harus benar-benar UVC dan HP harus mendukung USB Host/OTG.
3. Sebagian HP tidak memasok daya yang cukup untuk capture card; gunakan USB-C hub OTG dengan suplai daya jika perlu.
4. Implementasi ini adalah proyek awal yang perlu diuji pada kombinasi HP + capture card tertentu. API/library UVC dan mode resolusi yang tersedia dapat berbeda. Tidak ada jaminan bahwa semua capture card berfungsi.
5. Audio HDMI belum diimplementasikan. Aplikasi ini fokus pada video monitor.
6. Target SDK sengaja 28 untuk kompatibilitas USB Host dengan library UVC lawas; setelah diuji, target SDK sebaiknya dinaikkan.

## Membuat APK di Windows
1. Pasang Android Studio dari situs resmi: https://developer.android.com/studio
2. Ekstrak folder proyek ini.
3. Buka Android Studio → **Open** → pilih folder `CanonHDMIMonitor`.
4. Tunggu Gradle Sync dan unduh Android SDK Platform 35 bila diminta.
5. Pilih **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. APK debug biasanya berada di `app/build/outputs/apk/debug/app-debug.apk`.

## Pengujian pertama
- Hubungkan capture card ke HP melalui USB-C OTG.
- Buka aplikasi, tekan HUBUNGKAN, lalu izinkan akses USB.
- Hidupkan Canon EOS 600D, masuk Live View, lalu sambungkan mini-HDMI ke capture card.
- Jika gambar tidak muncul, coba mode output HDMI 720p, kabel lain, atau USB-C hub bertenaga.

## Dependensi
Library UVC: `org.uvccamera:lib:0.1.0` (Apache 2.0; upstream dan detail: https://alexey-pelykh.com/UVCCamera/).


## Membuat APK tanpa Android Studio (GitHub Actions)
1. Buat akun/login di https://github.com.
2. Buat repository baru, misalnya `CanonHDMIMonitor`.
3. Unggah seluruh isi ZIP proyek ini ke repository pada branch `main` (termasuk folder tersembunyi `.github`).
4. Buka tab **Actions**. Workflow **Build Canon HDMI Monitor APK** akan berjalan setelah push; atau pilih **Run workflow**.
5. Jika berhasil, buka hasil workflow dan unduh artifact `CanonHDMIMonitor-debug-apk`. Di dalamnya ada `app-debug.apk`.
6. Pindahkan APK ke HP dan izinkan instalasi dari sumber tersebut bila Android meminta.

Jika build gagal, buka log workflow dan periksa langkah **Build debug APK**. Build cloud menghilangkan kebutuhan memasang Android Studio di PC, tetapi tetap memerlukan internet dan akun GitHub. APK yang berhasil dibangun tetap perlu diuji pada HP + capture card.
