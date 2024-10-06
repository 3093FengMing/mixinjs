package me.fengming.mixinjs.config;

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
    @SerializedName("id")
    private String id;

    @SerializedName("refmap")
    private String refmap;

    @SerializedName("mixins")
    private List<String> mixins;

    private final List<MixinScriptFile> mixinScriptFiles = new ArrayList<>();

    private final List<String> serverMixinClasses = new ArrayList<>();
    private final List<String> clientMixinClasses = new ArrayList<>();

    public static MixinJsConfig create(String configPath) {
        try {
            InputStream is = Files.newInputStream(Utils.mixinScriptPath.resolve(configPath));
            // MixinJs.LOGGER.info("mixinconfig: {}", Files.readString(Utils.mixinScriptPath.resolve(configPath)));
            MixinJsConfig cfg = Utils.GSON.fromJson(new InputStreamReader(is), MixinJsConfig.class);
            if (cfg == null) throw new IllegalArgumentException("Failed to read config: " + configPath);
            return cfg;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read config: " + configPath, e);
        }
    }

    public void load() {
        MixinJs.LOGGER.info("[MixinJs] Loading MixinJs config {}", id);
        mixins.forEach(f -> mixinScriptFiles.add(new MixinScriptFile(f)));
        loadScripts();
    }

    public void writeMixinConfig() {
        // Create mixin config
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("template.generated.mixins.json");
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                String toString = new String(bytes);
                String json = toString.replace("${refmap}", refmap)
                        .replace("${serverMixins}", mixinsToString(clientMixinClasses))
                        .replace("${clientMixins}", mixinsToString(serverMixinClasses));
                if (Files.notExists(Utils.mixinConfigPath)) {
                    Files.createFile(Utils.mixinConfigPath);
                }
                Files.writeString(Utils.mixinConfigPath, json);
                is.close();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("[MixinJs] Failed to read template.generated.mixins.json: ", e);
        }
    }

    public void loadScripts() {
        mixinScriptFiles.forEach(MixinScriptFile::run);
    }

    public void putMixinClass(String className, boolean isClient) {
        if (isClient) {
            clientMixinClasses.add(className);
        } else {
            serverMixinClasses.add(className);
        }
    }

    private static String mixinsToString(List<String> list) {
        if (list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        list.forEach(s -> sb.append("\"").append(s).append("\", "));
        return sb.delete(sb.length() - 2, sb.length()).append("]").toString();
    }
}
