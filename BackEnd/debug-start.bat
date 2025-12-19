@echo off
echo 调试启动脚本
echo.

REM 进入项目目录
cd /d "C:\Users\10717\Desktop\R4\BackEnd"
echo 当前目录: %CD%

REM 检查Java
echo.
echo 检查Java版本:
java -version

REM 检查Maven
echo.
echo 检查Maven版本:
call mvn -version

REM 编译
echo.
echo 开始编译...
call mvn clean compile

REM 检查编译结果
if %errorlevel% neq 0 (
    echo 编译失败！
    pause
    exit /b 1
) else (
    echo 编译成功！
)

REM 启动
echo.
echo 启动应用...
call mvn spring-boot:run

pause