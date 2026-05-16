# JDK 21 for Medieval Kingdoms (Fabric 1.21.11)

This project targets **Java 21** (`build.gradle` uses `release = 21`). Gradle must run on a **JDK** (not only a JRE).

## Install (macOS)

**Homebrew (Temurin 21):**

```bash
brew install temurin@21
```

Then set `JAVA_HOME` for your shell (add to `~/.zshrc` if you use zsh):

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

If `/usr/libexec/java_home -v 21` prints nothing, point `JAVA_HOME` at the JDK manually, for example:

```bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
```

## Install (Windows)

Install **Eclipse Temurin 21** from [Adoptium](https://adoptium.net/). In PowerShell or CMD, set:

```text
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.x.x-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
```

(Adjust the path to match your installed folder.)

## Cursor / VS Code

After installing the JDK, reload the window and ensure the **Extension Pack for Java** uses that JDK (Command Palette → “Java: Configure Java Runtime”).

## Optional: pin Gradle to a JDK

If Gradle still picks the wrong Java, you can add **one line** to `gradle.properties` (use your real path):

```properties
org.gradle.java.home=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
```

## Verify

From the project root (macOS/Linux):

```bash
./gradlew --version
```

You should see **JVM: 21** (or compatible). Then:

```bash
./gradlew genSources
./gradlew build
```

On Windows, use `gradlew` instead of `./gradlew`, and run each command on its own line.
