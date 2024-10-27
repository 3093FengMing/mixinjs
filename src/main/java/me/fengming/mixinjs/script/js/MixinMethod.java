package me.fengming.mixinjs.script.js;

public record MixinMethod(String target, InjectorJS injector, String desc, boolean isStatic, MixinHandler<?> handler) {
}
