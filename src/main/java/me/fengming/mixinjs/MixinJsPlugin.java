package me.fengming.mixinjs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import me.fengming.mixinjs.script.MixinScriptManager;
import me.fengming.mixinjs.script.js.AtJS;
import me.fengming.mixinjs.script.js.InjectorJS;
import me.fengming.mixinjs.script.js.MixinsJS;

public class MixinJsPlugin implements KubeJSPlugin {
    public MixinJsPlugin() {

    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        // Supports ProbeJs
        bindings.add("Mixins", MixinsJS.class);
        bindings.add("Injector", InjectorJS.class);
        bindings.add("At", AtJS.class);
    }

    @Override
    public void initStartup() {
        if (MixinJs.config.isForceKubeJsLoad()) {
            MixinScriptManager.loadScript();
        }
    }
}
