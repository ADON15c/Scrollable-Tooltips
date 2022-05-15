package net.adon15.scrollable_tooltips.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Environment(EnvType.CLIENT)
public class Scrollable_tooltipsClient implements ClientModInitializer {
    public static final String MOD_ID = "scrollable_tooltips";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initialized " + MOD_ID);
    }
}
