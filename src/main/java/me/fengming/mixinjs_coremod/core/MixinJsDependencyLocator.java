package me.fengming.mixinjs_coremod.core;

import com.mojang.logging.LogUtils;
import me.fengming.mixinjs_coremod.utils.JarInJarUtils;
import net.neoforged.neoforgespi.locating.IDependencyLocator;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import org.slf4j.Logger;

import java.util.List;

public class MixinJsDependencyLocator implements IDependencyLocator {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void scanMods(List<IModFile> loadedMods, IDiscoveryPipeline pipeline) {
        LOGGER.info("[MixinJsCoreMod] Loaded MixinJs DependencyLocator");
        pipeline.addModFile(pipeline.readModFile(JarInJarUtils.getJarInJar(e -> LOGGER.error("Failed to load main mod: ", e)), ModFileDiscoveryAttributes.DEFAULT));
    }

    @Override
    public String toString() {
        return "mixinjs";
    }
}
