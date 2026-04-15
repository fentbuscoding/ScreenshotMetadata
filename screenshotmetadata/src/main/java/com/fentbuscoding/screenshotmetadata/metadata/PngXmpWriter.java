package com.fentbuscoding.screenshotmetadata.metadata;

import com.fentbuscoding.screenshotmetadata.ScreenshotMetadataMod;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;

/**
 * Embeds XMP metadata directly inside PNG files as an iTXt chunk with the
 * standard keyword {@code XML:com.adobe.xmp}. This format is read by Adobe
 * tools, Windows File Explorer, and other mods such as screenshot-manager-enhanced.
 */
public class PngXmpWriter {

    private static final String XMP_ITXT_KEYWORD = "XML:com.adobe.xmp";

    /**
     * Embeds XMP metadata into a PNG file.
     *
     * @param file     The PNG file to add embedded XMP to
     * @param metadata Map of key-value metadata pairs
     * @throws IOException if file operations fail
     */
    public static void writeMetadata(File file, Map<String, String> metadata) throws IOException {
        if (file == null || metadata == null) {
            throw new IllegalArgumentException("File and metadata must not be null");
        }
        if (!file.exists() || !file.getName().toLowerCase().endsWith(".png")) {
            throw new IllegalArgumentException("File must be an existing PNG file: " + file.getPath());
        }
        if (metadata.isEmpty()) {
            ScreenshotMetadataMod.LOGGER.debug("No metadata provided for {} - skipping embedded XMP", file.getName());
            return;
        }

        ScreenshotMetadataMod.LOGGER.debug("Writing embedded XMP to: {} ({} entries)", file.getName(), metadata.size());

        Path tempPath = Files.createTempFile(file.getParentFile().toPath(), file.getName(), ".tmp");
        boolean moved = false;

        ImageWriter writer = null;
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                throw new IOException("Could not read image data from: " + file.getName());
            }

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
            if (!writers.hasNext()) {
                throw new IOException("No PNG writer available");
            }
            writer = writers.next();

            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            IIOMetadata meta = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), writeParam);
            String nativeFormat = meta.getNativeMetadataFormatName();

            if (nativeFormat == null || nativeFormat.isBlank()) {
                throw new IOException("PNG metadata format is unavailable");
            }

            String xmpPacket = buildXmpPacket(metadata);
            if (!mergeXmpChunk(meta, nativeFormat, xmpPacket)) {
                throw new IOException("Could not merge XMP iTXt chunk into PNG metadata");
            }

            try (ImageOutputStream output = ImageIO.createImageOutputStream(tempPath.toFile())) {
                writer.setOutput(output);
                writer.write(null, new IIOImage(image, null, meta), writeParam);
            }

            try {
                Files.move(tempPath, file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException atomicFailure) {
                Files.move(tempPath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;

        } catch (Exception e) {
            String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            ScreenshotMetadataMod.LOGGER.debug("Failed to write embedded XMP to {}: {}", file.getName(), reason, e);
            throw new IOException("Failed to write embedded XMP: " + reason, e);
        } finally {
            if (writer != null) {
                writer.dispose();
            }
            if (!moved) {
                try {
                    Files.deleteIfExists(tempPath);
                } catch (IOException cleanupFailure) {
                    ScreenshotMetadataMod.LOGGER.warn("Could not delete temp file {}: {}", tempPath, cleanupFailure.getMessage());
                }
            }
        }

        ScreenshotMetadataMod.LOGGER.debug("Successfully wrote embedded XMP to: {}", file.getName());
    }

    /**
     * Builds a complete XMP packet from the metadata map.
     */
    private static String buildXmpPacket(Map<String, String> metadata) {
        StringBuilder xmp = new StringBuilder();
        xmp.append("<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n");
        xmp.append("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n");
        xmp.append(" <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
        xmp.append("          xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n");
        xmp.append("          xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\"\n");
        xmp.append("          xmlns:mc=\"http://fentbuscoding.com/minecraft/ns/\">\n");
        xmp.append("  <rdf:Description rdf:about=\"\">\n");

        addDublinCoreFields(xmp, metadata);
        addXmpBasicFields(xmp, metadata);
        addMinecraftFields(xmp, metadata);

        xmp.append("  </rdf:Description>\n");
        xmp.append(" </rdf:RDF>\n");
        xmp.append("</x:xmpmeta>\n");
        xmp.append("<?xpacket end=\"w\"?>");
        return xmp.toString();
    }

    private static void addDublinCoreFields(StringBuilder xmp, Map<String, String> metadata) {
        String title = "Minecraft - " + metadata.getOrDefault("Username", "Unknown Player");
        xmp.append("   <dc:title>").append(escapeXml(title)).append("</dc:title>\n");

        StringBuilder description = new StringBuilder("Minecraft Screenshot");
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

        String creator = metadata.getOrDefault("Username", "Unknown Player");
        xmp.append("   <dc:creator>").append(escapeXml(creator)).append("</dc:creator>\n");

        String subject = "Minecraft Screenshot";
        if (metadata.containsKey("Tags")) {
            String tags = metadata.get("Tags");
            if (tags != null && !tags.isBlank()) {
                subject = subject + ", " + tags;
            }
        }
        xmp.append("   <dc:subject>").append(escapeXml(subject)).append("</dc:subject>\n");
        xmp.append("   <dc:type>Image</dc:type>\n");
    }

    private static void addXmpBasicFields(StringBuilder xmp, Map<String, String> metadata) {
        xmp.append("   <xmp:CreatorTool>Screenshot Metadata Mod v")
                .append(escapeXml(ScreenshotMetadataMod.MOD_VERSION))
                .append("</xmp:CreatorTool>\n");

        if (metadata.containsKey("Timestamp")) {
            try {
                Instant timestamp = Instant.parse(metadata.get("Timestamp"));
                String formattedDate = DateTimeFormatter.ISO_INSTANT.format(timestamp);
                xmp.append("   <xmp:CreateDate>").append(formattedDate).append("</xmp:CreateDate>\n");
                xmp.append("   <xmp:ModifyDate>").append(formattedDate).append("</xmp:ModifyDate>\n");
            } catch (Exception e) {
                ScreenshotMetadataMod.LOGGER.debug("Could not parse timestamp: {}", metadata.get("Timestamp"));
            }
        }
    }

    private static void addMinecraftFields(StringBuilder xmp, Map<String, String> metadata) {
        appendIfPresent(xmp, "mc:world", metadata, "World");
        appendIfPresent(xmp, "mc:dimension", metadata, "Dimension");
        appendIfPresent(xmp, "mc:biome", metadata, "Biome");

        if (metadata.containsKey("X") && metadata.containsKey("Y") && metadata.containsKey("Z")) {
            String coords = metadata.get("X") + ", " + metadata.get("Y") + ", " + metadata.get("Z");
            xmp.append("   <mc:coordinates>").append(escapeXml(coords)).append("</mc:coordinates>\n");
            xmp.append("   <mc:x>").append(escapeXml(metadata.get("X"))).append("</mc:x>\n");
            xmp.append("   <mc:y>").append(escapeXml(metadata.get("Y"))).append("</mc:y>\n");
            xmp.append("   <mc:z>").append(escapeXml(metadata.get("Z"))).append("</mc:z>\n");
        }

        appendIfPresent(xmp, "mc:player", metadata, "Username");
        appendIfPresent(xmp, "mc:worldName", metadata, "WorldName");
        appendIfPresent(xmp, "mc:weather", metadata, "Weather");
        appendIfPresent(xmp, "mc:difficulty", metadata, "Difficulty");
        appendIfPresent(xmp, "mc:gameMode", metadata, "GameMode");
        appendIfPresent(xmp, "mc:minecraftVersion", metadata, "MinecraftVersion");
        appendIfPresent(xmp, "mc:serverType", metadata, "ServerType");

        if (metadata.containsKey("Tags")) {
            String tags = metadata.get("Tags");
            if (tags != null && !tags.isBlank()) {
                xmp.append("   <mc:tags>").append(escapeXml(tags)).append("</mc:tags>\n");
            }
        }
    }

    private static void appendIfPresent(StringBuilder xmp, String element, Map<String, String> metadata, String key) {
        if (metadata.containsKey(key)) {
            String value = metadata.get(key);
            if (value != null && !value.isBlank()) {
                xmp.append("   <").append(element).append(">")
                        .append(escapeXml(value))
                        .append("</").append(element).append(">\n");
            }
        }
    }

    /**
     * Merges the XMP packet as an iTXt chunk into the PNG metadata tree.
     */
    private static boolean mergeXmpChunk(IIOMetadata meta, String nativeFormat, String xmpPacket) {
        try {
            IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(nativeFormat);
            IIOMetadataNode iTXtNode = new IIOMetadataNode("iTXt");
            IIOMetadataNode entry = new IIOMetadataNode("iTXtEntry");

            entry.setAttribute("keyword", XMP_ITXT_KEYWORD);
            entry.setAttribute("compressionFlag", "FALSE");
            entry.setAttribute("compressionMethod", "0");
            entry.setAttribute("languageTag", "");
            entry.setAttribute("translatedKeyword", "");
            entry.setAttribute("text", xmpPacket);

            iTXtNode.appendChild(entry);
            root.appendChild(iTXtNode);
            meta.mergeTree(nativeFormat, root);
            return true;
        } catch (Exception e) {
            ScreenshotMetadataMod.LOGGER.debug("XMP iTXt merge failed: {}", e.getMessage());
            return false;
        }
    }

    private static String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
