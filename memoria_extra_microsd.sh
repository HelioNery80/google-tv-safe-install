#!/usr/bin/env bash
# =============================================================================
# memoria_extra_microsd.sh
# TV JVC (MStar Edison, Android 4.2.1, kernel 3.1.10) — usa um microSD
# (em adaptador USB) como área de TROCA (swap) para a RAM.
#
# O que este script FAZ:
#   - cria um arquivo de troca no cartão, liga a troca e mostra a memória.
# O que ele NÃO FAZ:
#   - não grava nada no firmware, na eMMC, na partição system ou no boot.
#   - nada fica permanente: ao reiniciar a TV, a troca desliga sozinha.
#
# Uso (no computador, com adb instalado e a TV na mesma rede):
#   ./memoria_extra_microsd.sh <IP_DA_TV> ligar   [MB]   (padrão 512)
#   ./memoria_extra_microsd.sh <IP_DA_TV> status
#   ./memoria_extra_microsd.sh <IP_DA_TV> desligar
#
# Antes: formate o microSD em FAT32 no computador e ligue-o na TV
# pelo adaptador USB. Use um cartão que você aceita desgastar.
# =============================================================================
set -u

IP="${1:-}"; ACAO="${2:-status}"; TAM="${3:-512}"
BB=/system/bin/busybox
ARQ_NOME=tv_swapfile
[ -z "$IP" ] && { echo "Uso: $0 <IP_DA_TV> ligar|status|desligar [MB]"; exit 1; }
case "$TAM" in 128|256|512|768|1024) ;; *) echo "Tamanho permitido: 128, 256, 512, 768 ou 1024 MB"; exit 1;; esac

# Android 4.2 não devolve o código de saída do 'adb shell', então
# cada comando imprime OK/FALHA e o script lê o texto.
sh_tv() { adb -s "$IP:5555" shell "$1" | tr -d '\r'; }

echo ">> Conectando em $IP:5555 ..."
adb connect "$IP:5555" >/dev/null
adb -s "$IP:5555" get-state 2>/dev/null | grep -q device || { echo "TV não respondeu. Confira IP e Depuração USB."; exit 1; }

# Firmware é 'userdebug': tenta 'adb root'; se não der, usa 'su'.
adb -s "$IP:5555" root >/dev/null 2>&1; sleep 3
adb connect "$IP:5555" >/dev/null
if [ "$(sh_tv 'id -u')" = "0" ]; then RUN() { sh_tv "$1"; }
elif sh_tv 'su -c id' | grep -q 'uid=0'; then RUN() { sh_tv "su -c '$1'"; }
else echo "Sem acesso root (adb root e su falharam). Nada foi alterado."; exit 1; fi

# Confirma que o kernel aceita troca.
RUN "[ -f /proc/swaps ] && echo OK || echo FALHA" | grep -q OK \
  || { echo "Kernel sem suporte a swap. Nada foi alterado."; exit 1; }

# Acha o cartão montado (pontos de montagem do init.rc deste firmware).
DIR=$(RUN "for d in /mnt/usb/sda1 /mnt/usb/sdb1 /mnt/usb/mmcblka1 /mnt/usb/*; do \
  grep -q \" \$d \" /proc/mounts && [ -w \$d ] && { echo \$d; break; }; done")
DIR=$(echo "$DIR" | head -n1)

status() {
  echo; echo "== Memória =="; RUN "$BB free -m"
  echo; echo "== Áreas de troca =="; RUN "cat /proc/swaps"
  echo; echo -n "swappiness: "; RUN "cat /proc/sys/vm/swappiness"
}

case "$ACAO" in
ligar)
  [ -z "$DIR" ] && { echo "Nenhum cartão/USB gravável encontrado em /mnt/usb. Nada foi alterado."; exit 1; }
  ARQ="$DIR/$ARQ_NOME"
  if RUN "grep -q $ARQ /proc/swaps && echo SIM" | grep -q SIM; then
    echo "A troca já está ligada em $ARQ."; status; exit 0; fi
  LIVRE=$(RUN "$BB df -m $DIR | $BB tail -n1 | $BB awk '{print \$4}'")
  [ "${LIVRE:-0}" -lt $((TAM + 64)) ] 2>/dev/null && { echo "Pouco espaço no cartão (${LIVRE} MB livres)."; exit 1; }

  echo ">> Criando arquivo de troca de ${TAM} MB em $ARQ (leva alguns minutos)..."
  # dd com zeros reais: arquivo sem 'buracos', exigência do kernel.
  RUN "$BB dd if=/dev/zero of=$ARQ bs=1048576 count=$TAM && sync && echo OK" | grep -q OK \
    || { echo "Falha ao criar o arquivo."; RUN "rm -f $ARQ"; exit 1; }
  RUN "$BB mkswap $ARQ >/dev/null && echo OK" | grep -q OK \
    || { echo "Falha no mkswap."; RUN "rm -f $ARQ"; exit 1; }
  if ! RUN "$BB swapon $ARQ && echo OK" | grep -q OK; then
    echo "O kernel recusou a troca neste sistema de arquivos. Use o cartão em FAT32."
    RUN "rm -f $ARQ"; exit 1; fi
  # Prioridade baixa: só usa o cartão quando a RAM realmente apertar.
  RUN "echo 20 > /proc/sys/vm/swappiness"
  echo ">> Memória extra LIGADA. Ela desliga sozinha ao reiniciar a TV."
  status ;;
desligar)
  ARQ=$(RUN "$BB awk '/$ARQ_NOME/{print \$1}' /proc/swaps" | head -n1)
  if [ -n "$ARQ" ]; then
    RUN "$BB swapoff $ARQ && rm -f $ARQ && echo OK" | grep -q OK && echo ">> Troca desligada e arquivo apagado."
  else
    echo "Nenhuma troca deste script está ligada."
    [ -n "$DIR" ] && RUN "rm -f $DIR/$ARQ_NOME"
  fi
  status ;;
status) status ;;
*) echo "Ação inválida: use ligar, status ou desligar"; exit 1 ;;
esac
