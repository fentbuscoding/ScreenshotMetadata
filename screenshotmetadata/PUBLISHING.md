# Publishing Guide for Screenshot Metadata Mod

## 🚀 Quick Publishing Steps

### **1. Set Up Project IDs**

#### **Create CurseForge Project**
1. Go to [CurseForge Console](https://console.curseforge.com/)
2. Create new project for Minecraft
3. Set category to "Utility"
4. Note down your project ID (number in URL)
5. Update `gradle.properties`: `curseforge_id=YOUR_PROJECT_ID`

#### **Create Modrinth Project**  
1. Go to [Modrinth](https://modrinth.com/dashboard/projects)
2. Create new project
3. Set category to "Utility" 
4. Note down your project slug/ID
5. Update `gradle.properties`: `modrinth_id=YOUR_PROJECT_SLUG`

### **2. Get API Tokens**

#### **CurseForge Token**
1. Go to [CurseForge API Tokens](https://console.curseforge.com/api-tokens)
2. Create new token with "Upload" permission
3. Set environment variable: `CURSEFORGE_TOKEN=your_token_here`

#### **Modrinth Token**
1. Go to [Modrinth Settings](https://modrinth.com/settings/account)
2. Create new API token with "Write" permission  
3. Set environment variable: `MODRINTH_TOKEN=your_token_here`

## 🔧 **Environment Variables Setup**

### **Windows (PowerShell)**
```powershell
# Set environment variables (replace with your actual tokens)
$env:CURSEFORGE_TOKEN = "your_curseforge_token_here"
$env:MODRINTH_TOKEN = "your_modrinth_token_here"

# Or set permanently
[Environment]::SetEnvironmentVariable("CURSEFORGE_TOKEN", "your_token", "User")
[Environment]::SetEnvironmentVariable("MODRINTH_TOKEN", "your_token", "User")
```

### **Linux/Mac (Bash)**
```bash
export CURSEFORGE_TOKEN="your_curseforge_token_here"
export MODRINTH_TOKEN="your_modrinth_token_here"

# Or add to ~/.bashrc for permanent setup
echo 'export CURSEFORGE_TOKEN="your_token"' >> ~/.bashrc
echo 'export MODRINTH_TOKEN="your_token"' >> ~/.bashrc
```

## 📦 **Publishing Commands**

### **Publish to Both Platforms**
```powershell
.\gradlew.bat publishMods
```

### **Publish to CurseForge Only**
```powershell
.\gradlew.bat publishCurseforge
```

### **Publish to Modrinth Only**
```powershell
.\gradlew.bat publishModrinth
```

### **Build Only (No Publishing)**
```powershell
.\gradlew.bat build
```

## 📋 **Pre-Publishing Checklist**

- [ ] Update version in `gradle.properties`
- [ ] Update changelog in `CHANGELOG.md`
- [ ] Test the mod thoroughly
- [ ] Set CurseForge project ID in `gradle.properties`
- [ ] Set Modrinth project ID in `gradle.properties`
- [ ] Set environment variables for API tokens
- [ ] Verify build works: `.\gradlew.bat build`
- [ ] Review mod description and metadata

## 🎯 **Project Configuration**

### **CurseForge Settings**
- **Category**: Utility
- **Environment**: Client
- **Game Version**: 1.21.8
- **Mod Loader**: Fabric
- **Dependencies**: Fabric API (required)

### **Modrinth Settings**
- **Category**: Utility  
- **Environment**: Client
- **Game Version**: 1.21.8
- **Mod Loader**: Fabric
- **Dependencies**: Fabric API (required)

## 📝 **Version Management**

### **Update Version**
1. Edit `gradle.properties`: `mod_version=1.0.1`
2. Update `CHANGELOG.md` with new changes
3. Commit changes to git
4. Run publish command

### **Release Types**
- **STABLE**: For tested, production-ready releases
- **BETA**: For feature-complete but testing releases  
- **ALPHA**: For early development releases

## 🔍 **Troubleshooting**

### **Common Issues**
- **401 Unauthorized**: Check API tokens
- **403 Forbidden**: Verify project permissions
- **404 Not Found**: Check project IDs
- **Build Failed**: Run `.\gradlew.bat clean build`

### **Verify Setup**
```powershell
# Check environment variables
echo $env:CURSEFORGE_TOKEN
echo $env:MODRINTH_TOKEN

# Test build
.\gradlew.bat clean build
```

## 🎉 **After Publishing**

1. **Verify Upload**: Check both platforms for your mod
2. **Update Descriptions**: Add screenshots and detailed descriptions
3. **Social Media**: Share your mod release
4. **Monitor**: Watch for user feedback and bug reports

---

**Ready to publish your Screenshot Metadata Mod! 🚀**
