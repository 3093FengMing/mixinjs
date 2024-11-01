package me.fengming.mixinjs.mixin;

import me.fengming.mixinjs.MixinJs;
import me.fengming.mixinjs.Utils;
import me.fengming.mixinjs.config.MixinJsConfig;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

import java.nio.file.Files;

@SuppressWarnings("unused")
public class MixinJsConnector implements IMixinConnector {
    @Override
    public void connect() {
        MixinJs.LOGGER.info("[MixinJsConnector] Connected to Mixin.");
        if (Files.notExists(Utils.configPath)) {
            MixinJs.LOGGER.error("[MixinJs] Not found mixin config file!");
            return;
        }
        // Load configs
        MixinJs.config = MixinJsConfig.create("mixins.config.json");
        MixinJs.config.load();
        MixinJs.config.writeMixinConfig();

        Mixins.addConfiguration(Utils.mixinConfigPath.toString());
    }
}
