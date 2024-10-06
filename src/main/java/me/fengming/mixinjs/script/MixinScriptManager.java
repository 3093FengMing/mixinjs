package me.fengming.mixinjs.script;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.Scriptable;
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
import java.util.HashMap;

public class MixinScriptManager {

    protected static Context defaultContext = new ContextFactory().enter();
    protected static Scriptable defaultScope = defaultContext.initSafeStandardObjects();

    private static boolean kubeLoaded = false;
    private static final HashMap<String, MixinHandler<?>> handlers = new HashMap<>();

    static {
        MixinJsJavaWrapper.init(defaultContext);
        defaultContext.addToScope(defaultScope, "Logger", MixinJs.LOGGER);
        defaultContext.addToScope(defaultScope, "JavaWrapper", MixinJsJavaWrapper.INSTANCE);
        defaultContext.addToScope(defaultScope, "Mixins", MixinsJS.class);
        defaultContext.addToScope(defaultScope, "Injector", InjectorJS.class);
        defaultContext.addToScope(defaultScope, "At", AtJS.class);
    }

    public static void runWithBindings(Path scriptPath, String name) {
        // run scripts to add custom mixin to Mixins
        try {
            InputStreamReader isr = new InputStreamReader(Files.newInputStream(scriptPath), StandardCharsets.UTF_8);
            defaultContext.evaluateReader(defaultScope, isr, name, 1, null);
        } catch (IOException e) {
            MixinJs.LOGGER.error("Failed to run script: {}", name, e);
        }
    }

    public static Object handle(String handlerName, CallbackInfo ci, Object thisObject, Object[] args) {
        MixinHandler<?> handler = getHandler(handlerName);
        if (handler == null) {
            MixinJs.LOGGER.error("Script {} not found", handlerName);
            return null;
        }
        // be sure call by KubeJs
        if (MixinJs.isForceKubeJsLoad) {
            if (!kubeLoaded) {
                kubeLoaded = Utils.getLoadedClasses().stream().anyMatch(s -> s.startsWith("dev.latvian.mods.kubejs"));
            }
            if (!kubeLoaded) {
                MixinJs.LOGGER.error("Script {} attempted to run when KubeJs was not loaded, skipped.", handlerName);
                return null;
            } else {
                KubeJSContext kubeContext = (KubeJSContext) KubeJS.getStartupScriptManager().contextFactory.enter();
                defaultContext.setTopCall(kubeContext.topLevelScope);
            }
        }
        return handler.handle(thisObject, ci, args);
    }

    public static MixinHandler<?> addHandler(String handlerName, MixinHandler<?> handler) {
        return handlers.put(handlerName, handler);
    }

    public static MixinHandler<?> getHandler(String handlerName) {
        return handlers.getOrDefault(handlerName, null);
    }
}
