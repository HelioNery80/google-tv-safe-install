# 🚀 Guia Prático: Instalar Launcher Personalizado - ADB (RECOMENDADO)

## ⚡ RÁPIDO: Por que começar com launcher?

- ✅ **Tempo**: 2-4 horas (não precisa compilar)
- ✅ **Risco**: Mínimo (sem tocar firmware/kernel)
- ✅ **Resultado**: "Nova interface" sem danificar TV
- ✅ **Revertível**: Basta desinstalar

**Esta é a forma mais inteligente de começar!**

---

## 📋 PASSO 0: VERIFICAR PRÉ-REQUISITOS

### 0.1 Confirmar que sua TV suporta ADB

```bash
# Na TV, vá para:
Menu → Configurações → Sobre
Anotar:
- Versão Android
- Build number

# Depois:
Menu → Configurações → Segurança
Procurar por: "USB Debugging" ou "ADB Debugging"
```

**Se não achar:**
- Tentar: Configurações → Desenvolvimento → ADB
- Televisão pode precisar update anterior
- Último recurso: Compilar launcher sem ADB

### 0.2 Computador (Windows/Mac/Linux)

```bash
# Instalar ADB
# Windows: https://developer.android.com/studio/releases/platform-tools
# Mac: brew install android-platform-tools
# Linux: sudo apt-get install android-tools-adb

# Verificar instalação
adb version
# Deve retornar versão (ex: 31.0.1)
```

### 0.3 Conexão de rede

- TV e computador **na mesma rede Wi-Fi**
- Ou cabo ethernet na TV
- Testar ping: `ping [IP_DA_TV]`

---

## 🔐 PASSO 1: HABILITAR ADB NA TV

### 1.1 Ativar "USB Debugging"

```
Menu da TV
  ↓
Configurações / Settings
  ↓
Sobre a TV / About
  ↓
Build Number (clicar 5-10 vezes) 
  ↓
Voltar para Configurações
  ↓
Opções de Desenvolvedor / Developer Options
  ↓
USB Debugging → ATIVAR
  ↓
ADB Wireless (opcional - mais fácil que USB)
  ↓
Anotar IP que aparecer (ex: 192.168.1.100:5555)
```

### 1.2 Permitir conexão quando ligar TV

Quando primeira conexão ADB:
- TV mostrará mensagem: "Permitir depuração USB?"
- Selecionar **"Sim"** ou **"Allow"**
- ✅ Marcar "Confiar neste computador"

---

## 💻 PASSO 2: CONECTAR COM ADB

### 2.1 Usando cable USB (mais seguro)

```bash
# Conectar pendrive com debug ativado
adb devices
# Resultado esperado:
# List of attached devices
# [SERIAL_NUMBER]    device

# Se aparecer "unauthorized":
# - Volta à TV e autoriza no popup
# - Depois: adb devices novamente
```

### 2.2 Usando Wi-Fi/Rede (recomendado)

```bash
# 1. ATIVAR na TV
Configurações → Opções Desenvolvedor → ADB Wireless

# 2. TV exibirá: IP_ADDRESS:PORT (ex: 192.168.1.100:5555)

# 3. No computador:
adb connect 192.168.1.100:5555

# 4. Autorizar novamente na TV popup

# 5. Verificar conexão:
adb devices
# Esperado: 192.168.1.100:5555    device
```

### Troubleshooting conexão:

```bash
# Se não conecta:
adb kill-server
adb start-server
adb connect 192.168.1.100:5555

# Checar se TV está respondendo:
ping 192.168.1.100
# Deve receber resposta

# Forçar reconexão:
adb disconnect
adb connect 192.168.1.100:5555
```

---

## 🎨 PASSO 3: ESCOLHER LAUNCHER PERSONALIZADO

### 3.1 Opções de Launcher para Smart TV

#### A. **Wolf Launcher** (Recomendado)
- Download: GitHub wolfylauncher
- Tamanho: ~15MB
- Customização: Alta
- Compatibilidade: Android 6+

#### B. **ATV Launcher**
- Minimalista e leve
- Melhor performance
- Menos customização
- Ideal para TV velha

#### C. **Projectivy**
- Tema moderno
- Muitos ícones customizáveis
- Mais recursos

#### D. **Smart Launcher**
- Categorização automática
- Interface limpa
- Bom para muitos apps

#### E. **CRIAR SEU PRÓPRIO**
- Android Studio + Android TV SDK
- Tempo: 20-40 horas
- Maior controle

### 3.2 Download do launcher

```bash
# Opção 1: Baixar APK pronto
# GitHub → [launcher]/releases → download .apk

# Opção 2: Compilar (avançado)
# Android Studio → File → Open → [pasta launcher]
# Build → Build APK

# Guardar em: ~/Downloads/launcher.apk
```

---

## 📲 PASSO 4: INSTALAR LAUNCHER VIA ADB

### 4.1 Instalar o APK

```bash
# Verificar conexão ainda está ativa
adb devices

# Transferir APK para TV
adb install ~/Downloads/wolf_launcher.apk

# Resultado esperado:
# Success

# Se der erro "INSTALL_PARSE_FAILED_NO_CERTIFICATES":
adb install -r ~/Downloads/wolf_launcher.apk  # -r = reinstalar
```

### 4.2 Definir como launcher padrão

**OPÇÃO A: Manualmente na TV**
```
Menu → Configurações → Apps → Padrão
Procurar: Launcher / Home
Selecionar seu novo launcher
```

