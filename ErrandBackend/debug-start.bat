@echo off
echo 校园跑腿服务系统 - 调试启动脚本
echo.

REM 进入项目目录
cd /d "C:\Users\10717\Desktop\R4\ErrandBackend"
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
echo 请确保：
echo - MySQL服务已启动
echo - 数据库 campus_errand 已创建  
echo - 数据库密码为 123456
echo.
echo 应用将在 http://localhost:18083/api 启动
echo 按 Ctrl+C 停止应用
echo.

call mvn spring-boot:run -Dspring-boot.run.main-class=com.campus.errand.ErrandsApplication

pause