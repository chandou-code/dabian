#!/bin/bash

echo "校园失物招领系统 - 后端启动脚本"
echo

echo "1. 检查Java环境..."
if ! java -version 2>&1 | head -1 | grep -q "java"; then
    echo "错误：未找到Java环境，请安装Java 11或更高版本"
    exit 1
fi

echo
echo "2. 检查Maven环境..."
if ! mvn -version &> /dev/null; then
    echo "警告：未找到Maven环境，请确保已安装Maven"
fi

echo
echo "3. 编译项目..."
if ! mvn clean compile; then
    echo "错误：项目编译失败"
    exit 1
fi

echo
echo "4. 启动应用..."
echo "请确保："
echo "- MySQL服务已启动"
echo "- 数据库 campus_lost_found 已创建"
echo "- 数据库密码为 123456"
echo
echo "应用将在 http://localhost:8080 启动"
echo "API文档地址: http://localhost:8080/api/doc.html"
echo "按 Ctrl+C 停止应用"
echo

mvn spring-boot:run