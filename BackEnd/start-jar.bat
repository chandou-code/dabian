@echo off
echo 启动校园失物招领系统 (JAR版本)
echo.

REM 进入项目目录
cd /d "C:\Users\10717\Desktop\R4\BackEnd"

REM 检查JAR文件是否存在
if not exist "target\lost-found-1.0.0.jar" (
    echo JAR文件不存在，正在构建...
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo 构建失败！
        pause
        exit /b 1
    )
)

echo.
echo 启动应用...
echo 应用将在 http://localhost:18080 启动
echo API文档地址: http://localhost:18080/swagger-ui/index.html
echo 按 Ctrl+C 停止应用
echo.

java -jar target\lost-found-1.0.0.jar

echo.
echo 应用已停止
pause