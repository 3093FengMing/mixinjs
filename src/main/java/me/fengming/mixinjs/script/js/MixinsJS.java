package me.fengming.mixinjs.script.js;

import me.fengming.mixinjs.MixinJs;

@SuppressWarnings("unused")
public class MixinsJS {
    public static MixinBuilderJS builder(boolean isClient, String id, String target) {
        return new MixinBuilderJS(isClient, id, target);
    }

    public static void forceKubeJsLoad(boolean bl) {
        MixinJs.isForceKubeJsLoad = bl;
    }

    public static void forceKubeJsLoad() {
        MixinJs.isForceKubeJsLoad = true;
    }
}
