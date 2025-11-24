#!/bin/bash

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 设置 JDK 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}启动所有 StellarMed 服务模块${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查 Nacos 是否运行
echo -e "${YELLOW}检查 Nacos 服务器状态...${NC}"
if curl -s http://localhost:8848/nacos > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Nacos 服务器已运行${NC}"
else
    echo -e "${RED}✗ Nacos 服务器未运行，请先启动 Nacos${NC}"
    echo "  启动命令: cd nacos/bin && sh startup.sh -m standalone"
    echo ""
    read -p "是否继续启动服务? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo ""

# 项目根目录
PROJECT_ROOT="/Users/leon/Desktop/code/javaProject/StellarMed"
cd "$PROJECT_ROOT" || exit

# 定义服务列表（服务名:端口:目录）
declare -a SERVICES=(
    "patient-service:8101:patient-service"
    "symptom-ai-service:8201:symptom-ai-service"
    "diagnosis-ai-service:8202:diagnosis-ai-service"
    "test-suggestion-ai-service:8083:test-suggestion-ai-service"
    "summary-ai-service:8204:summary-ai-service"
    "prescription-ai-service:8085:prescription-ai-service"
    "doctor-confirm-service:8301:doctor-confirm-service"
)

# 日志目录
LOG_DIR="$PROJECT_ROOT/logs"
mkdir -p "$LOG_DIR"

# 启动服务函数
start_service() {
    local service_name=$1
    local port=$2
    local service_dir=$3
    
    echo -e "${YELLOW}启动 $service_name (端口: $port)...${NC}"
    
    # 检查端口是否被占用
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "${RED}✗ 端口 $port 已被占用，跳过 $service_name${NC}"
        return 1
    fi
    
    cd "$PROJECT_ROOT/$service_dir" || return 1
    
    # 后台启动服务
    nohup mvn spring-boot:run > "$LOG_DIR/${service_name}.log" 2>&1 &
    local pid=$!
    
    # 等待几秒检查服务是否启动成功
    sleep 5
    
    if ps -p $pid > /dev/null 2>&1; then
        echo -e "${GREEN}✓ $service_name 启动中 (PID: $pid, 端口: $port)${NC}"
        echo "  日志文件: $LOG_DIR/${service_name}.log"
        return 0
    else
        echo -e "${RED}✗ $service_name 启动失败，请查看日志: $LOG_DIR/${service_name}.log${NC}"
        return 1
    fi
}

# 启动所有服务
echo -e "${GREEN}开始启动所有服务...${NC}"
echo ""

for service_info in "${SERVICES[@]}"; do
    IFS=':' read -r service_name port service_dir <<< "$service_info"
    start_service "$service_name" "$port" "$service_dir"
    sleep 2  # 服务间启动间隔
done

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}所有服务启动完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "服务列表："
for service_info in "${SERVICES[@]}"; do
    IFS=':' read -r service_name port service_dir <<< "$service_info"
    echo "  - $service_name: http://localhost:$port"
done
echo ""
echo "查看日志: tail -f $LOG_DIR/*.log"
echo "停止所有服务: pkill -f 'spring-boot:run'"
echo ""

