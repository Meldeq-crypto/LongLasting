package com.everlasting;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EverlastingFixes implements ClientModInitializer {
    public static final String MOD_ID = "everlastingfixes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Everlasting Fixes initialized.");
    }
}
