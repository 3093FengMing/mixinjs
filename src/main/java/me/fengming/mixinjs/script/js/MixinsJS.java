package me.fengming.mixinjs.script.js;

@SuppressWarnings("unused")
public class MixinsJS {
    public static MixinBuilderJS builder(boolean isClient, String id, String target) {
        return new MixinBuilderJS(isClient, id, target);
    }
}
