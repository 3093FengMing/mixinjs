package me.fengming.mixinjs_coremod.core;

import net.neoforged.fml.loading.FMLPaths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.transformers.MixinClassReader;

import java.io.IOException;
import java.nio.file.Files;

public class MixinInfoInjector {
    // Called by MixinInfo
    public static ClassNode getMixinClassNode(IClassBytecodeProvider provider,
                                              String mixinClassName, boolean runTransformers, int readerFlags) {
        try {
            // try load class in classpath (original behavior)
            return provider.getClassNode(mixinClassName, runTransformers, readerFlags);
        } catch (ClassNotFoundException | IOException e) {
            // if it cannot be found, go to the path we specified to find
            try {
                String internalName = mixinClassName.replace('.', '/');
                byte[] bytes = Files.readAllBytes(FMLPaths.GAMEDIR.get().resolve(".mixinjs").resolve(internalName + ".class"));
                ClassReader cr = new MixinClassReader(bytes, mixinClassName);
                ClassNode cn = new ClassNode();
                cr.accept(cn, readerFlags);
                return cn;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
