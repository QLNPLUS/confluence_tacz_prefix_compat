package dev.confluence_tacz_compat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ConfluenceTaczCompat.MOD_ID)
public final class ConfluenceTaczCompat {
    public static final String MOD_ID = "confluence_tacz_compat";

    @SuppressWarnings("removal")
    public ConfluenceTaczCompat() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CompatConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(new PrefixAttributeHandler());
    }
}
