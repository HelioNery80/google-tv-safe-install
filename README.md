# Google TV Safe Install

Ferramentas para dar uma interface nova a uma Smart TV Android antiga **usando apenas ADB**: sem flashar firmware, sem mexer em partições e sem tirar a TV de fábrica. Testado como alvo na **JVC LT-55NU40B**.

## Primeiro: o que esta TV é de verdade

A análise do firmware original da LT-55NU40B (`MstarUpgrade.bin`) mostrou:

| Item | Valor |
|---|---|
| Android | **4.2.1 (API 17)**, build `JOP40D` de 2013 |
| Placa | MStar "Edison", base de software Konka |
| Kernel | Linux 3.1.10 |
| Launcher original | `com.konka.metrolauncher` |
| App de TV | `com.konka.tvsettings` |
| Cartão / pen drive | entra pelo USB e é montado em `/mnt/usb/sda1` |

Versões anteriores deste README diziam Android 9. Estava errado.

## O que **não** dá para fazer nesta TV

- **Google TV ou Android TV oficial.** Precisam de um Android bem mais novo. Não existe firmware mais novo para esta placa, e montar um exigiria drivers fechados da MStar (vídeo, sintonizador, painel) que não são públicos.
- **Netflix e Play Store atuais.** Não funcionam no Android 4.2.
- **"Atualizar" com um `.bin` de Google TV de terceiros.** O arquivo de atualização da MStar apaga e reparticiona toda a memória interna da TV. Um arquivo errado ou editado é a forma mais comum de deixar a TV sem ligar. Não faça.

**Se você quer Google TV de verdade**, ligue um aparelho com Google TV na entrada HDMI. A TV vira só a tela, e o aparelho traz o Android novo, os apps e as atualizações. É o único caminho seguro.

## O que este projeto faz

### 1. Launcher "Início TV" (`launcher/`)

Uma tela inicial nova, em estilo de cards, feita para o Android 4.2 da TV e navegável pelo controle remoto:

- **Início**: destaque "TV ao vivo" (Assistir / Trocar entrada), fileira "Seus apps" e fileira "Mídia" (Vídeos, Fotos, Músicas, Arquivos, Rede, usando o app de mídia da própria TV).
- **Apps**: todos os apps instalados.
- **Memória extra**: mostra RAM e a troca no microSD em tempo real.

Não usa bibliotecas, não pede permissões e não precisa de root. Para compilar:

```bash
cd launcher
./build.sh           # gera launcher/build/InicioTV.apk
```

Para instalar e usar:

```bash
adb connect IP_DA_TV:5555
adb install launcher/build/InicioTV.apk
```

Depois aperte **HOME** no controle, escolha **Início TV** e toque em **Sempre**.

Para voltar ao original: `adb uninstall com.helionery.tvlauncher`. O launcher da Konka volta sozinho.

### 2. Memória extra no microSD (`memoria_extra_microsd.sh`)

Usa um microSD (num adaptador USB) como **área de troca (swap)**. Quando a RAM enche, a TV passa dados pouco usados para o cartão em vez de fechar apps.

```bash
./memoria_extra_microsd.sh IP_DA_TV ligar 512   # 128, 256, 512, 768 ou 1024 MB
./memoria_extra_microsd.sh IP_DA_TV status
./memoria_extra_microsd.sh IP_DA_TV desligar
```

- Funciona porque o firmware desta TV é uma build `userdebug`, com `su` e `busybox`, e o kernel aceita swap.
- **Não é RAM de verdade.** O cartão é centenas de vezes mais lento: ajuda a manter apps abertos, mas trocar de app pode ficar mais lento, e o cartão se desgasta. Use um cartão em FAT32 que você aceite gastar.
- Nada é gravado no sistema. A troca desliga sozinha quando a TV reinicia.

### 3. Menu ADB (`install_launcher.sh`)

Menu para instalar um launcher, listar e remover apps, fazer backup e restaurar, e ver informações da TV. Em Android anterior ao 7, a escolha do launcher padrão é feita pelo botão HOME, como explicado acima.

Guia passo a passo (ativar ADB, achar o IP etc.): [`GUIA_ADB.md`](GUIA_ADB.md).

## Requisitos

- ADB no computador (`brew install android-platform-tools` no Mac, `sudo apt-get install adb` no Linux ou *platform-tools* no Windows).
- TV e computador na mesma rede.
- Depuração USB / ADB ativada nas Opções de Desenvolvedor da TV.
- Para compilar o launcher: JDK 8+, `aapt`, `dx`, `zipalign`, `apksigner` e um `android.jar` (no Ubuntu: `sudo apt-get install aapt dalvik-exchange zipalign apksigner android-sdk-platform-23`).

## Aviso

Use por sua conta e risco. Faça um backup (opção 5 do menu) antes de trocar o launcher. Outros modelos e versões de Android podem se comportar de outro jeito.

## Licença

MIT. Veja [LICENSE](LICENSE).
