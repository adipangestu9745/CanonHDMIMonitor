@echo off
echo Build Canon HDMI Monitor debug APK
if not exist gradlew.bat (
  echo Buka folder ini melalui Android Studio agar Gradle wrapper dapat dibuat, atau gunakan Build APK dari Android Studio.
  pause
  exit /b 1
)
call gradlew.bat assembleDebug
if errorlevel 1 (
  echo Build gagal. Periksa error Gradle/Android SDK di atas.
  pause
  exit /b 1
)
echo APK: app\build\outputs\apk\debug\app-debug.apk
pause
