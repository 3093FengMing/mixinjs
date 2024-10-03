package me.fengming.mixinjs.script.js;

public record MixinMethod(String target, InjectorJS injector, MixinHandler<?> handler, String desc, boolean isStatic, boolean withReturn) {
}
