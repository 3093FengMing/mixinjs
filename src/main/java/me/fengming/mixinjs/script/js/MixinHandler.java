package me.fengming.mixinjs.script.js;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeJavaArray;
import dev.latvian.mods.rhino.Scriptable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface MixinHandler<T> {
    T handle(Context context, Scriptable scope, Object thisObject, CallbackInfo ci, NativeJavaArray args);
}
