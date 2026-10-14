# A game for GFF AWARD 2027

## Installing from source

### Prerequisites

1. The build system requires JDK 25 to run. Install it with one of the following if missing:

   ```powershell
   # Windows
   winget install EclipseAdoptium.Temurin.25.JDK
   ```

   ```zsh
   # macOS
   brew install --cask temurin@25
   ```

   ```bash
   # Ubuntu
   sudo apt install openjdk-25-jdk
   ```

2. Clone the [source](https://github.com/hhth24340005/kt-gff2027-2d) with git:

   ```bash
   git clone https://github.com/hhth24340005/kt-gff2027-2d.git
   cd kt-gff2027-2d
   ```

---

### Building

This command will output the fully bundled application image to `./app/build/jpackage/image/`:

```bash
./gradlew :app:jpackage
```

---

### Running directly

```bash
./gradlew :game:jvmRun
```

---

### Testing and Linting

```bash
./gradlew ktlint detekt allTests
```

## Repository layout

| File/Folder | Explanation |
|-|-|
| `app/` |  Bundles the game with Java into a standalone application |
| `game/` | The game logic itself |
| `engine2d/` | High-level graphics API for 2D games |
| `wrapper/` | Kotlin-friendly API for FFI |
| `math/`| Math utilities |
| `native/webgpu/` |  Downloads the WebGPU library and makes it callable from Java |
| `healthcheck/` | A tiny program to check that the native library loads in JVM, used by CI |
| `build-logic/` | Build settings shared by the directories above |
| `gradlew(.bat)` | Runs Gradle; downloads the configured Gradle distribution on first use |
| `gradle/` | The versions for Gradle itself and for libraries used by the build |

## [License](LICENSE)

The source code is distributed under the [MIT License](https://opensource.org/license/mit).
