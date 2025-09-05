package com.example.screenshotmetadata.mixin;

import com.example.screenshotmetadata.PngMetadataWriter;
import com.example.screenshotmetadata.XmpSidecarWriter;
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

@Mixin(ScreenshotRecorder.class)
public class ScreenshotRecorderMixin {
    
    @Inject(method = "saveScreenshot(Ljava/io/File;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V", at = @At("TAIL"))
    private static void onScreenshotSaving(File gameDirectory, net.minecraft.client.gl.Framebuffer framebuffer, java.util.function.Consumer<net.minecraft.text.Text> messageReceiver, CallbackInfo ci) {
        try {
            System.out.println("Screenshot save completed - adding metadata...");
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                // Find the most recent screenshot file
                File screenshotsDir = new File(gameDirectory, "screenshots");
                if (screenshotsDir.exists()) {
                    File[] files = screenshotsDir.listFiles((dir, name) -> name.endsWith(".png"));
                    if (files != null && files.length > 0) {
                        // Sort by last modified to get the newest file
                        java.util.Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                        File newestFile = files[0];
                        
                        // Build metadata
                        Map<String, String> metadata = new HashMap<>();
                        metadata.put("Username", client.getSession().getUsername());
                        metadata.put("X", String.valueOf((int)client.player.getX()));
                        metadata.put("Y", String.valueOf((int)client.player.getY()));
                        metadata.put("Z", String.valueOf((int)client.player.getZ()));
                        
                        if (client.world != null) {
                            metadata.put("World", client.world.getRegistryKey().getValue().toString());
                            // Clean up biome name for better readability
                            String biomeName = client.world.getBiome(client.player.getBlockPos()).toString();
                            // Extract just the biome name from the full reference string
                            if (biomeName.contains("minecraft:")) {
                                int start = biomeName.indexOf("minecraft:") + 10;
                                int end = biomeName.indexOf("]", start);
                                if (end > start) {
                                    biomeName = biomeName.substring(start, end);
                                    // Convert snake_case to Title Case
                                    biomeName = biomeName.replace("_", " ");
                                    String[] words = biomeName.split(" ");
                                    StringBuilder titleCase = new StringBuilder();
                                    for (String word : words) {
                                        if (!word.isEmpty()) {
                                            titleCase.append(Character.toUpperCase(word.charAt(0)))
                                                    .append(word.substring(1).toLowerCase())
                                                    .append(" ");
                                        }
                                    }
                                    biomeName = titleCase.toString().trim();
                                }
                            }
                            metadata.put("Biome", biomeName);
                        }
                        metadata.put("Timestamp", Instant.now().toString());
                        
                        // Add metadata to the screenshot (PNG text chunks)
                        PngMetadataWriter.writeMetadata(newestFile, metadata);
                        
                        // Also create an XMP sidecar file for better File Explorer compatibility
                        XmpSidecarWriter.writeSidecarFile(newestFile, metadata);
                        
                        System.out.println("Added metadata to screenshot: " + newestFile.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to add metadata to screenshot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
