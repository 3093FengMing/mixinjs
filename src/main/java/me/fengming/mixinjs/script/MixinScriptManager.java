package me.fengming.mixinjs.script;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.bindings.JavaWrapper;
import dev.latvian.mods.rhino.*;
import dev.latvian.mods.rhino.type.TypeInfo;
import me.fengming.mixinjs.MixinJs;
import me.fengming.mixinjs.Utils;
import me.fengming.mixinjs.script.js.AtJS;
import me.fengming.mixinjs.script.js.InjectorJS;
import me.fengming.mixinjs.script.js.MixinHandler;
import me.fengming.mixinjs.script.js.MixinsJS;
import me.fengming.mixinjs.script.wrapper.MixinJsJavaWrapper;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MixinScriptManager {
    public static final List<MixinScriptFile> mixinScriptFiles = new ArrayList<>();

    public static KubeJSScriptManager kubeScriptManager = null;
    protected static Context defaultContext;
    protected static Scriptable defaultScope;

    private static boolean kubeJsLoaded = false;
    private static final HashMap<String, MixinHandler<?>> handlers = new HashMap<>();

    public static void load() {
        // script manager
        defaultContext = new ContextFactory().enter();
        defaultScope = defaultContext.initSafeStandardObjects();
        addBindings(defaultContext, defaultScope, false);
        // scripts
        InjectorJS.load();
    }

    public static void addBindings(Context context, Scriptable scope, boolean kubeJavaWrapper) {
        if (kubeJavaWrapper) {
            context.addToScope(scope, "JavaWrapper", JavaWrapper.class);
        } else {
            context.addToScope(scope, "JavaWrapper", MixinJsJavaWrapper.class);
        }
        context.addToScope(scope, "MixinJsLogger", MixinJs.LOGGER);
        context.addToScope(scope, "Mixins", MixinsJS.class);
        context.addToScope(scope, "Injector", InjectorJS.class);
        context.addToScope(scope, "At", AtJS.class);
    }

    public static void loadScript() throws RuntimeException {
        load();
        mixinScriptFiles.forEach(MixinScriptFile::run);
    }

    public static void runWithBindings(Path scriptPath, String name) throws RuntimeException {
        // run scripts to add custom mixin to Mixins
        try {
            InputStreamReader isr = new InputStreamReader(Files.newInputStream(scriptPath), StandardCharsets.UTF_8);
            defaultContext.evaluateReader(defaultScope, isr, name, 1, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to run script: " + name); // trigger reload command
        }
    }

    public static Object handle(String handlerName, CallbackInfo ci, Object thisObject, Object[] args) {
        MixinHandler<?> handler = getHandler(handlerName);
        if (handler == null) {
            MixinJs.LOGGER.error("Script {} not found", handlerName);
            return null;
        }
        // be sure call by KubeJs
        if (!kubeJsLoaded && MixinJs.config.isForceKubeJsLoad()) {
            if (!isKubeJsLoaded()) {
                MixinJs.LOGGER.error("Script {} attempted to run when KubeJS was not loaded, skipped.", handlerName);
                return null;
            } else {
                // Re-set the context and the scope with KubeJs context and scope
                if (kubeScriptManager == null) {
                    kubeScriptManager = new KubeJSScriptManager(KubeJS.getStartupScriptManager());
                }
                defaultContext = kubeScriptManager.context;
                defaultScope = kubeScriptManager.topScope;
                defaultContext.setTopCall(defaultScope);
                addBindings(defaultContext, defaultScope, true);
                kubeJsLoaded = true;
            }
        }

        NativeJavaArray methodArgs = (NativeJavaArray) defaultContext.wrap(defaultScope, args, TypeInfo.OBJECT_ARRAY);
        try {
            Object obj = handler.handle(defaultContext, defaultScope, thisObject, ci, methodArgs);
            if (obj instanceof NativeJavaObject nativeObj) {
                return nativeObj.unwrap();
            }
            return obj;
        } catch (Throwable e) {
            MixinJs.LOGGER.error("Failed to handle {}: ", handlerName, e);
        }
        return null;
    }

    public static boolean isKubeJsLoaded() {
        if (!kubeJsLoaded) {
            kubeJsLoaded = Utils.getLoadedClasses().stream().anyMatch(s -> s.startsWith("dev.latvian.mods.kubejs"));
            // here is a shortcut
            if (kubeJsLoaded && KubeJS.getStartupScriptManager() == null) {
                kubeJsLoaded = false;
            }
        }
        return kubeJsLoaded;
    }

    public static void addHandler(String handlerName, MixinHandler<?> handler) {
        handlers.put(handlerName, handler);
    }

    public static MixinHandler<?> getHandler(String handlerName) {
        return handlers.getOrDefault(handlerName, null);
    }
}
