package me.fengming.mixinjs.mixin;

import me.fengming.mixinjs.MixinJs;
import me.fengming.mixinjs.Utils;
import me.fengming.mixinjs.config.MixinJsConfig;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

import java.io.IOException;
import java.nio.file.Files;

public class MixinJsConnector implements IMixinConnector {
    @Override
    public void connect() {
        MixinJs.LOGGER.info("[MixinJsConnector] Connected to Mixin.");
        if (Files.notExists(Utils.configPath)) {
            try {
                Files.createDirectories(Utils.mixinScriptPath);
                Files.createFile(Utils.configPath);
                // writeTestConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // Load mixins config
        MixinJs.config = MixinJsConfig.create("mixins.config.json");
        MixinJs.config.load();
        // Generate and load mixin class
        MixinJs.config.loadScripts();
        // Load mixin
        Mixins.addConfiguration(Utils.mixinConfigPath.toString());
    }

    private void writeTestConfig() throws IOException {
        Files.writeString(Utils.configPath, """
                        {
                          "id": "mixinjs_test",
                          "mixins": {
                            "server": [
                            ],
                            "client": [
                              "new_mixin"
                            ]
                          }
                        }
                        """);
    }
}
