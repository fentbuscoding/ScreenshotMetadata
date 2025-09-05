package com.example.screenshotmetadata;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class PngMetadataWriter {
    public static void writeMetadata(File file, Map<String, String> metadata) throws IOException {
        // Load original image
        BufferedImage image = ImageIO.read(file);
        
        // Prepare writer
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        IIOMetadata meta = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), writeParam);
        
        // Add PNG text chunks - this is the most reliable method for PNG
        String nativeFormat = meta.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(nativeFormat);
        IIOMetadataNode textNode = new IIOMetadataNode("tEXt");
        
        // Add individual metadata entries
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            IIOMetadataNode textEntry = new IIOMetadataNode("tEXtEntry");
            textEntry.setAttribute("keyword", entry.getKey());
            textEntry.setAttribute("value", entry.getValue());
            textNode.appendChild(textEntry);
        }
        
        // Add a comprehensive summary that file explorers might pick up
        StringBuilder summary = new StringBuilder();
        summary.append("Minecraft Screenshot");
        if (metadata.containsKey("Username")) {
            summary.append(" - Player: ").append(metadata.get("Username"));
        }
        if (metadata.containsKey("World")) {
            summary.append(" | World: ").append(metadata.get("World"));
        }
        if (metadata.containsKey("X") && metadata.containsKey("Y") && metadata.containsKey("Z")) {
            summary.append(" | Coords: (")
                   .append(metadata.get("X")).append(", ")
                   .append(metadata.get("Y")).append(", ")
                   .append(metadata.get("Z")).append(")");
        }
        if (metadata.containsKey("Biome")) {
            summary.append(" | Biome: ").append(metadata.get("Biome"));
        }
        
        // Add common keywords that Windows might recognize
        IIOMetadataNode commentEntry = new IIOMetadataNode("tEXtEntry");
        commentEntry.setAttribute("keyword", "Comment");
        commentEntry.setAttribute("value", summary.toString());
        textNode.appendChild(commentEntry);
        
        IIOMetadataNode descriptionEntry = new IIOMetadataNode("tEXtEntry");
        descriptionEntry.setAttribute("keyword", "Description");
        descriptionEntry.setAttribute("value", summary.toString());
        textNode.appendChild(descriptionEntry);
        
        IIOMetadataNode titleEntry = new IIOMetadataNode("tEXtEntry");
        titleEntry.setAttribute("keyword", "Title");
        String title = "Minecraft - " + metadata.getOrDefault("Username", "Unknown Player");
        titleEntry.setAttribute("value", title);
        textNode.appendChild(titleEntry);
        
        IIOMetadataNode softwareEntry = new IIOMetadataNode("tEXtEntry");
        softwareEntry.setAttribute("keyword", "Software");
        softwareEntry.setAttribute("value", "Minecraft Screenshot Metadata Mod");
        textNode.appendChild(softwareEntry);
        
        root.appendChild(textNode);
        
        try {
            meta.mergeTree(nativeFormat, root);
        } catch (Exception e) {
            System.err.println("Failed to merge PNG metadata: " + e.getMessage());
            throw new IOException("Failed to merge PNG metadata", e);
        }
        
        // Write to temp file
        File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
        try (FileImageOutputStream output = new FileImageOutputStream(tmp)) {
            writer.setOutput(output);
            writer.write(meta, new IIOImage(image, null, meta), writeParam);
        } finally {
            writer.dispose();
        }
        
        // Replace original file
        if (!file.delete() || !tmp.renameTo(file)) {
            throw new IOException("Could not replace original PNG file");
        }
        
        System.out.println("Successfully wrote metadata to: " + file.getName());
    }
}
