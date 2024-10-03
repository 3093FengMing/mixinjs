package me.fengming.mixinjs.script;

import me.fengming.mixinjs.Mixinjs;
import me.fengming.mixinjs.Utils;

public class MixinScriptFile {
    protected boolean isClient;
    protected String name;

    public MixinScriptFile(String scriptName, boolean isClient) {
        this.name = scriptName;
        this.isClient = isClient;
    }

    public String getName() {
        return this.name;
    }

    public void run() {
        Mixinjs.LOGGER.info("[MixinJs] Running mixin script: {}", name + ".js");
        MixinScriptManager.runWithBindings(Utils.mixinScriptPath.resolve(name + ".js"), name, isClient);
    }
}
