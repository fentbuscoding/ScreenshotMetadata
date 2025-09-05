package com.example.screenshotmetadata;

import net.fabricmc.api.ClientModInitializer;

public class ScreenshotMetadataMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("Screenshot Metadata mod loaded! Screenshots will now include metadata.");
    }
}
