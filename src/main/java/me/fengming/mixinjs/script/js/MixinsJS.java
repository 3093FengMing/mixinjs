package me.fengming.mixinjs.script.js;

public class MixinsJS {
    public static MixinBuilderJS builder(String id, String target) {
        return new MixinBuilderJS(id, target);
    }
}
