package me.fengming.mixinjs.config;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import me.fengming.mixinjs.MixinJs;
import me.fengming.mixinjs.Utils;
import me.fengming.mixinjs.script.MixinScriptFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MixinJsConfig {
    protected static Gson gson = new Gson();

    @SerializedName("id")
    private String id;

    @SerializedName("refmap")
    private String refmap;

    @SerializedName("mixins")
    private Mixins mixins;

    private static class Mixins {
        @SerializedName("server")
        private List<String> serverMixins;

        @SerializedName("client")
        private List<String> clientMixins;
    }

    private final List<MixinScriptFile> serverMixins = new ArrayList<>();
    private final List<MixinScriptFile> clientMixins = new ArrayList<>();

    public static MixinJsConfig create(String configPath) {
        try {
            InputStream is = Files.newInputStream(Utils.mixinScriptPath.resolve(configPath));
            // MixinJs.LOGGER.info("mixinconfig: {}", Files.readString(Utils.mixinScriptPath.resolve(configPath)));
            MixinJsConfig cfg = gson.fromJson(new InputStreamReader(is), MixinJsConfig.class);
            if (cfg == null) throw new IllegalArgumentException("Failed to read config: " + configPath);
            return cfg;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read config: " + configPath, e);
        }
    }

    public void load() {
        MixinJs.LOGGER.info("[MixinJs] Loading MixinJs config {}", id);
        mixins.serverMixins.forEach(f -> serverMixins.add(new MixinScriptFile(f, false)));
        mixins.clientMixins.forEach(f -> clientMixins.add(new MixinScriptFile(f, true)));
        // Create mixin config
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("template.generated.mixins.json");
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                String toString = new String(bytes);
                String json = toString.replace("${refmap}", refmap)
                        .replace("${serverMixins}", mixinsToString(serverMixins))
                        .replace("${clientMixins}", mixinsToString(clientMixins));
                if (Files.notExists(Utils.mixinConfigPath)) {
                    Files.createFile(Utils.mixinConfigPath);
                }
                Files.writeString(Utils.mixinConfigPath, json);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("[MixinJs] Failed to read template.generated.mixins.json: ", e);
        }
    }

    public void loadScripts() {
        serverMixins.forEach(MixinScriptFile::run);
        clientMixins.forEach(MixinScriptFile::run);
    }

    private static String mixinsToString(List<MixinScriptFile> list) {
        if (list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        list.forEach(m -> sb.append("\"").append(m.getName()).append("\", "));
        return sb.delete(sb.length() - 2, sb.length()).append("]").toString();
    }

}
