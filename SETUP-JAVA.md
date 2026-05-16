# JDK 21 for Medieval Kingdoms (Fabric 1.21.11)

This project targets **Java 21** (`build.gradle` uses `release = 21`). Gradle must run on a **JDK** (not only a JRE).

## Why `JAVA_HOME` still matters

`gradle.properties` can set **`org.gradle.java.home`** so the Gradle **daemon** uses JDK 21. The **`gradlew`** script still needs a **`java`** on your **PATH** (or **`JAVA_HOME`**) to **start** Gradle—nothing in `gradle.properties` runs before that first JVM launches.

So: either install a JDK 21 that macOS exposes to `/usr/bin/java` (e.g. Temurin installer), or **export `JAVA_HOME`** / **PATH** in the terminal (or use **`scripts/run-client.sh`**, which does this for you).

## Install (macOS)

**Homebrew `openjdk@21` (no admin password for the JDK itself):**

```bash
brew install openjdk@21
```

Typical install locations:

| Mac | `JAVA_HOME` for Homebrew `openjdk@21` |
|-----|----------------------------------------|
| **Apple Silicon** (`/opt/homebrew`) | `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home` |
| **Intel** (`/usr/local`) | `/usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home` |

Add to `~/.zshrc` (or run in each terminal session):

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"   # Apple Silicon — Intel: use /usr/local/... as in the table above
export PATH="$JAVA_HOME/bin:$PATH"
```

**Homebrew Temurin (`brew install --cask temurin@21`):** uses Apple’s `.pkg` installer and may prompt for your **login password** (`sudo`). After install, `/usr/libexec/java_home -v 21` often works:

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
```

## Pin Gradle’s daemon (optional but recommended)

This repo sets **`org.gradle.java.home`** in `gradle.properties` to the Apple Silicon Homebrew path above. If you use **Intel Homebrew** or **Temurin**, edit that line to your real JDK path so Gradle daemons always use 21.

## Run the dev client

From the repo root (starts Minecraft; quit the game to return to the shell):

```bash
./scripts/run-client.sh
```

On Windows, use `gradlew runClient` in PowerShell after setting `JAVA_HOME` / `PATH` to JDK 21 (see below).

## Install (Windows)

Install **Eclipse Temurin 21** from [Adoptium](https://adoptium.net/). In PowerShell or CMD, set:

```text
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.x.x-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
```

(Adjust the path to match your installed folder.)

## Cursor / VS Code

After installing the JDK, reload the window and ensure the **Extension Pack for Java** uses that JDK (Command Palette → “Java: Configure Java Runtime”).

## Verify

From the project root, **with `JAVA_HOME` set** if needed:

```bash
./gradlew --version
```

You should see **Launcher JVM: 21** and **Daemon JVM** pointing at your JDK. Then:

```bash
./gradlew genSources
./gradlew build
```

On Windows, use `gradlew` instead of `./gradlew`, and run each command on its own line.
