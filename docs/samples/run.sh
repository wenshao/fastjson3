#!/bin/bash
# fastjson3 示例运行脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

print_info "fastjson3 示例代码运行脚本"
echo ""

# 检查 Java 版本
print_info "检查 Java 版本..."
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ -z "$JAVA_VERSION" ]; then
    print_error "未检测到 Java，请安装 JDK 21+"
    exit 1
fi

if [ "$JAVA_VERSION" -lt 21 ]; then
    print_warn "Java 版本: $JAVA_VERSION (推荐 21+)"
else
    print_info "Java 版本: $JAVA_VERSION ✓"
fi

# 检查 Maven
print_info "检查 Maven..."
if command -v mvn &> /dev/null; then
    print_info "Maven 已安装 ✓"
else
    print_error "未安装 Maven，请先安装"
    echo "  macOS: brew install maven"
    echo "  Ubuntu: sudo apt install maven"
    exit 1
fi

echo ""

# 显示菜单
show_menu() {
    echo "请选择要运行的示例："
    echo "  1) BasicExample - 基础示例"
    echo "  2) AnnotationExample - 注解示例"
    echo "  3) GenericExample - 泛型示例"
    echo "  4) JSONPathExample - JSONPath 示例"
    echo "  5) JSONPathRealWorldExample - JSONPath 实际应用"
    echo "  6) PerformanceExample - 性能示例"
    echo "  7) HighPerformanceExample - 高性能示例"
    echo "  a) 运行所有示例"
    echo "  b) 仅编译"
    echo "  q) 退出"
    echo -n "选择: "
}

# 运行单个示例
run_example() {
    local class=$1
    shift
    print_info "运行 $class..."
    mvn -q exec:java -Dexec.mainClass="$class" "$@"
}

# 仅编译
compile_only() {
    print_info "编译示例代码..."
    mvn clean compile
}

# 运行所有示例
run_all() {
    print_info "运行所有示例..."
    echo ""

    run_example "com.alibaba.fastjson3.samples.basic.BasicExample"
    echo ""
    run_example "com.alibaba.fastjson3.samples.basic.AnnotationExample"
    echo ""
    run_example "com.alibaba.fastjson3.samples.basic.GenericExample"
    echo ""
    run_example "com.alibaba.fastjson3.samples.jsonpath.JSONPathExample"
    echo ""
    run_example "com.alibaba.fastjson3.samples.jsonpath.JSONPathRealWorldExample"
    echo ""
    run_example "com.alibaba.fastjson3.samples.performance.PerformanceExample"
    echo ""
    run_example "com.alibaba.fastjson3.samples.performance.HighPerformanceExample"

    echo ""
    print_info "所有示例运行完成！"
}

# 主循环
if [ "$1" = "--all" ]; then
    # 快速运行所有
    compile_only
    run_all
else
    # 交互式菜单
    while true; do
        show_menu
        read -r choice

        case $choice in
            1)
                compile_only
                run_example "com.alibaba.fastjson3.samples.basic.BasicExample"
                ;;
            2)
                compile_only
                run_example "com.alibaba.fastjson3.samples.basic.AnnotationExample"
                ;;
            3)
                compile_only
                run_example "com.alibaba.fastjson3.samples.basic.GenericExample"
                ;;
            4)
                compile_only
                run_example "com.alibaba.fastjson3.samples.jsonpath.JSONPathExample"
                ;;
            5)
                compile_only
                run_example "com.alibaba.fastjson3.samples.jsonpath.JSONPathRealWorldExample"
                ;;
            6)
                compile_only
                run_example "com.alibaba.fastjson3.samples.performance.PerformanceExample"
                ;;
            7)
                compile_only
                run_example "com.alibaba.fastjson3.samples.performance.HighPerformanceExample"
                ;;
            a|A)
                compile_only
                run_all
                ;;
            b|B)
                compile_only
                print_info "编译完成"
                ;;
            q|Q)
                print_info "退出"
                exit 0
                ;;
            *)

                print_error "无效选择: $choice"
                ;;
        esac

        echo ""
        read -p "按 Enter 继续..." dummy
        clear
    done
fi
