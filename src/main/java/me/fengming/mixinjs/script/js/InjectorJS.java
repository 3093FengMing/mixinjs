package me.fengming.mixinjs.script.js;

public class InjectorJS {
    public static final InjectorJS INJECT = new InjectorJS("Inject","org.spongepowered.asm.mixin.injection.Inject");

    public final String name;
    public final String className;
    public String target;
    public AtJS at;

    protected InjectorJS(String name, String className) {
        this.name = name;
        this.className = className;
    }

    public InjectorJS target(String target) {
        this.target = target;
        return this;
    }

    public InjectorJS at(AtJS at) {
        this.at = at;
        return this;
    }

    public InjectorJS build() {
        return this;
    }
}
