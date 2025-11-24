#!/bin/bash

# 服务管理脚本
# 用法: ./manage-services.sh [start|stop|status|restart]

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT="/Users/leon/Desktop/code/javaProject/StellarMed"
LOG_DIR="$PROJECT_ROOT/logs"

declare -a SERVICES=(
    "patient-service:8101"
    "symptom-ai-service:8201"
    "test-suggestion-ai-service:8083"
    "summary-ai-service:8204"
    "prescription-ai-service:8085"
    "doctor-confirm-service:8301"
)

check_port() {
    local port=$1
    lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1
}

start_service() {
    local service_name=$1
    local port=$2
    local service_dir=$3
    
    if check_port $port; then
        echo -e "${YELLOW}⚠ $service_name (端口: $port) 已在运行${NC}"
        return 1
    fi
    
    echo -e "${BLUE}启动 $service_name (端口: $port)...${NC}"
    cd "$PROJECT_ROOT/$service_dir" || return 1
    
    export JAVA_HOME=$(/usr/libexec/java_home -v 17)
    export PATH=$JAVA_HOME/bin:$PATH
    
    mkdir -p "$LOG_DIR"
    nohup mvn spring-boot:run > "$LOG_DIR/${service_name}.log" 2>&1 &
    local pid=$!
    
    sleep 3
    
    if check_port $port; then
        echo -e "${GREEN}✓ $service_name 启动成功 (PID: $pid)${NC}"
        return 0
    else
        echo -e "${RED}✗ $service_name 启动失败，请查看日志: $LOG_DIR/${service_name}.log${NC}"
        return 1
    fi
}

stop_service() {
    local service_name=$1
    local port=$2
    
    if ! check_port $port; then
        echo -e "${YELLOW}⚠ $service_name (端口: $port) 未运行${NC}"
        return 1
    fi
    
    echo -e "${BLUE}停止 $service_name (端口: $port)...${NC}"
    
    # 查找并杀死占用端口的进程
    local pid=$(lsof -ti :$port)
    if [ -n "$pid" ]; then
        kill $pid 2>/dev/null
        sleep 2
        if check_port $port; then
            kill -9 $pid 2>/dev/null
        fi
        echo -e "${GREEN}✓ $service_name 已停止${NC}"
    fi
}

show_status() {
    echo -e "${BLUE}=== 服务状态 ===${NC}"
    echo ""
    for service_info in "${SERVICES[@]}"; do
        IFS=':' read -r service_name port <<< "$service_info"
        if check_port $port; then
            local pid=$(lsof -ti :$port)
            echo -e "${GREEN}✓${NC} $service_name (端口: $port, PID: $pid)"
        else
            echo -e "${RED}✗${NC} $service_name (端口: $port) - 未运行"
        fi
    done
    echo ""
}

case "$1" in
    start)
        echo -e "${GREEN}启动所有服务...${NC}"
        echo ""
        for service_info in "${SERVICES[@]}"; do
            IFS=':' read -r service_name port <<< "$service_info"
            service_dir=$(echo "$service_name" | tr '-' '_')
            start_service "$service_name" "$port" "$service_name"
            sleep 1
        done
        echo ""
        show_status
        ;;
    stop)
        echo -e "${RED}停止所有服务...${NC}"
        echo ""
        for service_info in "${SERVICES[@]}"; do
            IFS=':' read -r service_name port <<< "$service_info"
            stop_service "$service_name" "$port"
        done
        echo ""
        show_status
        ;;
    status)
        show_status
        ;;
    restart)
        echo -e "${YELLOW}重启所有服务...${NC}"
        echo ""
        for service_info in "${SERVICES[@]}"; do
            IFS=':' read -r service_name port <<< "$service_info"
            stop_service "$service_name" "$port"
            sleep 1
        done
        sleep 2
        echo ""
        for service_info in "${SERVICES[@]}"; do
            IFS=':' read -r service_name port <<< "$service_info"
            start_service "$service_name" "$port" "$service_name"
            sleep 1
        done
        echo ""
        show_status
        ;;
    *)
        echo "用法: $0 {start|stop|status|restart}"
        echo ""
        echo "命令说明:"
        echo "  start   - 启动所有服务"
        echo "  stop    - 停止所有服务"
        echo "  status  - 查看服务状态"
        echo "  restart - 重启所有服务"
        exit 1
        ;;
esac

