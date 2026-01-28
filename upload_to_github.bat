@echo off
chcp 65001 >nul

echo ====================================
echo 🚀 校园失物招领系统 - GitHub一键上传
echo ====================================
echo.

REM 检查Git是否存在
where git >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ 错误：未找到Git命令！请确保Git已安装并添加到环境变量。
    pause
    exit /b 1
)

echo 📁 当前工作目录：%cd%
echo.

REM 设置Git路径（使用完整路径确保兼容性）
set GIT_CMD="C:\Program Files\Git\bin\git.exe"

REM 设置仓库信息
set REPO_URL=https://github.com/chandou-code/dabian.git
set BRANCH=main

REM 1. 设置远程仓库地址
echo 🔗 设置远程仓库地址...
%GIT_CMD% remote set-url origin %REPO_URL% 2>nul
if %errorlevel% neq 0 (
    echo ⚠️  警告：设置远程仓库地址失败，可能已存在
)

REM 2. 检查Git状态
echo.
echo 📋 检查Git状态...
%GIT_CMD% status

REM 3. 添加所有更改到暂存区
echo.
echo ➕ 添加所有更改到暂存区...
%GIT_CMD% add .
if %errorlevel% neq 0 (
    echo ❌ 错误：添加文件失败！
    pause
    exit /b 1
)

REM 4. 提交更改
echo.
echo 💬 提交更改...
set COMMIT_MSG=更新校园失物招领系统：修复图片显示问题，完善前后端功能，优化用户体验
%GIT_CMD% commit -m "%COMMIT_MSG%"
if %errorlevel% neq 0 (
    echo ❌ 错误：提交失败！可能没有需要提交的更改。
    pause
    exit /b 1
)

REM 5. 强制推送到GitHub
echo.
echo 🚀 推送代码到GitHub...
%GIT_CMD% push -f origin %BRANCH%
if %errorlevel% neq 0 (
    echo ❌ 错误：推送失败！请检查网络连接或GitHub权限。
    pause
    exit /b 1
)

echo.
echo ✅ 上传成功！
echo 📌 仓库地址：%REPO_URL%
echo 📌 分支：%BRANCH%
echo.
echo 🎉 项目已成功上传到GitHub！
echo ====================================
pause
