package com.fentbuscoding.screenshotmetadata.metadata;

import com.fentbuscoding.screenshotmetadata.ScreenshotMetadataMod;
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

/**
 * Handles writing metadata to PNG files using ImageIO's text chunks.
 * This metadata is embedded directly in the PNG file and can be read by technical tools.
 */
public class PngMetadataWriter {
    
    /**
     * Writes metadata to a PNG file as text chunks.
     * Creates a temporary file and replaces the original to ensure data integrity.
     * 
     * @param file The PNG file to add metadata to
     * @param metadata Map of key-value pairs to embed
     * @throws IOException if file operations fail
     */
    public static void writeMetadata(File file, Map<String, String> metadata) throws IOException {
        if (!file.exists() || !file.getName().toLowerCase().endsWith(".png")) {
            throw new IllegalArgumentException("File must be an existing PNG file: " + file.getPath());
        }
        
        ScreenshotMetadataMod.LOGGER.debug("Writing PNG metadata to: {}", file.getName());
        
        BufferedImage image = null;
        ImageWriter writer = null;
        FileImageOutputStream output = null;
        File tempFile = null;
        
        try {
            // Load original image
            image = ImageIO.read(file);
            if (image == null) {
                throw new IOException("Could not read image data from: " + file.getName());
            }
            
            // Prepare PNG writer
            writer = ImageIO.getImageWritersByFormatName("png").next();
            if (writer == null) {
                throw new IOException("No PNG writer available");
            }
            
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            IIOMetadata meta = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), writeParam);
            
            // Build metadata tree with PNG text chunks
            String nativeFormat = meta.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(nativeFormat);
            IIOMetadataNode textNode = new IIOMetadataNode("tEXt");
            
            // Add individual metadata entries
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    IIOMetadataNode textEntry = new IIOMetadataNode("tEXtEntry");
                    textEntry.setAttribute("keyword", entry.getKey());
                    textEntry.setAttribute("value", entry.getValue());
                    textNode.appendChild(textEntry);
                }
            }
            
            // Add comprehensive summary entries for better tool compatibility
            addStandardTextEntries(textNode, metadata);
            
            root.appendChild(textNode);
            meta.mergeTree(nativeFormat, root);
            
            // Write to temporary file first
            tempFile = new File(file.getParent(), file.getName() + ".tmp");
            output = new FileImageOutputStream(tempFile);
            writer.setOutput(output);
            writer.write(meta, new IIOImage(image, null, meta), writeParam);
            
        } catch (Exception e) {
            ScreenshotMetadataMod.LOGGER.error("Failed to write PNG metadata to {}: {}", file.getName(), e.getMessage());
            throw new IOException("Failed to write PNG metadata", e);
        } finally {
            // Clean up resources
            if (output != null) {
                try { output.close(); } catch (IOException ignored) {}
            }
            if (writer != null) {
                writer.dispose();
            }
        }
        
        // Atomically replace original file
        if (!file.delete() || !tempFile.renameTo(file)) {
            if (tempFile.exists()) tempFile.delete();
            throw new IOException("Could not replace original PNG file: " + file.getName());
        }
        
        ScreenshotMetadataMod.LOGGER.debug("Successfully wrote PNG metadata to: {}", file.getName());
    }
    
    /**
     * Adds standard text entries that various tools might recognize
     */
    private static void addStandardTextEntries(IIOMetadataNode textNode, Map<String, String> metadata) {
        // Build comprehensive description
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
        
        // Add standard entries
        addTextEntry(textNode, "Comment", description.toString());
        addTextEntry(textNode, "Description", description.toString());
        addTextEntry(textNode, "Title", "Minecraft - " + metadata.getOrDefault("Username", "Unknown Player"));
        addTextEntry(textNode, "Software", "Screenshot Metadata Mod v1.0.0");
        addTextEntry(textNode, "Author", metadata.getOrDefault("Username", "Unknown Player"));
    }
    
    /**
     * Helper method to add a text entry
     */
    private static void addTextEntry(IIOMetadataNode textNode, String keyword, String value) {
        if (keyword != null && value != null && !value.trim().isEmpty()) {
            IIOMetadataNode textEntry = new IIOMetadataNode("tEXtEntry");
            textEntry.setAttribute("keyword", keyword);
            textEntry.setAttribute("value", value.trim());
            textNode.appendChild(textEntry);
        }
    }
}
