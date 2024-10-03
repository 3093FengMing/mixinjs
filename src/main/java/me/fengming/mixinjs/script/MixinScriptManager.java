package me.fengming.mixinjs.script;

import dev.latvian.mods.kubejs.StartupScriptManager;
import dev.latvian.mods.kubejs.client.ClientScriptManager;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;
import me.fengming.mixinjs.Utils;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class ScriptManager {
    private static final ClientScriptManager clientScriptManager = new ClientScriptManager();
    private static final dev.latvian.mods.kubejs.script.ScriptManager serverScriptManager = new StartupScriptManager();

    // Only be called in mixin class
    public static void runScript(boolean isClient, String scriptName, CallbackInfo ci, Object thisObject, Object... args) {
        // Get KubeJs' script manager
        Context context;
        if (isClient) {
            context = clientScriptManager.contextFactory.enter();
        } else {
            context = serverScriptManager.contextFactory.enter();
        }
        // Call js file
        Scriptable topScope = context.initSafeStandardObjects();
        context.addToScope(topScope, "ThisObject", thisObject);
        context.addToScope(topScope, "CallbackInfo", ci);
        context.addToScope(topScope, "Args", args);
        try {
            InputStreamReader isr = new InputStreamReader(Files.newInputStream(Utils.mixinScriptPath.resolve(scriptName)));
            context.evaluateReader(topScope, isr, scriptName, 0, null);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load script: " + scriptName, e);
        }
    }
}
