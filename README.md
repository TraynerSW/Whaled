# Whaled 🐋

**Whaled** (basé sur le contrôleur WLED) est une application Android moderne développée en **Kotlin** et **Jetpack Compose**, conçue pour contrôler vos appareils WLED. 

Ce projet met un accent particulier sur l'interface utilisateur et la personnalisation visuelle, en intégrant pleinement les principes de **Material Design 3 (Material You)**.

## ✨ Fonctionnalités Principales

* **Interface moderne (Jetpack Compose) :** Une UI fluide et réactive entièrement construite avec les derniers standards d'Android.
* **Support Material You (Couleurs Dynamiques) :** L'application s'adapte automatiquement aux couleurs de votre fond d'écran (sur les appareils compatibles Android 12+).
* **Moteur de thèmes avancé :** 
  * Système (S'adapte aux paramètres du téléphone)
  * Clair (Material You classique)
  * Sombre (Material You sombre)
  * **OLED Noir** (Optimisé pour les écrans AMOLED afin d'économiser de la batterie avec un vrai fond noir)
* **Contrôle WLED :** Intégration transparente pour gérer vos rubans LED.

## 🛠️ Stack Technique

* **Langage :** Kotlin
* **UI Toolkit :** Jetpack Compose, Material3
* **SDK Android :** Compile SDK 35, Min SDK 26 (Android 8.0)
* **Outil de build :** Gradle Kotlin DSL

## 📂 À propos des scripts du projet

À la racine du projet, vous remarquerez la présence de nombreux scripts Bash (`add_dark_theme.sh`, `fix_inversion.sh`, `fix_pixelos_theme.sh`, `update_theme.sh`, etc.). 
Ces scripts ont été utilisés pendant la phase de développement pour :
* Injecter rapidement des modifications de code dans les fichiers Kotlin (via heredoc).
* Itérer sur le moteur de thème et corriger les bugs d'inversion de couleurs.
* Adapter l'application aux spécificités de certaines ROMs Custom (comme PixelOS).
* Générer et modifier les APKs (`fix_apk.sh`, `rename_apk.sh`).

## 🚀 Comment compiler et lancer

Pour compiler le projet localement, vous devez avoir [Android Studio](https://developer.android.com/studio) installé.

1. Clonez ce dépôt :
   ```bash
   git clone https://github.com/TraynerSW/Whaled.git
   ```
2. Ouvrez le dossier du projet dans Android Studio.
3. Attendez la synchronisation de Gradle.
4. Cliquez sur **Run** (le bouton "Play" vert) pour compiler et lancer l'application sur votre émulateur ou votre appareil physique.

Vous pouvez également compiler l'APK en ligne de commande via Gradle :
```bash
# Pour compiler un APK de debug
./gradlew assembleDebug
```
L'APK généré se trouvera dans `app/build/outputs/apk/debug/`.
