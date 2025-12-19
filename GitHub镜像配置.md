# GitHub镜像配置和推送方案

## 🚀 **常用GitHub镜像源**

### 1. **GitHub官方镜像**
```bash
# 原地址
https://github.com/chandou-code/dabian.git

# 镜像地址
https://github.com.cnpmjs.org/chandou-code/dabian.git
```

### 2. **GitClone镜像**
```bash
https://gitclone.xyz/github.com/chandou-code/dabian.git
```

### 3. **HubFast镜像**
```bash
https://hub.fastgit.xyz/chandou-code/dabian.git
```

### 4. **Gitee导入**
```bash
# 先推送到Gitee，再同步到GitHub
https://gitee.com/chandou-code/dabian.git
```

## 🔧 **配置方法**

### 方法一：切换远程地址
```bash
cd C:\Users\10717\Desktop\R4

# 使用cnpmjs镜像
"C:\Program Files\Git\bin\git.exe" remote set-url origin https://github.com.cnpmjs.org/chandou-code/dabian.git

# 推送
"C:\Program Files\Git\bin\git.exe" push -u origin main
```

### 方法二：使用SSH免密钥
```bash
# 1. 生成SSH密钥
"C:\Program Files\Git\bin\ssh-keygen.exe" -t rsa -b 4096 -C "1071718696@qq.com"

# 2. 添加到GitHub
# 复制 C:\Users\10717\.ssh\id_rsa.pub 内容到GitHub设置

# 3. 使用SSH地址
"C:\Program Files\Git\bin\git.exe" remote set-url origin git@github.com:chandou-code/dabian.git

# 4. 测试连接
"C:\Program Files\Git\bin\ssh.exe" -T git@github.com

# 5. 推送
"C:\Program Files\Git\bin\git.exe" push -u origin main
```

### 方法三：手动镜像脚本
```bash
# 创建推送脚本
@echo off
echo 尝试GitHub镜像推送...

# 尝试镜像1
"C:\Program Files\Git\bin\git.exe" remote set-url origin https://github.com.cnpmjs.org/chandou-code/dabian.git
"C:\Program Files\Git\bin\git.exe" push -u origin main
if %errorlevel% == 0 (
    echo 推送成功！
    pause
    exit
)

# 尝试镜像2
"C:\Program Files\Git\bin\git.exe" remote set-url origin https://gitclone.xyz/github.com/chandou-code/dabian.git
"C:\Program Files\Git\bin\git.exe" push -u origin main
if %errorlevel% == 0 (
    echo 推送成功！
    pause
    exit
)

# 尝试镜像3
"C:\Program Files\Git\bin\git.exe" remote set-url origin https://hub.fastgit.xyz/chandou-code/dabian.git
"C:\Program Files\Git\bin\git.exe" push -u origin main
if %errorlevel% == 0 (
    echo 推送成功！
    pause
    exit
)

echo 所有镜像都失败了，请检查网络
pause
```

## 🛠️ **快速推送命令**

选择一个镜像源执行：

```bash
# 镜像1：cnpmjs
cd C:\Users\10717\Desktop\R4
"C:\Program Files\Git\bin\git.exe" remote set-url origin https://github.com.cnpmjs.org/chandou-code/dabian.git
"C:\Program Files\Git\bin\git.exe" push -u origin main

# 镜像2：gitclone
"C:\Program Files\Git\bin\git.exe" remote set-url origin https://gitclone.xyz/github.com/chandou-code/dabian.git
"C:\Program Files\Git\bin\git.exe" push -u origin main

# 镜像3：hubfast
"C:\Program Files\Git\bin\git.exe" remote set-url origin https://hub.fastgit.xyz/chandou-code/dabian.git
"C:\Program Files\Git\bin\git.exe" push -u origin main
```

## 📋 **推荐方案**

### 最快方案：使用cnpmjs镜像
```bash
cd C:\Users\10717\Desktop\R4
"C:\Program Files\Git\bin\git.exe" remote set-url origin https://github.com.cnpmjs.org/chandou-code/dabian.git
"C:\Program Files\Git\bin\git.exe" push -u origin main
```

### 最稳方案：Gitee中转
1. 创建Gitee仓库
2. 推送到Gitee
3. 在Gitee设置中开启GitHub同步

## ✅ **推送后验证**
```bash
# 切换回原地址（可选）
"C:\Program Files\Git\bin\git.exe" remote set-url origin https://github.com/chandou-code/dabian.git

# 查看远程地址
"C:\Program Files\Git\bin\git.exe" remote -v
```

---

**选择任意一个镜像源执行推送命令即可！**