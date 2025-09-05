package com.fentbuscoding.screenshotmetadata.mixin;

import com.fentbuscoding.screenshotmetadata.ScreenshotMetadataMod;
import com.fentbuscoding.screenshotmetadata.metadata.PngMetadataWriter;
import com.fentbuscoding.screenshotmetadata.metadata.XmpSidecarWriter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Mixin to intercept screenshot saving and add comprehensive metadata.
 * Uses async processing to avoid blocking the game thread.
 */
@Mixin(ScreenshotRecorder.class)
public class ScreenshotRecorderMixin {
    
    @Inject(method = "saveScreenshot(Ljava/io/File;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V", 
            at = @At("TAIL"))
    private static void onScreenshotSaved(File gameDirectory, 
                                         net.minecraft.client.gl.Framebuffer framebuffer, 
                                         java.util.function.Consumer<net.minecraft.text.Text> messageReceiver, 
                                         CallbackInfo ci) {
        // Process metadata asynchronously to avoid blocking the game thread
        CompletableFuture.runAsync(() -> {
            try {
                processScreenshotMetadata(gameDirectory);
            } catch (Exception e) {
                ScreenshotMetadataMod.LOGGER.error("Unexpected error in screenshot metadata processing", e);
            }
        });
    }
    
    /**
     * Processes the screenshot metadata addition
     */
    private static void processScreenshotMetadata(File gameDirectory) {
        try {
            ScreenshotMetadataMod.LOGGER.debug("Processing screenshot metadata...");
            
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) {
                ScreenshotMetadataMod.LOGGER.warn("Cannot add metadata: client or player is null");
                return;
            }
            
            // Find the most recent screenshot file
            File screenshotFile = findNewestScreenshot(gameDirectory);
            if (screenshotFile == null) {
                ScreenshotMetadataMod.LOGGER.warn("No screenshot file found to add metadata to");
                return;
            }
            
            // Collect comprehensive metadata
            Map<String, String> metadata = collectMetadata(client);
            if (metadata.isEmpty()) {
                ScreenshotMetadataMod.LOGGER.warn("No metadata collected");
                return;
            }
            
            // Add metadata using both methods
            addMetadataToScreenshot(screenshotFile, metadata);
            
            ScreenshotMetadataMod.LOGGER.info("Successfully added metadata to screenshot: {}", screenshotFile.getName());
            
        } catch (Exception e) {
            ScreenshotMetadataMod.LOGGER.error("Failed to process screenshot metadata", e);
        }
    }
    
    /**
     * Finds the newest screenshot file in the screenshots directory
     */
    private static File findNewestScreenshot(File gameDirectory) {
        File screenshotsDir = new File(gameDirectory, "screenshots");
        if (!screenshotsDir.exists() || !screenshotsDir.isDirectory()) {
            ScreenshotMetadataMod.LOGGER.warn("Screenshots directory not found: {}", screenshotsDir.getPath());
            return null;
        }
        
        File[] files = screenshotsDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".png") && !name.startsWith("."));
        
        if (files == null || files.length == 0) {
            return null;
        }
        
        // Find the most recently modified file
        File newest = files[0];
        for (File file : files) {
            if (file.lastModified() > newest.lastModified()) {
                newest = file;
            }
        }
        
        return newest;
    }
    
    /**
     * Collects comprehensive metadata from the game state
     */
    private static Map<String, String> collectMetadata(MinecraftClient client) {
        Map<String, String> metadata = new HashMap<>();
        
        try {
            // Player information
            if (client.getSession() != null && client.getSession().getUsername() != null) {
                metadata.put("Username", client.getSession().getUsername());
            }
            
            // Player coordinates
            if (client.player != null) {
                metadata.put("X", String.valueOf((int) client.player.getX()));
                metadata.put("Y", String.valueOf((int) client.player.getY()));
                metadata.put("Z", String.valueOf((int) client.player.getZ()));
            }
            
            // World and biome information
            if (client.world != null && client.player != null) {
                // World/dimension
                String worldKey = client.world.getRegistryKey().getValue().toString();
                metadata.put("World", worldKey);
                
                // Biome (cleaned up for readability)
                String biomeName = getBiomeName(client);
                if (biomeName != null && !biomeName.isEmpty()) {
                    metadata.put("Biome", biomeName);
                }
            }
            
            // Timestamp
            metadata.put("Timestamp", Instant.now().toString());
            
            // Game version info
            metadata.put("MinecraftVersion", client.getGameVersion());
            metadata.put("ModVersion", "1.0.0");
            
        } catch (Exception e) {
            ScreenshotMetadataMod.LOGGER.error("Error collecting metadata", e);
        }
        
        return metadata;
    }
    
    /**
     * Extracts and cleans up the biome name for better readability
     */
    private static String getBiomeName(MinecraftClient client) {
        try {
            String biomeName = client.world.getBiome(client.player.getBlockPos()).toString();
            
            // Extract just the biome name from the full reference string
            if (biomeName.contains("minecraft:")) {
                int start = biomeName.indexOf("minecraft:") + 10;
                int end = biomeName.indexOf("]", start);
                if (end > start) {
                    biomeName = biomeName.substring(start, end);
                    // Convert snake_case to Title Case
                    return formatBiomeName(biomeName);
                }
            }
            
            return biomeName;
        } catch (Exception e) {
            ScreenshotMetadataMod.LOGGER.debug("Could not extract biome name", e);
            return "Unknown";
        }
    }
    
    /**
     * Formats biome name from snake_case to Title Case
     */
    private static String formatBiomeName(String biomeName) {
        if (biomeName == null || biomeName.isEmpty()) {
            return "Unknown";
        }
        
        String[] words = biomeName.replace("_", " ").split(" ");
        StringBuilder titleCase = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                titleCase.append(Character.toUpperCase(word.charAt(0)))
                         .append(word.substring(1).toLowerCase())
                         .append(" ");
            }
        }
        
        return titleCase.toString().trim();
    }
    
    /**
     * Adds metadata to the screenshot using both PNG and XMP methods
     */
    private static void addMetadataToScreenshot(File screenshotFile, Map<String, String> metadata) {
        // Add PNG embedded metadata
        try {
            PngMetadataWriter.writeMetadata(screenshotFile, metadata);
        } catch (Exception e) {
            ScreenshotMetadataMod.LOGGER.error("Failed to write PNG metadata to {}", screenshotFile.getName(), e);
        }
        
        // Create XMP sidecar file
        try {
            XmpSidecarWriter.writeSidecarFile(screenshotFile, metadata);
        } catch (Exception e) {
            ScreenshotMetadataMod.LOGGER.error("Failed to create XMP sidecar for {}", screenshotFile.getName(), e);
        }
    }
}
