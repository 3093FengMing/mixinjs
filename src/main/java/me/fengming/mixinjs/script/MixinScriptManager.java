package me.fengming.mixinjs.script;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.Scriptable;
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

    public static void addBindings(Context context, Scriptable scope, boolean noJavaWrapper) {
        if (!noJavaWrapper) {
            context.addToScope(scope, "JavaWrapper", MixinJsJavaWrapper.getInstanceForContext(context));
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
            MixinJs.LOGGER.error("Failed to run script: {}", name, e);
            throw new RuntimeException(); // trigger reload command
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
            if (!kubeJsLoaded) {
                kubeJsLoaded = Utils.getLoadedClasses().stream().anyMatch(s -> s.startsWith("dev.latvian.mods.kubejs"));
            }
            if (!kubeJsLoaded) {
                MixinJs.LOGGER.error("Script {} attempted to run when KubeJs was not loaded, skipped.", handlerName);
                return null;
            } else {
                KubeJSContext kubeContext = (KubeJSContext) KubeJS.getStartupScriptManager().contextFactory.enter();
                defaultContext = kubeContext;
                addBindings(defaultContext, kubeContext.topLevelScope, true);
                defaultContext.setTopCall(kubeContext.topLevelScope);
            }
        }
        try {
            return handler.handle(thisObject, ci, defaultContext.wrap(defaultScope, args, TypeInfo.OBJECT_ARRAY));
        } catch (Exception e) {
            MixinJs.LOGGER.error("Failed to handle {}: ", handlerName, e);
        }
        return null;
    }

    public static void addHandler(String handlerName, MixinHandler<?> handler) {
        handlers.put(handlerName, handler);
    }

    public static MixinHandler<?> getHandler(String handlerName) {
        return handlers.getOrDefault(handlerName, null);
    }
}
