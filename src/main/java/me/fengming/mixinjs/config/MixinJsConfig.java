package me.fengming.mixinjs.core;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import me.fengming.mixinjs.Mixinjs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MixinJsConfig {
    @SerializedName("id")
    private String id;

    @SerializedName("mixins")
    private Mixins mixins;

    private static class Mixins {
        @SerializedName("server")
        private List<String> serverMixins;

        @SerializedName("client")
        private List<String> clientMixins;
    }

    public static MixinJsConfig create(Path configPath) {
        Gson gson = new Gson();
        try {
            InputStream is = Files.newInputStream(configPath);
            return gson.fromJson(new InputStreamReader(is), MixinJsConfig.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read config: ", e);
        }
    }

    public void load() {
        Mixinjs.LOGGER.info("[MixinJs] Loading MixinJs config {}", id);
    }
}