**OPÇÃO B: Via ADB (automático)**
```bash
# Encontrar package name (geralmente está no APK)
# Exemplo: com.wolfysoft.launcher

adb shell cmd package set-home-user-as-default com.wolfysoft.launcher

# Verificar:
adb shell getprop ro.com.android.launcher
```

### 4.3 Remover launcher antigo (opcional)

```bash
# Listar apps instalados
adb shell pm list packages | grep launcher

# Desinstalar launcher antigo (cuidado! ter novo funcionando)
adb shell pm uninstall --user 0 com.old.launcher

# NUNCA desinstale: com.android.tv.launcher
```

---

## 🎯 PASSO 5: PERSONALIZAÇÃO

### 5.1 Instalar temas

```bash
# Baixar APK de tema
# Instalar via ADB
adb install ~/Downloads/tema_dark.apk

# Ativar na app do launcher (configurações)
```

### 5.2 Instalar seus apps

```bash
# Seu APK personalizado
adb install ~/Downloads/meu_app.apk

# Vários ao mesmo tempo
adb install ~/Downloads/*.apk

# Remover app
adb uninstall com.seu.pacote
```

### 5.3 Backup e restauro

```bash
# Fazer backup de toda configuração
adb backup -apk -shared -all -f backup_launcher.ab

# Restaurar depois
adb restore backup_launcher.ab
```

---

## 🔧 PASSO 6: CUSTOMIZAÇÃO AVANÇADA

### 6.1 Adicionar script de inicialização

```bash
# Script que roda ao iniciar TV
cat > /home/seu_usuario/init_launcher.sh << 'EOF'
#!/bin/bash
adb connect 192.168.1.100:5555
adb shell am start -n com.wolfysoft.launcher/com.wolfysoft.launcher.MainActivity
EOF

chmod +x init_launcher.sh
```

### 6.2 Wallpaper personalizado

```bash
# Transferir imagem PNG/JPG
adb push ~/Imagens/wallpaper.png /sdcard/DCIM/

# Defini-la como wallpaper via launcher settings
# Ou via comando (depende do launcher)
```

### 6.3 Remapear botões do controle

```bash
# Arquivo de configuração de botões:
adb pull /system/usr/keylayout/

# Editar e reenviar (precisa root - difícil)
# Melhor: usar app "Button Remapper"
adb install ~/Downloads/button_remapper.apk
```

---

## 📊 PASSO 7: OTIMIZAÇÃO

### 7.1 Aumentar performance

```bash
# Limpar cache
adb shell pm trim-caches 1024M

# Desabilitar animações de sistema
adb shell settings put global animator_duration_scale 0

# Aumentar memória de transição
adb shell settings put secure transition_animation_scale 0.5
```

### 7.2 Monitorar uso de recursos

```bash
# Ver consumo de memória
adb shell dumpsys meminfo

# Ver processo mais pesado
adb shell top -n 1

# Ver apps rodando
adb shell pm list packages -3  # apenas user apps
```

### 7.3 Melhorar inicialização

```bash
# Apps para rodar no boot
# Arquivo: /system/etc/init.d/ (requer root)

# Sem root, via launcher settings:
# Configurar apps "favorites" que carregam rápido
```

---

## 🎨 PASSO 8: CRIAR LAUNCHER DO ZERO (Opcional)

### 8.1 Estrutura básica em Android Studio

```java
// MainActivity.java
package com.meu.launcher;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Seu código aqui
    }
}
```

### 8.2 AndroidManifest.xml (importante)

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.meu.launcher">
    
    <uses-permission android:name="android.permission.GET_INSTALLED_APPS" />
    <uses-permission android:name="android.permission.REORDER_TASKS" />
    
    <application>
        <activity android:name=".MainActivity"
            android:theme="@android:style/Theme.NoTitleBar.Fullscreen">
            
            <!-- CRÍTICO: Definir como home -->
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.HOME" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### 8.3 Build e instalar seu launcher

```bash
# Android Studio:
# Build → Build APK → release

# Ou linha de comando:
./gradlew build

# Instalar
adb install app/build/outputs/apk/release/app-release.apk
```

---

## ✅ CHECKLIST ANTES DE COMEÇAR

- [ ] USB Debugging ativado na TV
- [ ] ADB conectado e funcionando (`adb devices`)
- [ ] Launcher baixado (.apk)
- [ ] Package name do launcher identificado
- [ ] Backup da TV feito (opcional mas recomendado)
- [ ] Computador com espaço em disco (~500MB)

---

## 🛠️ TROUBLESHOOTING FINAL

| Problema | Solução |
|----------|---------|
| ADB não conecta | Reiniciar TV; verificar USB Debug ativo; mesma rede Wi-Fi |
| APK não instala | Usar `adb install -r`; verificar formato APK |
| Launcher não aparece | Restart TV; verificar set-home-user-as-default |
| TV trava em launcher | Desinstalar; instalar launcher oficial; reiniciar |
| Botões não funcionam | Mapear no launcher settings |

---

## 📞 SUPORTE

**Se algo quebrar:**
1. Adb shell pm uninstall seu.launcher
2. Reiniciar TV
3. Volta ao launcher original

**Comunidades:**
- XDA Developers (seção launcher Android TV)
- GitHub issues do launcher que escolheu
- Stack Overflow (tag android-launcher)

---

**Tempo total**: 2-4 horas
**Dificuldade**: ⭐⭐ (iniciante-intermediário)
**Risco**: Baixo (totalmente revertível)

