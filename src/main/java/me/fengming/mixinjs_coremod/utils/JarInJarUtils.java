package me.fengming.mixinjs_coremod.utils;

import com.google.common.collect.ImmutableMap;
import cpw.mods.jarhandling.JarContents;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

public class JarInJarUtils {
    public static JarContents getJarInJar(Consumer<Exception> errorHandler) {
        // net.neoforged.fml.loading.moddiscovery.locators.JarInJarDependencyLocator
        try {
            Path pathInModFile = Path.of(JarInJarUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI()).resolve("META-INF/jarjar/mixinjs-main.jar");
            URI filePathUri = new URI("jij:" + pathInModFile.toAbsolutePath().toUri().getRawSchemeSpecificPart()).normalize();
            Map<String, ?> outerFsArgs = ImmutableMap.of("packagePath", pathInModFile);
            FileSystem zipFS = FileSystems.newFileSystem(filePathUri, outerFsArgs);
            return JarContents.of(zipFS.getPath("/"));
        } catch (Exception e) {
            errorHandler.accept(e);
        }
        return null;
    }
}
