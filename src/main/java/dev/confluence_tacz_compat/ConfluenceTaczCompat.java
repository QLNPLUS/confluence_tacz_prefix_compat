package dev.confluence_tacz_compat;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ConfluenceTaczCompat.MOD_ID)
public final class ConfluenceTaczCompat {
    public static final String MOD_ID = "confluence_tacz_compat";

    public ConfluenceTaczCompat(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, CompatConfig.SPEC);
        NeoForge.EVENT_BUS.register(new PrefixAttributeHandler());
    }
}
