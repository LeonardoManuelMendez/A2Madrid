# iosApp — envoltorio iOS (Xcode)

Glue mínima en SwiftUI que monta la UI de Compose Multiplatform (compartida en `commonMain`)
dentro de una app iOS. La lógica y la UI viven en Kotlin; aquí solo está el contenedor.

- `iosApp/iOSApp.swift` — punto de entrada SwiftUI (`@main`).
- `iosApp/ContentView.swift` — envuelve `MainViewControllerKt.MainViewController()` (Kotlin) en SwiftUI.
- `iosApp/Info.plist` — metadatos del bundle.

## El `.xcodeproj` se genera en un Mac

El proyecto Xcode (`iosApp.xcodeproj`) **no está versionado** porque se crea/abre en macOS
(este repo se desarrolla en Linux). En un Mac, para tener la app ejecutable:

1. **Abrir el proyecto KMP en Android Studio (con el plugin KMP) o en Fleet** y dejar que genere
   el `iosApp.xcodeproj`; o crear una app iOS en Xcode (SwiftUI) y añadir estos tres archivos.
2. Integrar el framework compartido con **integración directa**: en *Build Phases* del target,
   añadir una *Run Script* **antes** de "Compile Sources":
   ```bash
   cd "$SRCROOT/.."
   ./gradlew :app:embedAndSignAppleFrameworkForXcode
   ```
   y en *Build Settings* → *Framework Search Paths* añadir `$(SRCROOT)/../app/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`.
3. `import ComposeApp` (el `baseName` del framework definido en `app/build.gradle.kts`).
4. Seleccionar un simulador y ejecutar (▶).

## CI

El workflow `.github/workflows/ci.yml` compila el **framework de iOS** en un runner macOS en cada
push, de modo que el repositorio verifica públicamente que el código iOS enlaza, sin necesidad de
tener un Mac en local.
