@echo off
chcp 65001 >nul
echo 校园失物招领系统 - 后端启动脚本
echo.

echo 1. 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未找到Java环境，请安装Java 17或更高版本
    pause
    exit /b 1
)
echo Java环境正常

echo.
echo 2. 进入项目目录...
cd /d "%~dp0"
if %errorlevel% neq 0 (
    echo 错误：无法进入项目目录
    pause
    exit /b 1
)

echo.
echo 3. 编译项目...
call mvn clean compile
if %errorlevel% neq 0 (
    echo 错误：项目编译失败，请检查代码和依赖
    pause
    exit /b 1
)
echo 编译成功

echo.
echo 4. 启动应用...
echo 请确保：
echo - MySQL服务已启动
echo - 数据库 campus_lost_found 已创建  
echo - 数据库密码为 123456
echo.
echo 应用将在 http://localhost:18080 启动
echo API文档地址: http://localhost:18080/swagger-ui/index.html
echo 按 Ctrl+C 停止应用
echo.

call mvn spring-boot:run

echo.
echo 应用已停止
pause