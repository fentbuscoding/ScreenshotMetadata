@echo off
echo Screenshot Metadata Mod - Publishing Script
echo ==========================================

echo.
echo Checking build...
call gradlew.bat clean build
if errorlevel 1 (
    echo Build failed! Please fix errors before publishing.
    pause
    exit /b 1
)

echo.
echo Build successful! 
echo.
echo Publishing to CurseForge and Modrinth...
echo Make sure you have set your API tokens:
echo - CURSEFORGE_TOKEN
echo - MODRINTH_TOKEN
echo.

pause

call gradlew.bat publishMods
if errorlevel 1 (
    echo Publishing failed! Check your tokens and project IDs.
    pause
    exit /b 1
)

echo.
echo ================================
echo    Publishing completed! 🎉
echo ================================
echo.
echo Your mod has been published to:
echo - CurseForge
echo - Modrinth
echo.
echo Don't forget to:
echo 1. Add screenshots to your project pages
echo 2. Update project descriptions
echo 3. Share your release on social media
echo.
pause
