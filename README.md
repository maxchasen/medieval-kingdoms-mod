# Medieval Kingdoms

Fabric mod for Minecraft **1.21.11** (Mojang mappings, Java 21). See the project PRD for gameplay scope.

## Toolchain (pinned)

| Property | Value |
|----------|--------|
| Minecraft | `1.21.11` |
| Fabric Loader | `0.19.2` |
| Fabric API | `0.141.4+1.21.11` |
| Loom | `1.16-SNAPSHOT` (see [Fabric develop](https://fabricmc.net/develop/)) |

## Setup

If `./gradlew --version` fails with no Java runtime, install **JDK 21** first — see [SETUP-JAVA.md](SETUP-JAVA.md). The Gradle **wrapper** needs `java` on your **PATH** (or **`JAVA_HOME`**) before it reads `org.gradle.java.home`; the setup doc explains that.

Then follow the [Fabric documentation](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) for your IDE (Cursor / VS Code Extension Pack for Java, or IntelliJ).

## Run dev client (macOS / Linux)

From the repo root:

```bash
./scripts/run-client.sh
```

This exports `JAVA_HOME` for Homebrew **openjdk@21** (Apple Silicon or Intel paths) and runs `./gradlew runClient`. Quit the Minecraft window to stop. On Windows, set `JAVA_HOME` then run `gradlew runClient` (see SETUP-JAVA.md).

## License

Template base is CC0-1.0 (see `LICENSE`).
