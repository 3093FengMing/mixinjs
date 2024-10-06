package me.fengming.mixinjs.script.wrapper;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeJavaClass;

@SuppressWarnings("unused")
public class MixinJsJavaWrapper {
    public static MixinJsJavaWrapper INSTANCE;

    private final Context context;

    protected MixinJsJavaWrapper(Context context) {
        this.context = context;
    }

    public Object loadClass(String className) throws ClassNotFoundException {
        return new NativeJavaClass(context, context.getTopCallScope(), Class.forName(className));
    }

    public static void init(Context context) {
        INSTANCE = new MixinJsJavaWrapper(context);
    }
}
