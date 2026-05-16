#!/usr/bin/env bash
# Run Fabric dev client with JDK 21 on PATH (Gradle wrapper needs a JVM before it reads org.gradle.java.home).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
  :
elif [[ -d "/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home" ]]; then
  # Homebrew on Apple Silicon
  export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
elif [[ -d "/usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home" ]]; then
  # Homebrew on Intel Mac
  export JAVA_HOME="/usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
else
  echo "Could not find JDK 21. Install Homebrew openjdk@21: brew install openjdk@21" >&2
  echo "Or export JAVA_HOME to a JDK 21 install, then run this script again." >&2
  exit 1
fi

export PATH="${JAVA_HOME}/bin:${PATH}"
exec ./gradlew runClient
