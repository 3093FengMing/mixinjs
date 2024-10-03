package me.fengming.mixinjs.core;

import me.fengming.mixinjs.Mixinjs;
import me.fengming.mixinjs.script.MixinScriptManager;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class MixinClassUtils {

    // Only be called in mixin class
    public static void runScript(String scriptName, CallbackInfo ci, Object thisObject, Object... args) {
        MixinScriptManager.handle(scriptName, ci, thisObject, args);
    }
}
