PS C:\Users\User\Documents\plugintest\screenshotmetadata> .\gradlew.bat runClient

# Instructions for testing the enhanced metadata system:

## What's New:
1. **Dual Metadata Storage**: Your screenshots now get metadata in TWO ways:
   - PNG text chunks (embedded in the image file)
   - XMP sidecar files (separate .xmp files that Windows recognizes)

2. **File Explorer Visibility**: 
   - XMP files contain standard metadata that Windows File Explorer can read
   - Right-click any screenshot → Properties → Details to see metadata

## To Test:
1. Run: `.\gradlew.bat runClient`
2. Take a screenshot (F2 key)
3. Check the screenshots folder - you should see:
   - `2025-09-05_XX.XX.XX.png` (your screenshot)
   - `2025-09-05_XX.XX.XX.xmp` (metadata sidecar file)

## File Explorer Test:
1. Right-click the .png file → Properties → Details
2. You should see Title, Description, etc. populated with Minecraft data
3. The .xmp file can also be opened with any text editor to see the metadata

## What You'll See:
- **Title**: "Minecraft - [YourUsername]"
- **Description**: "Minecraft Screenshot - Player: [User] | World: [World] | Coords: (X, Y, Z) | Biome: [Biome]"
- **Creator**: Your Minecraft username
- **Subject**: "Minecraft Screenshot"

The XMP sidecar approach is more reliable for Windows File Explorer visibility than trying to embed standard EXIF data in PNG files.
