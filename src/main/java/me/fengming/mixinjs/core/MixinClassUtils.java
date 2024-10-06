package me.fengming.mixinjs.core;

import me.fengming.mixinjs.script.MixinScriptManager;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
public class MixinClassUtils {
    // Only be called in mixin class
    public static Object runScriptR(String scriptName, CallbackInfo ci, Object thisObject, Object... args) {
        return MixinScriptManager.handle(scriptName, ci, thisObject, args);
    }

    public static void runScript(String scriptName, CallbackInfo ci, Object thisObject, Object... args) {
        MixinScriptManager.handle(scriptName, ci, thisObject, args);
    }

//    public void test(Object arg, CallbackInfo ci) {
//        MixinClassUtils.runScript("test", ci, this, arg);
//    }
}
