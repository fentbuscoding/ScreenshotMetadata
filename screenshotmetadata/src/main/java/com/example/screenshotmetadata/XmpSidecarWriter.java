package com.example.screenshotmetadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class XmpSidecarWriter {
    public static void writeSidecarFile(File imageFile, Map<String, String> metadata) {
        try {
            String baseName = imageFile.getName();
            int dotIndex = baseName.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = baseName.substring(0, dotIndex);
            }
            
            File xmpFile = new File(imageFile.getParent(), baseName + ".xmp");
            
            StringBuilder xmp = new StringBuilder();
            xmp.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xmp.append("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n");
            xmp.append(" <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n");
            xmp.append("  <rdf:Description rdf:about=\"\">\n");
            
            // Add basic XMP properties that Windows recognizes
            String title = "Minecraft - " + metadata.getOrDefault("Username", "Unknown Player");
            xmp.append("   <dc:title>").append(escapeXml(title)).append("</dc:title>\n");
            
            StringBuilder description = new StringBuilder();
            description.append("Minecraft Screenshot");
            if (metadata.containsKey("Username")) {
                description.append(" - Player: ").append(metadata.get("Username"));
            }
            if (metadata.containsKey("World")) {
                description.append(" | World: ").append(metadata.get("World"));
            }
            if (metadata.containsKey("X") && metadata.containsKey("Y") && metadata.containsKey("Z")) {
                description.append(" | Coords: (")
                          .append(metadata.get("X")).append(", ")
                          .append(metadata.get("Y")).append(", ")
                          .append(metadata.get("Z")).append(")");
            }
            if (metadata.containsKey("Biome")) {
                description.append(" | Biome: ").append(metadata.get("Biome"));
            }
            
            xmp.append("   <dc:description>").append(escapeXml(description.toString())).append("</dc:description>\n");
            xmp.append("   <dc:creator>").append(escapeXml(metadata.getOrDefault("Username", "Unknown Player"))).append("</dc:creator>\n");
            xmp.append("   <dc:subject>Minecraft Screenshot</dc:subject>\n");
            
            // Add custom namespace for Minecraft data
            xmp.append("   <minecraft:world>").append(escapeXml(metadata.getOrDefault("World", ""))).append("</minecraft:world>\n");
            xmp.append("   <minecraft:biome>").append(escapeXml(metadata.getOrDefault("Biome", ""))).append("</minecraft:biome>\n");
            xmp.append("   <minecraft:coordinates>")
               .append(escapeXml(metadata.getOrDefault("X", "") + "," + 
                               metadata.getOrDefault("Y", "") + "," + 
                               metadata.getOrDefault("Z", "")))
               .append("</minecraft:coordinates>\n");
            
            xmp.append("  </rdf:Description>\n");
            xmp.append(" </rdf:RDF>\n");
            xmp.append("</x:xmpmeta>\n");
            
            try (FileWriter writer = new FileWriter(xmpFile)) {
                writer.write(xmp.toString());
            }
            
            System.out.println("Created XMP sidecar file: " + xmpFile.getName());
            
        } catch (IOException e) {
            System.err.println("Failed to create XMP sidecar file: " + e.getMessage());
        }
    }
    
    private static String escapeXml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
