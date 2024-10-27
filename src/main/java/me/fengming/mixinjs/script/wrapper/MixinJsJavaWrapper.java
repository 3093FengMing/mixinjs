package me.fengming.mixinjs.script.wrapper;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeJavaClass;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class MixinJsJavaWrapper {
    private static final Map<String, NativeJavaClass> classCache = new HashMap<>();

    public static Object loadClass(Context cx, String className) throws ClassNotFoundException {
        NativeJavaClass clazz = classCache.get(className);
        if (clazz == null) {
            clazz = new NativeJavaClass(cx, cx.getTopCallOrThrow(), Class.forName(className));
            classCache.put(className, clazz);
        }
        return clazz;
    }
}
