package me.fengming.mixinjs.script.js;

import me.fengming.mixinjs.MixinJs;

@SuppressWarnings("unused")
public class MixinsJS {
    public static MixinBuilderJS builder(String id, String target) {
        return new MixinBuilderJS(id, target);
    }

    public static void forceKubeJsLoad(boolean bl) {
        MixinJs.isForceKubeJsLoad = bl;
    }

    public static void forceKubeJsLoad() {
        MixinJs.isForceKubeJsLoad = true;
    }
}
