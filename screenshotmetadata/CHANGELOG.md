# Changelog

## [1.0.0] - 2025-09-06

### Added
- Initial release of Screenshot Metadata Mod
- Comprehensive metadata collection for Minecraft screenshots
- Dual metadata storage: PNG text chunks and XMP sidecar files
- File Explorer integration for Windows users
- Player information metadata (username, coordinates)
- World and environment data (dimension, biome)
- Timestamp and version information
- Async processing to prevent game performance impact
- Professional code structure with proper error handling
- Support for Minecraft 1.21.8 with Fabric

### Features
- **Automatic Metadata Addition**: No user action required, works automatically with F2 screenshots
- **File Explorer Visibility**: Metadata appears in Windows Properties → Details
- **Comprehensive Data**: Player name, coordinates (X,Y,Z), world/dimension, biome, timestamp
- **Dual Format Support**: Both embedded PNG metadata and XMP sidecar files
- **Clean Biome Names**: User-friendly biome names instead of technical references
- **Performance Optimized**: Async processing with robust error handling
- **Standards Compliant**: Proper XMP format with Dublin Core metadata

### Technical Details
- **Package**: com.fentbuscoding.screenshotmetadata
- **Minecraft Version**: 1.21.8
- **Fabric Loader**: 0.16.0+
- **Java Version**: 21+
- **Dependencies**: Fabric API

### Requirements
- Minecraft 1.21.8
- Fabric Loader 0.16.0 or higher
- Fabric API
- Java 21 or higher

### Installation
1. Download the mod JAR file
2. Place in your Minecraft mods folder
3. Launch Minecraft with Fabric
4. Take screenshots as normal (F2 key)
5. Check File Explorer properties to see metadata!
