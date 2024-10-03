package me.fengming.mixinjs.script;

import dev.latvian.mods.kubejs.StartupScriptManager;
import dev.latvian.mods.kubejs.client.ClientScriptManager;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.script.KubeJSContextFactory;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.Scriptable;
import me.fengming.mixinjs.Mixinjs;
import me.fengming.mixinjs.Utils;
import me.fengming.mixinjs.script.js.AtJS;
import me.fengming.mixinjs.script.js.InjectorJS;
import me.fengming.mixinjs.script.js.MixinHandler;
import me.fengming.mixinjs.script.js.MixinsJS;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class MixinScriptManager {
    protected ScriptManager scriptManager;
    protected KubeJSContext context;
    protected Scriptable topScope;

    private static final HashMap<String, MixinHandler<?>> handlers = new HashMap<>();

    public MixinScriptManager(ScriptManager scriptManager) {
        this.scriptManager = scriptManager;
        this.context = (KubeJSContext) new KubeJSContextFactory(scriptManager).enter();
        this.topScope = context.topLevelScope;
        context.addToScope(topScope, "console", context.getType().console);
        context.addToScope(topScope, "Mixins", MixinsJS.class);
        context.addToScope(topScope, "Injector", InjectorJS.class);
        context.addToScope(topScope, "At", AtJS.class);
    }

    public static void runWithBindings(Path scriptPath, String name, boolean isClient) {
        try {
            MixinScriptManager msm = isClient ? Utils.clientScriptManager : Utils.serverScriptManager;
            InputStreamReader isr = new InputStreamReader(Files.newInputStream(scriptPath));
            msm.context.evaluateReader(msm.topScope, isr, name, 1, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object handle(String handlerName, CallbackInfo ci, Object thisObject, Object[] args) {
        MixinHandler<?> handler = getHandler(handlerName);
        if (handler == null) {
            Mixinjs.LOGGER.error("Script {} not found", handlerName);
            return null;
        }
        return handler.handle(thisObject, ci, args);
    }

    public static MixinHandler<?> addHandler(String handlerName, MixinHandler<?> handler) {
        return handlers.put(handlerName, handler);
    }

    public static MixinHandler<?> getHandler(String handlerName) {
        return handlers.getOrDefault(handlerName, null);
    }

    public static class Client extends MixinScriptManager {
        public Client() {
            super(new ClientScriptManager());
        }
    }

    public static class Server extends MixinScriptManager {
        public Server() {
            super(new StartupScriptManager());
        }
    }
}
