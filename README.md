# Study Player 🎓

> **A modern Android study and media experience.**

Study Player is an Android application focused on combining study-oriented content, media playback and a modern Jetpack Compose interface.

## ✨ Highlights

- 🎓 Study-focused experience
- ▶️ Media3 / ExoPlayer playback
- 🎨 Jetpack Compose UI
- 🔥 Firebase integration
- 🗄️ Room local database
- 🌐 Retrofit + OkHttp networking
- 🖼️ Coil image loading
- ⚡ Kotlin Coroutines
- 🧩 Native C++ / CMake integration
- 🧪 Unit, UI and screenshot testing support

## 🧰 Tech Stack

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Media3](https://img.shields.io/badge/Media3-000000?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![C%2B%2B](https://img.shields.io/badge/C%2B%2B-00599C?style=for-the-badge&logo=cplusplus&logoColor=white)

## 🤖 Studio Origin

The repository originated from a Studio/AI-assisted application workflow and uses the Android application package:

`com.aistudio.studycontroller.pvkqrx`

## 🏗️ Architecture

The project combines:

- Kotlin + Jetpack Compose
- ViewModel + lifecycle-aware state
- Room persistence
- Media3 playback
- Firebase services
- Retrofit networking
- Native C++ through CMake
- Kotlin Symbol Processing (KSP)

## 🚀 Run Locally

### Requirements

- Android Studio
- Android SDK 36
- JDK 11
- CMake 3.22.1
- Android device or emulator

### Setup

1. Clone the repository.
2. Open it in Android Studio.
3. Let Gradle sync and download dependencies.
4. Configure required environment values in `.env`.
5. Configure Firebase if required by your local build.
6. Run the debug build on an emulator or physical device.

### Build

```bash
./gradlew assembleDebug
```

For a release build, configure the required signing environment variables first.

## 🔐 Configuration

Keep private credentials outside source control. Use `.env.example` as the reference for required secrets.

## 👤 Author

**Imanshu — @Imanshu999**

[GitHub](https://github.com/Imanshu999)

---

**Learn. Build. Ship. Repeat. ⚡**
