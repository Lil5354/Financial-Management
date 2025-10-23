@echo off
echo ========================================
echo    NoNo Expense Tracker - Cleanup Script
echo ========================================
echo.

echo [1/5] Cleaning build directories...
if exist "build\" (
    rmdir /s /q "build\"
    echo ✓ Removed build/ directory
) else (
    echo - build/ directory not found
)

if exist "app\build\" (
    rmdir /s /q "app\build\"
    echo ✓ Removed app/build/ directory
) else (
    echo - app/build/ directory not found
)

echo.
echo [2/5] Cleaning cache files...
if exist ".DS_Store" (
    del ".DS_Store"
    echo ✓ Removed .DS_Store
) else (
    echo - .DS_Store not found
)

if exist "Quy trình thiết kế app\.DS_Store" (
    del "Quy trình thiết kế app\.DS_Store"
    echo ✓ Removed Quy trình thiết kế app/.DS_Store
) else (
    echo - Quy trình thiết kế app/.DS_Store not found
)

echo.
echo [3/5] Cleaning temporary files...
del /q *.tmp 2>nul
del /q *.temp 2>nul
del /q *.log 2>nul
echo ✓ Removed temporary files

echo.
echo [4/5] Checking project size...
for /f "tokens=3" %%a in ('dir /s /-c ^| find "File(s)"') do set size=%%a
echo Current project size: %size% bytes

echo.
echo [5/5] Cleanup completed!
echo ========================================
echo.
echo Next steps:
echo 1. Run: git add .
echo 2. Run: git commit -m "Clean project"
echo 3. Push to GitHub
echo.
pause
