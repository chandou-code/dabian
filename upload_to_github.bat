@echo off
echo Starting GitHub upload process...
cd /d "c:\Users\10717\Desktop\R4"

echo Checking Git status...
"C:\Program Files\Git\bin\git.exe" status

echo Checking remote repositories...
"C:\Program Files\Git\bin\git.exe" remote -v

echo Checking recent commits...
"C:\Program Files\Git\bin\git.exe" log --oneline -3

echo Adding remote origin if not exists...
"C:\Program Files\Git\bin\git.exe" remote add origin https://github.com/chandou-code/dabian.git 2>nul

echo Pushing to GitHub...
"C:\Program Files\Git\bin\git.exe" push -u origin main

echo Upload process completed!
pause