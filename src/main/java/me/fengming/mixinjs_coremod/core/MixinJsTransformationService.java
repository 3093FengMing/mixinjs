package me.fengming.mixinjs_coremods.core;

import cpw.mods.modlauncher.api.*;
import me.fengming.mixinjs.core.MixinClassGenerator;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class MixinJsTransformationService implements ITransformationService {

    public static final Logger LOGGER = LoggerFactory.getLogger("MixinJsCoreMod");

    private static final String MIXIN_CONFIG = "org.spongepowered.asm.mixin.transformer.MixinConfig";
    private static final String IAE = "java/lang/IllegalArgumentException";

    @Override
    public @NotNull String name() {
        return "mixinjs";
    }

    @Override
    public void initialize(IEnvironment environment) {}

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {
        // hacky
        LOGGER.info("[MixinJsCoreMod] Patching MixinConfig");
        try {
            // Construct a new MixinConfig to replace the old one
            // Allow Mixin to read config through external files
            ClassNode clazz = new ClassNode();
            ClassReader cr = new ClassReader(MIXIN_CONFIG);
            cr.accept(clazz, ClassReader.SKIP_FRAMES);
            MethodNode createMethod = clazz.methods.stream().filter(mn -> mn.name.equals("create")).findFirst().orElseThrow();

            // code: resource = new FileInputStream(configFile);
            InsnList insertInsn = new InsnList();
            insertInsn.add(new TypeInsnNode(Opcodes.NEW, "java/io/FileInputStream"));
            insertInsn.add(new InsnNode(Opcodes.DUP));
            insertInsn.add(new VarInsnNode(Opcodes.ALOAD, 0)); // 0 = configFile
            insertInsn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/io/FileInputStream", "<init>", "(Ljava/lang/String;)V", false));
            insertInsn.add(new VarInsnNode(Opcodes.ASTORE, 4)); // 4 = resource

            int index = 0;
            for (AbstractInsnNode instruction : createMethod.instructions) {
                // Get index of the first instruction in if case
                if (instruction.getType() == AbstractInsnNode.TYPE_INSN
                        && instruction.getOpcode() == Opcodes.NEW
                        && IAE.equals(((TypeInsnNode) instruction).desc)
                ) {
                    index = createMethod.instructions.indexOf(instruction);
                    break;
                }
            }
            // Remove original code
            for (int i = 0; i < 12; i++) {
                createMethod.instructions.set(createMethod.instructions.get(index + i), new InsnNode(Opcodes.NOP));
            }
            // Insert our code at first
            createMethod.instructions.insert(createMethod.instructions.get(index), insertInsn);

            MixinClassGenerator.loadClass(MIXIN_CONFIG, clazz);
        } catch (Throwable e) {
            LOGGER.error("Failed to patch MixinConfig class: ", e);
        }
    }

    @Override
    public @NotNull List<? extends ITransformer<?>> transformers() {
        return List.of();
    }
}
