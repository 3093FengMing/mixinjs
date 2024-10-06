package me.fengming.mixinjs.script;

import me.fengming.mixinjs.MixinJs;
import me.fengming.mixinjs.Utils;

public class MixinScriptFile {
    protected String name;

    public MixinScriptFile(String scriptName) {
        this.name = scriptName;
    }

    public String getName() {
        return this.name;
    }

    public void run() {
        MixinJs.LOGGER.info("[MixinJs] Running mixin script: {}", name + ".js");
        MixinScriptManager.runWithBindings(Utils.mixinScriptPath.resolve(name + ".js"), name);
    }
}
