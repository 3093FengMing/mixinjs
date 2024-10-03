package me.fengming.mixinjs.script.js;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface MixinCaller<T> {
    T call(Object thisObject, CallbackInfo ci, Object[] args);
}
