@echo off
echo 测试 Java 编译器...
cd /d "C:\Users\10717\Desktop\R4\BackEnd"
echo 当前目录: %CD%
echo.

echo 检查文件是否存在:
if exist "src\main\java\com\campus\lostfound\controller\StatisticsController.java" (
    echo ✓ StatisticsController.java 存在
) else (
    echo ✗ StatisticsController.java 不存在
)

echo.
echo 尝试直接使用 javac 测试语法（仅测试，不完整编译）:
javac -cp "target\classes" "src\main\java\com\campus\lostfound\controller\StatisticsController.java" 2>&1
echo.

echo 编译测试完成
pause