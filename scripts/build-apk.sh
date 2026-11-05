#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SDK_DIR="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Android/Sdk}}"
JDK_DIR="${JAVA_HOME:-}"
OUT="$PROJECT_DIR/build-local"

if [[ -z "$JDK_DIR" ]]; then
    if command -v javac >/dev/null 2>&1; then
        JDK_DIR="$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")"
    elif [[ -x /snap/android-studio/current/jbr/bin/javac ]]; then
        JDK_DIR="/snap/android-studio/current/jbr"
    fi
fi

BUILD_TOOLS_DIR="$(find "$SDK_DIR/build-tools" -mindepth 1 -maxdepth 1 -type d -print 2>/dev/null | sort -V | tail -n 1)"
PLATFORM_DIR="$(find "$SDK_DIR/platforms" -mindepth 1 -maxdepth 1 -type d -print 2>/dev/null | sort -V | tail -n 1)"
BUILD_TOOLS="$BUILD_TOOLS_DIR"
ANDROID_JAR="$PLATFORM_DIR/android.jar"

for tool in aapt d8 zipalign apksigner; do
    if [[ ! -x "$BUILD_TOOLS/$tool" ]]; then
        echo "Ferramenta ausente: $BUILD_TOOLS/$tool" >&2
        exit 1
    fi
done
if [[ -z "$JDK_DIR" || ! -x "$JDK_DIR/bin/javac" || ! -f "$ANDROID_JAR" ]]; then
    echo "JDK ou Android SDK não encontrado. Ajuste JAVA_HOME ou ANDROID_SDK_ROOT." >&2
    exit 1
fi
export JAVA_HOME="$JDK_DIR"
export PATH="$JDK_DIR/bin:$PATH"

mkdir -p "$OUT"
WORK="$(mktemp -d "$OUT/.build.XXXXXX")"
trap 'rm -rf "$WORK"' EXIT
mkdir -p "$WORK/classes" "$WORK/dex"

"$JDK_DIR/bin/javac" -source 8 -target 8 -bootclasspath "$ANDROID_JAR" \
    -d "$WORK/classes" \
    "$PROJECT_DIR/app/src/main/java/br/com/casa/launcher/MainActivity.java"

mapfile -t CLASS_FILES < <(find "$WORK/classes" -name '*.class' -print)
"$BUILD_TOOLS/d8" --min-api 23 --lib "$ANDROID_JAR" --output "$WORK/dex" "${CLASS_FILES[@]}"

"$BUILD_TOOLS/aapt" package -f \
    -M "$PROJECT_DIR/app/src/main/AndroidManifest.xml" \
    -I "$ANDROID_JAR" \
    -F "$WORK/casa-unsigned.apk" \
    --min-sdk-version 23 --target-sdk-version 28 \
    --version-code 1 --version-name 1.0

(cd "$WORK/dex" && "$BUILD_TOOLS/aapt" add "$WORK/casa-unsigned.apk" classes.dex)
"$BUILD_TOOLS/zipalign" -f 4 "$WORK/casa-unsigned.apk" "$WORK/casa-aligned.apk"

if [[ ! -f "$OUT/casa-debug.keystore" ]]; then
    "$JDK_DIR/bin/keytool" -genkeypair -keystore "$OUT/casa-debug.keystore" \
        -storepass android -keypass android -alias casa-debug \
        -dname "CN=Casa Launcher, OU=Personal, O=Local, L=Local, S=Local, C=BR" \
        -keyalg RSA -keysize 2048 -validity 10000 -noprompt
fi

"$BUILD_TOOLS/apksigner" sign \
    --ks "$OUT/casa-debug.keystore" --ks-key-alias casa-debug \
    --ks-pass pass:android --key-pass pass:android \
    --out "$OUT/CasaLauncher.apk" "$WORK/casa-aligned.apk"
"$BUILD_TOOLS/apksigner" verify "$OUT/CasaLauncher.apk"
echo "APK criado em: $OUT/CasaLauncher.apk"
