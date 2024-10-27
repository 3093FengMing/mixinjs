package me.fengming.mixinjs.script;

import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.Scriptable;

public class KubeJSScriptManager {

    public final KubeJSContext context;
    public final Scriptable topScope;

    public KubeJSScriptManager(ScriptManager scriptManager) {
        this.context = (KubeJSContext) scriptManager.contextFactory.enter();
        this.topScope = this.context.topLevelScope;
    }
}
