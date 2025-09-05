# Testing Instructions for Screenshot Metadata Mod v1.0.0

## 🎯 **Quick Test**

1. **Build and Run**:
   ```powershell
   .\gradlew.bat clean build
   .\gradlew.bat runClient
   ```

2. **Take a Screenshot**: Press F2 in-game

3. **Check Results**: Navigate to `run\screenshots\` folder

## 📁 **What You Should See**

For each screenshot, you'll find:
- `2025-XX-XX_XX.XX.XX.png` - Your screenshot with embedded metadata
- `2025-XX-XX_XX.XX.XX.xmp` - Metadata sidecar file

## 🔍 **Verification Methods**

### **Method 1: File Explorer (Recommended)**
1. Right-click the PNG file
2. Select **Properties** → **Details** tab
3. Look for:
   - **Title**: "Minecraft - [Username]"
   - **Comments**: Full coordinate and world info
   - **Authors**: Your Minecraft username

### **Method 2: PowerShell Script**
```powershell
.\check_metadata.ps1 "path\to\screenshot.png"
```

### **Method 3: XMP File (Text Editor)**
Open the `.xmp` file in any text editor to see structured metadata.

## 🎮 **Expected Metadata**

### **Standard Fields (File Explorer Visible)**
- **Title**: Minecraft - Player900
- **Description**: Minecraft Screenshot - Player: Player900 | World: minecraft:overworld | Coords: (122, 80, 172) | Biome: Forest
- **Creator**: Player900
- **Subject**: Minecraft Screenshot
- **Software**: Screenshot Metadata Mod v1.0.0

### **Technical Fields (PNG Text Chunks)**
- Username
- X, Y, Z coordinates
- World (dimension)
- Biome (cleaned format)
- Timestamp (ISO format)
- MinecraftVersion
- ModVersion

## 🚨 **Troubleshooting**

### **No Metadata Visible**
1. Check console logs for errors
2. Verify mod loaded: Look for "Screenshot Metadata v1.0.0 initialized!" message
3. Ensure you're checking the newest screenshot file

### **XMP Parsing Errors**
- XMP files should have proper XML namespace declarations
- Check for malformed XML with any XML validator

### **Performance Issues**
- Metadata processing is async and shouldn't affect game performance
- Check logs for any processing errors

## 📝 **Console Messages**

You should see these in the game console:
```
[INFO] Screenshot Metadata v1.0.0 initialized! Screenshots will now include comprehensive metadata.
[INFO] Processing screenshot metadata...
[INFO] Successfully added metadata to screenshot: 2025-XX-XX_XX.XX.XX.png
[DEBUG] Writing PNG metadata to: 2025-XX-XX_XX.XX.XX.png
[DEBUG] Created XMP sidecar file: 2025-XX-XX_XX.XX.XX.xmp
```

## 🎯 **Success Criteria**

✅ **Mod loads without errors**  
✅ **Screenshots taken normally (F2 works)**  
✅ **XMP files created alongside PNGs**  
✅ **Metadata visible in File Explorer Properties**  
✅ **No game performance impact**  
✅ **Clean, readable biome names**  

## 🔧 **Advanced Testing**

### **Different Biomes**
Test in various biomes to ensure proper name formatting.

### **Different Dimensions**
Test in Nether, End, and custom dimensions.

### **Multiple Screenshots**
Take several screenshots quickly to test async processing.

### **Long Gaming Sessions**
Verify metadata accuracy over extended play periods.

---

**Report any issues with full console logs and steps to reproduce!**
