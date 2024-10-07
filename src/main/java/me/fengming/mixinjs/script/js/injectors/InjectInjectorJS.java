package me.fengming.mixinjs.script.js.injectors;

import me.fengming.mixinjs.script.js.InjectorJS;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

public class InjectInjectorJS extends InjectorJS {
    public boolean cancellable = false;
    public LocalCapture locals = LocalCapture.NO_CAPTURE;

    public InjectInjectorJS() {
        super(Inject.class.getName(), AtFlag.MULTI);
    }

    public InjectInjectorJS locals(LocalCapture locals) {
        this.locals = locals;
        return this;
    }

    public InjectInjectorJS cancellable(boolean cancellable) {
        this.cancellable = cancellable;
        return this;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public boolean hasCI() {
        return true;
    }
}
