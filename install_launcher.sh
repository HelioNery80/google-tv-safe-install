#!/bin/bash

###############################################################################
# Script Automatizado: Instalar Launcher Android Personalizado via ADB
# Para Smart TV JVC LT-55NU40B
# 
# Uso: ./install_launcher.sh
# 
# Requerimentos:
# - adb instalado (sudo apt-get install android-tools-adb)
# - TV com USB Debugging ativado
# - APK do launcher em ~/Downloads/
###############################################################################

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configurações padrão
TV_IP="192.168.1.100"
TV_PORT="5555"
LAUNCHER_PATH=""
LAUNCHER_PACKAGE=""
LOGFILE="launcher_install.log"

###############################################################################
# FUNÇÕES AUXILIARES
###############################################################################

log() {
    echo -e "${BLUE}[$(date '+%H:%M:%S')]${NC} $1" | tee -a "$LOGFILE"
}

success() {
    echo -e "${GREEN}✓ $1${NC}" | tee -a "$LOGFILE"
}

error() {
    echo -e "${RED}✗ $1${NC}" | tee -a "$LOGFILE"
}

warning() {
    echo -e "${YELLOW}⚠ $1${NC}" | tee -a "$LOGFILE"
}

header() {
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

###############################################################################
# VERIFICAÇÕES INICIAIS
###############################################################################

check_adb_installed() {
    header "VERIFICANDO INSTALAÇÃO DE ADB"
    
    if ! command -v adb &> /dev/null; then
        error "ADB não encontrado!"
        echo -e "\n${YELLOW}Instale com:${NC}"
        echo "Ubuntu/Debian: sudo apt-get install android-tools-adb"
        echo "Mac: brew install android-platform-tools"
        echo "Windows: Baixe em https://developer.android.com/studio/releases/platform-tools"
        exit 1
    fi
    
    ADB_VERSION=$(adb version | head -n1)
    success "ADB instalado: $ADB_VERSION"
}

check_tv_connection() {
    header "VERIFICANDO CONEXÃO COM TV"
    
    log "Procurando por TV na rede..."
    
    # Tentar IP padrão
    if ping -c 1 "$TV_IP" &> /dev/null; then
        success "TV encontrada em $TV_IP"
        return 0
    fi
    
    warning "TV não encontrada em $TV_IP"
    log "Escaneando rede..."
    
    # Tentar escanear rede
    for i in {1..254}; do
        local ip="192.168.1.$i"
        (ping -c 1 "$ip" &> /dev/null) && {
            log "Encontrado: $ip"
        } &
    done
    
    wait
    
    read -p "Digite o IP da TV: " TV_IP
    read -p "Digite a porta (padrão 5555): " TV_PORT
    TV_PORT=${TV_PORT:-5555}
}

connect_adb() {
    header "CONECTANDO ADB À TV"
    
    log "Conectando a $TV_IP:$TV_PORT..."
    
    adb connect "$TV_IP:$TV_PORT" > /dev/null 2>&1
    
    sleep 2
    
    # Verificar conexão
    local devices=$(adb devices | grep "device$")
    
    if echo "$devices" | grep -q "$TV_IP"; then
        success "Conectado à TV: $TV_IP:$TV_PORT"
        return 0
    else
        error "Falha ao conectar!"
        warning "Verifique:"
        echo "  - TV está ligada?"
        echo "  - USB Debugging está ativado?"
        echo "  - Autorizou a conexão no popup da TV?"
        echo "  - TV está na mesma rede?"
        exit 1
    fi
}

###############################################################################
# GERENCIAMENTO DE LAUNCHER
###############################################################################

find_launcher_apk() {
    header "PROCURANDO POR ARQUIVO LAUNCHER (APK)"
    
    # Procurar em Downloads
    local apks=(~/Downloads/*.apk)
    
    if [ ${#apks[@]} -eq 0 ]; then
        error "Nenhum APK encontrado em ~/Downloads"
        read -p "Digite o caminho completo do launcher.apk: " LAUNCHER_PATH
    elif [ ${#apks[@]} -eq 1 ]; then
        LAUNCHER_PATH="${apks[0]}"
        success "APK encontrado: $LAUNCHER_PATH"
    else
        log "Múltiplos APKs encontrados:"
        select apk in "${apks[@]}"; do
            LAUNCHER_PATH="$apk"
            success "Selecionado: $LAUNCHER_PATH"
            break
        done
    fi
    
    # Validar arquivo
    if [ ! -f "$LAUNCHER_PATH" ]; then
        error "Arquivo não encontrado: $LAUNCHER_PATH"
        exit 1
    fi
}

extract_package_name() {
    header "EXTRAINDO PACKAGE NAME DO APK"
    
    if ! command -v aapt &> /dev/null; then
        warning "aapt não encontrado. Instalando android-tools..."
        sudo apt-get install -y android-tools-apk &>/dev/null
    fi
    
    if command -v aapt &> /dev/null; then
        LAUNCHER_PACKAGE=$(aapt dump badging "$LAUNCHER_PATH" | grep "package:" | sed "s/.*name='\([^']*\).*/\1/")
        success "Package name: $LAUNCHER_PACKAGE"
    else
        warning "Não foi possível extrair automaticamente"
        read -p "Digite o package name do launcher (ex: com.wolfysoft.launcher): " LAUNCHER_PACKAGE
    fi
}

install_launcher() {
    header "INSTALANDO LAUNCHER"
    
    log "Transferindo APK para TV..."
    log "Arquivo: $LAUNCHER_PATH"
    log "Tamanho: $(du -h "$LAUNCHER_PATH" | cut -f1)"
    
    if adb -s "$TV_IP:$TV_PORT" install "$LAUNCHER_PATH"; then
        success "Launcher instalado com sucesso!"
        return 0
    else
        warning "Tentando reinstalar (modo force)..."
        if adb -s "$TV_IP:$TV_PORT" install -r "$LAUNCHER_PATH"; then
            success "Launcher instalado (modo force) com sucesso!"
            return 0
        else
            error "Falha ao instalar launcher"
            return 1
        fi
    fi
}

set_default_launcher() {
    header "DEFININDO COMO LAUNCHER PADRÃO"
    
    log "Definindo $LAUNCHER_PACKAGE como home..."
    
    if adb -s "$TV_IP:$TV_PORT" shell cmd package set-home-user-as-default "$LAUNCHER_PACKAGE"; then
        success "Launcher definido como padrão!"
        return 0
    else
        warning "Pode ser necessário fazer manualmente na TV"
        echo ""
        echo "Na TV, faça:"
        echo "  Menu → Configurações → Apps → Padrão → Launcher"
        echo "  Selecione: $(echo $LAUNCHER_PACKAGE | cut -d. -f3)"
        return 1
    fi
}

###############################################################################
# GERENCIAMENTO DE APLICATIVOS
###############################################################################

list_installed_apps() {
    header "APLICATIVOS INSTALADOS NA TV"
    
    log "Obtendo lista de aplicativos..."
    adb -s "$TV_IP:$TV_PORT" shell pm list packages -3 | sed 's/package://' | sort
}

uninstall_app() {
    header "DESINSTALAR APLICATIVO"
    
    read -p "Digite o package name a desinstalar: " package_name
    
    log "Desinstalando $package_name..."
    
    if adb -s "$TV_IP:$TV_PORT" shell pm uninstall --user 0 "$package_name"; then
        success "Aplicativo desinstalado!"
    else
        error "Falha ao desinstalar"
    fi
}

backup_tv() {
    header "FAZER BACKUP DA TV"
    
    local backup_file="tv_backup_$(date +%Y%m%d_%H%M%S).ab"
    
    log "Criando backup: $backup_file"
    log "Isso pode levar alguns minutos..."
    
    if adb -s "$TV_IP:$TV_PORT" backup -apk -shared -all -f "$backup_file"; then
        success "Backup criado: $backup_file"
        log "Tamanho: $(du -h "$backup_file" | cut -f1)"
    else
        error "Falha ao criar backup"
    fi
}

restore_tv() {
    header "RESTAURAR BACKUP"
    
    read -p "Caminho do arquivo de backup (.ab): " backup_file
    
    if [ ! -f "$backup_file" ]; then
        error "Arquivo não encontrado"
        return 1
    fi
    
    log "Restaurando backup..."
    
    if adb -s "$TV_IP:$TV_PORT" restore "$backup_file"; then
        success "Backup restaurado!"
    else
        error "Falha ao restaurar"
    fi
}

###############################################################################
# OTIMIZAÇÃO
###############################################################################

optimize_tv() {
    header "OTIMIZAR PERFORMANCE DA TV"
    
    log "Limpando cache do sistema..."
    adb -s "$TV_IP:$TV_PORT" shell pm trim-caches 1024M
    
    log "Desabilitando animações..."
    adb -s "$TV_IP:$TV_PORT" shell settings put global animator_duration_scale 0.5
    
    log "Otimizando transições..."
    adb -s "$TV_IP:$TV_PORT" shell settings put secure transition_animation_scale 0.5
    
    success "TV otimizada!"
}

show_tv_stats() {
    header "INFORMAÇÕES DA TV"
    
    log "Versão Android:"
    adb -s "$TV_IP:$TV_PORT" shell getprop ro.build.version.release
    
    log "Modelo:"
    adb -s "$TV_IP:$TV_PORT" shell getprop ro.product.model
    
    log "Build:"
    adb -s "$TV_IP:$TV_PORT" shell getprop ro.build.fingerprint
    
    log "Memória livre:"
    adb -s "$TV_IP:$TV_PORT" shell dumpsys meminfo | grep "Total"
}

###############################################################################
# MENU PRINCIPAL
###############################################################################

show_menu() {
    echo ""
    echo -e "${BLUE}╔═════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║  GERENCIADOR DE LAUNCHER - SMART TV     ║${NC}"
    echo -e "${BLUE}╚═════════════════════════════════════════╝${NC}"
    echo ""
    echo "  Instalação:"
    echo "    1) Instalar novo launcher"
    echo "    2) Definir launcher padrão"
    echo ""
    echo "  Gerenciamento:"
    echo "    3) Listar aplicativos instalados"
    echo "    4) Desinstalar aplicativo"
    echo ""
    echo "  Backup & Restauração:"
    echo "    5) Fazer backup da TV"
    echo "    6) Restaurar backup"
    echo ""
    echo "  Otimização:"
    echo "    7) Otimizar performance"
    echo "    8) Ver informações da TV"
    echo ""
    echo "  Conexão:"
    echo "    9) Reconectar à TV"
    echo ""
    echo "    0) Sair"
    echo ""
    read -p "Escolha uma opção (0-9): " opcao
}

###############################################################################
# MAIN
###############################################################################

main() {
    header "INSTALADOR DE LAUNCHER - SMART TV JVC"
    
    # Verificações iniciais
    check_adb_installed
    
    if ! adb devices | grep -q "device$"; then
        check_tv_connection
        connect_adb
    else
        success "TV já conectada"
        adb devices | grep "device$"
    fi
    
    # Menu principal
    while true; do
        show_menu
        
        case $opcao in
            1) 
                find_launcher_apk
                extract_package_name
                install_launcher
                ;;
            2)
                if [ -z "$LAUNCHER_PACKAGE" ]; then
                    read -p "Digite o package name: " LAUNCHER_PACKAGE
                fi
                set_default_launcher
                ;;
            3)
                list_installed_apps
                ;;
            4)
                uninstall_app
                ;;
            5)
                backup_tv
                ;;
            6)
                restore_tv
                ;;
            7)
                optimize_tv
                ;;
            8)
                show_tv_stats
                ;;
            9)
                connect_adb
                ;;
            0)
                success "Encerrando..."
                exit 0
                ;;
            *)
                error "Opção inválida!"
                ;;
        esac
        
        read -p "Pressione Enter para continuar..."
    done
}

# Verificar se é primeira execução
if [ ! -f "$LOGFILE" ]; then
    > "$LOGFILE"
fi

# Executar
main

