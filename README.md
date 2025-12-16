# Wave Distortion Demo

A Compose Multiplatform application demonstrating a wave distortion effect using a RuntimeShader (AGSL/SKSL).

This project showcases complex visual effects in Compose Multiplatform that work across Android, iOS, Desktop (JVM), and Web (Wasm).

## Features

- **Interactive Ripple Effect**: Tap anywhere on the image to trigger a wave distortion ripple.
- **Real-time Customization**: Adjust shader parameters dynamically using the control panel:
  - **Amplitude**: Controls the strength of the distortion.
  - **Angular Frequency**: Adjusts the frequency of the waves.
  - **Decay Rate**: Determines how quickly the ripple fades out.
  - **Wave Speed**: Controls the speed of the wave propagation.
  - **Tint Ratio**: Adds a chromatic aberration effect based on the distortion strength.
- **Auto Waves**: Toggle automatic random wave generation to see the effect in action without interaction.

## Platforms

This project targets the following platforms using Kotlin Multiplatform:

- **Android**
- **iOS**
- **Desktop (JVM)**
- **Web (Wasm)**

## Getting Started

### Prerequisites

- JDK 17 or higher
- Android Studio
- Xcode (for iOS development, macOS only)

### Build and Run

**Android**
```shell
./gradlew :composeApp:assembleDebug
```

**Desktop**
```shell
./gradlew :composeApp:run
```

**Web (Wasm)**
```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

**iOS**

- Use [Kotlin Multiplatform Intellij Plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform)
  
or

- Open `iosApp/iosApp.xcodeproj` in Xcode and run the application.

## Technologies Used

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [AGSL/SKSL](https://developer.android.com/develop/ui/views/graphics/agsl)
