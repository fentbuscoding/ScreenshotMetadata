# Screenshot Metadata Mod - Publishing Script

Write-Host "Screenshot Metadata Mod - Publishing Script" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "Checking build..." -ForegroundColor Yellow
& .\gradlew.bat clean build

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed! Please fix errors before publishing." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "Build successful!" -ForegroundColor Green
Write-Host ""
Write-Host "Publishing to CurseForge and Modrinth..." -ForegroundColor Yellow
Write-Host "Make sure you have set your API tokens:" -ForegroundColor Magenta
Write-Host "- CURSEFORGE_TOKEN" -ForegroundColor Magenta
Write-Host "- MODRINTH_TOKEN" -ForegroundColor Magenta
Write-Host ""

# Check if tokens are set
if (-not $env:CURSEFORGE_TOKEN) {
    Write-Host "WARNING: CURSEFORGE_TOKEN not set!" -ForegroundColor Red
}
if (-not $env:MODRINTH_TOKEN) {
    Write-Host "WARNING: MODRINTH_TOKEN not set!" -ForegroundColor Red
}

Read-Host "Press Enter to continue with publishing"

& .\gradlew.bat publishMods

if ($LASTEXITCODE -ne 0) {
    Write-Host "Publishing failed! Check your tokens and project IDs." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "    Publishing completed! 🎉" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""
Write-Host "Your mod has been published to:" -ForegroundColor Cyan
Write-Host "- CurseForge" -ForegroundColor Cyan
Write-Host "- Modrinth" -ForegroundColor Cyan
Write-Host ""
Write-Host "Don't forget to:" -ForegroundColor Yellow
Write-Host "1. Add screenshots to your project pages" -ForegroundColor Yellow
Write-Host "2. Update project descriptions" -ForegroundColor Yellow
Write-Host "3. Share your release on social media" -ForegroundColor Yellow
Write-Host ""
Read-Host "Press Enter to exit"
