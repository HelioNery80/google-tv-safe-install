#!/usr/bin/env bash
# Compila o launcher sem Android Studio nem Gradle.
# Precisa de: javac (JDK 8+), aapt, dx, zipalign, apksigner, android.jar (API 17 ou maior)
#   Ubuntu/Debian: sudo apt-get install aapt dalvik-exchange zipalign apksigner android-sdk-platform-23
# Uso: ./build.sh   → gera build/InicioTV.apk
set -euo pipefail
cd "$(dirname "$0")"

ANDROID_JAR="${ANDROID_JAR:-$(ls /usr/lib/android-sdk/platforms/*/android.jar 2>/dev/null | head -n1)}"
DX="${DX:-$(command -v dx || command -v dalvik-exchange || echo /usr/lib/android-sdk/build-tools/debian/dx)}"
[ -f "$ANDROID_JAR" ] || { echo "android.jar não encontrado (defina ANDROID_JAR)"; exit 1; }

rm -rf build && mkdir -p build/gen build/classes

# Ícone simples gerado na hora (sem arquivo binário no repositório)
mkdir -p res/drawable-mdpi
python3 - <<'EOF'
from PIL import Image, ImageDraw
im = Image.new("RGBA", (96, 96), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle((4, 14, 92, 72), 10, fill=(26, 30, 39, 255), outline=(159, 216, 207, 255), width=5)
d.rectangle((38, 76, 58, 84), fill=(159, 216, 207, 255))
d.rectangle((26, 84, 70, 89), fill=(159, 216, 207, 255))
im.save("res/drawable-mdpi/ic_launcher.png")
EOF

aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -I "$ANDROID_JAR" -F build/unsigned.apk
javac -source 8 -target 8 -nowarn -Xlint:-options -bootclasspath "$ANDROID_JAR" \
      -d build/classes $(find src build/gen -name '*.java')
"$DX" --dex --min-sdk-version=17 --output=build/classes.dex build/classes
(cd build && aapt add unsigned.apk classes.dex >/dev/null)
zipalign -f 4 build/unsigned.apk build/aligned.apk

# Assinatura de teste (chave local). Para publicar, troque pela sua chave.
KS=build/../debug.keystore
[ -f "$KS" ] || keytool -genkeypair -keystore "$KS" -storepass android -keypass android \
    -alias inicio -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Inicio TV" >/dev/null 2>&1
# Android 4.2 só entende a assinatura v1 (JAR)
apksigner sign --ks "$KS" --ks-pass pass:android --ks-key-alias inicio \
    --min-sdk-version 17 --v1-signing-enabled true --v2-signing-enabled false \
    --out build/InicioTV.apk build/aligned.apk
echo "OK: $(pwd)/build/InicioTV.apk"
