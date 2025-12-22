@echo off
echo 开始编译项目...
cd /d "C:\Users\10717\Desktop\R4\BackEnd"
mvn clean compile
if %ERRORLEVEL% EQU 0 (
    echo 编译成功！
) else (
    echo 编译失败！
)
pause