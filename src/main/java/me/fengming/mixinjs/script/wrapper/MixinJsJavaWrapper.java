package me.fengming.mixinjs.script.wrapper;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeJavaClass;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class MixinJsJavaWrapper {
    public static MixinJsJavaWrapper INSTANCE;

    private final Map<String, NativeJavaClass> classCache = new HashMap<>();
    private final Context context;

    protected MixinJsJavaWrapper(Context context) {
        this.context = context;
    }

    public Object loadClass(String className) throws ClassNotFoundException {
        NativeJavaClass clazz = classCache.get(className);
        if (clazz == null) {
            clazz = new NativeJavaClass(context, context.getTopCallScope(), Class.forName(className));
            classCache.put(className, clazz);
        }
        return clazz;
    }

    public static void init(Context context) {
        INSTANCE = new MixinJsJavaWrapper(context);
    }
}
