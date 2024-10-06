package me.fengming.mixinjs.mixin;

import me.fengming.mixinjs.MixinJs;
import me.fengming.mixinjs.Utils;
import me.fengming.mixinjs.config.MixinJsConfig;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

import java.io.IOException;
import java.nio.file.Files;

@SuppressWarnings("unused")
public class MixinJsConnector implements IMixinConnector {
    @Override
    public void connect() {
        MixinJs.LOGGER.info("[MixinJsConnector] Connected to Mixin.");
        if (Files.notExists(Utils.modConfigPath)) {
            try {
                Files.createDirectories(Utils.modConfigPath);
                Files.createFile(Utils.modConfigPath);
                writeDefaultConfig();
            } catch (IOException e) {
                MixinJs.LOGGER.error("Failed to create mod config file: ", e);
            }
        }
        if (Files.notExists(Utils.configPath)) {
            try {
                Files.createDirectories(Utils.mixinScriptPath);
                Files.createFile(Utils.configPath);
                writeTestConfig();
            } catch (IOException e) {
                MixinJs.LOGGER.error("Failed to create mixin config file: ", e);
            }
        }
        // Load configs
        MixinJs.config = MixinJsConfig.create("mixins.config.json");
        MixinJs.config.load();
        // Generate and load mixin class
        MixinJs.config.loadScripts();
        // Load mixin
        Mixins.addConfiguration(Utils.mixinConfigPath.toString());
    }

    private void writeDefaultConfig() throws IOException {
        Files.writeString(Utils.modConfigPath, """
                        {
                          "id": "mixinjs_test",
                          "mixins": {
                            "server": [
                            ],
                            "client": [
                              "hello_mixin"
                            ]
                          }
                        }
                        """);
    }

    private void writeTestConfig() throws IOException {
        Files.writeString(Utils.configPath, """
                        {
                          "id": "mixinjs_test",
                          "mixins": {
                            "server": [
                            ],
                            "client": [
                              "hello_mixin"
                            ]
                          }
                        }
                        """);
    }
}
