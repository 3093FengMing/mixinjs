package me.fengming.mixinjs.script.js;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface MixinHandler<T> {
    T handle(Object thisObject, CallbackInfo ci, Object[] args);
}
