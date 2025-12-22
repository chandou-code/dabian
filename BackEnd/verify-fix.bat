@echo off
echo ================================
echo 验证编译修复
echo ================================
cd /d "C:\Users\10717\Desktop\R4\BackEnd"

echo 当前目录: %CD%
echo Java 版本:
java -version
echo.

echo Maven 版本:
mvn -version
echo.

echo 开始编译...
mvn clean compile

echo.
echo ================================
echo 编译完成
echo ================================
pause