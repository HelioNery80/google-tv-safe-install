# Google TV Safe Install

Script Bash para trocar o launcher de uma Smart TV Android (testado em uma **JVC LT-55NU40B**, Android 9) por uma interface no estilo Google TV/Android TV — **usando apenas ADB**, sem flashar firmware, sem mexer em partições e sem root.

## O que este projeto realmente faz

- Conecta na TV via `adb connect IP:5555` (ADB via rede local).
- Instala um APK de launcher que **você mesmo fornece** (`adb install`).
- Define esse launcher como padrão (`cmd package set-home-user-as-default`).
- Lista/desinstala apps, faz backup e restore da TV (`adb backup` / `adb restore`).
- Aplica pequenos ajustes de performance (cache, animações).

Tudo isso usa comandos ADB padrão do Android — nada aqui modifica o bootloader, a ROM ou a partição de sistema da TV.

## O que este projeto **não** faz (leia antes de usar)

- **Não vira "Google TV" oficial.** Google TV é uma certificação/produto do Google; uma TV comum não se torna uma unidade certificada só por trocar de launcher. O que este script faz é dar uma cara parecida com Google TV (launcher em estilo *leanback*, com cards), usando um APK de launcher de terceiros que você forneça.
- **Não inclui nenhum APK.** O repositório não distribui o launcher em si — você precisa obter um (ex.: um launcher Android TV/leanback de sua escolha, de uma fonte legítima) e apontar o script para ele.
- **Não flasha firmware.** Versões anteriores deste material chegaram a mencionar "arquivos de firmware .bin" para uma instalação mais profunda — esses arquivos eram placeholders de teste, não firmware real, e foram **removidos** deste repositório. Guias que pedem para você baixar um `.bin` de "Google TV" de terceiros e atualizar sua TV via pendrive são, na prática, o método que mais causa *brick* (TV inutilizada) — evite.

## Por que o DRM (Widevine / Netflix 4K) não é afetado

Como o método só instala um app via ADB e nunca toca no firmware, no bootloader ou no keystore da TV, as chaves de DRM protegidas por hardware (ex.: Widevine L1) continuam intactas — o que depende de firmware, como reprodução em 4K na Netflix, segue funcionando normalmente.

## Requisitos

- ADB instalado no computador (`brew install android-platform-tools` no Mac, `sudo apt-get install android-tools-adb` no Linux, ou o pacote *platform-tools* do Android no Windows).
- TV e computador na mesma rede Wi-Fi/Ethernet.
- "Depuração USB"/"ADB Debugging" ativado nas Opções de Desenvolvedor da TV.
- Um APK de launcher que você já tenha (o script pergunta o caminho se não encontrar nada em `~/Downloads`).

## Uso

```bash
chmod +x install_launcher.sh
./install_launcher.sh
```

O script mostra um menu:

1. Instalar novo launcher
2. Definir launcher padrão
3. Listar aplicativos instalados
4. Desinstalar aplicativo
5. Fazer backup da TV
6. Restaurar backup
7. Otimizar performance
8. Ver informações da TV
9. Reconectar à TV

Guia detalhado passo a passo (como ativar ADB na TV, achar o IP, etc.): [`GUIA_ADB.md`](GUIA_ADB.md).

## Reversão

Se algo não agradar: `adb shell pm uninstall <package.do.launcher>`, reinicie a TV e o launcher original volta.

## Aviso

Use por sua conta e risco. Teste sempre com um backup feito (opção 5 do menu) antes de trocar o launcher padrão. Modelos/versões de Android diferentes podem se comportar de forma diferente do testado aqui.

## Licença

MIT — veja [LICENSE](LICENSE).
