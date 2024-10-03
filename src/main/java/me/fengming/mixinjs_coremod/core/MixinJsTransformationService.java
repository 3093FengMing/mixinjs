package me.fengming.mixinjs_coremod.core;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import org.codehaus.plexus.util.Base64;
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
    private static final String MIXIN_INFO = "org.spongepowered.asm.mixin.transformer.MixinInfo";
    private static final String IAE = "java/lang/IllegalArgumentException";

    private static final byte[] injectorBytes = Base64.decodeBase64("yv66vgAAAEEAdgoAAgADBwAEDAAFAAYBABBqYXZhL2xhbmcvT2JqZWN0AQAGPGluaXQ+AQADKClWCwAIAAkHAAoMAAsADAEANG9yZy9zcG9uZ2Vwb3dlcmVkL2FzbS9zZXJ2aWNlL0lDbGFzc0J5dGVjb2RlUHJvdmlkZXIBAAxnZXRDbGFzc05vZGUBADgoTGphdmEvbGFuZy9TdHJpbmc7WkkpTG9yZy9vYmplY3R3ZWIvYXNtL3RyZWUvQ2xhc3NOb2RlOwcADgEAIGphdmEvbGFuZy9DbGFzc05vdEZvdW5kRXhjZXB0aW9uBwAQAQATamF2YS9pby9JT0V4Y2VwdGlvbgoAEgATBwAUDAAVABYBABBqYXZhL2xhbmcvU3RyaW5nAQAHcmVwbGFjZQEAFihDQylMamF2YS9sYW5nL1N0cmluZzsJABgAGQcAGgwAGwAcAQAibmV0L25lb2ZvcmdlZC9mbWwvbG9hZGluZy9GTUxQYXRocwEAB0dBTUVESVIBACRMbmV0L25lb2ZvcmdlZC9mbWwvbG9hZGluZy9GTUxQYXRoczsKABgAHgwAHwAgAQADZ2V0AQAWKClMamF2YS9uaW8vZmlsZS9QYXRoOwgAIgEACC5taXhpbmpzCwAkACUHACYMACcAKAEAEmphdmEvbmlvL2ZpbGUvUGF0aAEAB3Jlc29sdmUBACgoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL25pby9maWxlL1BhdGg7EgAAACoMACsALAEAF21ha2VDb25jYXRXaXRoQ29uc3RhbnRzAQAmKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL1N0cmluZzsKAC4ALwcAMAwAMQAyAQATamF2YS9uaW8vZmlsZS9GaWxlcwEADHJlYWRBbGxCeXRlcwEAGChMamF2YS9uaW8vZmlsZS9QYXRoOylbQgcANAEAM29yZy9zcG9uZ2Vwb3dlcmVkL2FzbS90cmFuc2Zvcm1lcnMvTWl4aW5DbGFzc1JlYWRlcgoAMwA2DAAFADcBABcoW0JMamF2YS9sYW5nL1N0cmluZzspVgcAOQEAIG9yZy9vYmplY3R3ZWIvYXNtL3RyZWUvQ2xhc3NOb2RlCgA4AAMKADwAPQcAPgwAPwBAAQAdb3JnL29iamVjdHdlYi9hc20vQ2xhc3NSZWFkZXIBAAZhY2NlcHQBACQoTG9yZy9vYmplY3R3ZWIvYXNtL0NsYXNzVmlzaXRvcjtJKVYHAEIBABpqYXZhL2xhbmcvUnVudGltZUV4Y2VwdGlvbgoAQQBEDAAFAEUBABgoTGphdmEvbGFuZy9UaHJvd2FibGU7KVYHAEcBACptZS9mZW5nbWluZy9taXhpbmpzL2NvcmUvTWl4aW5JbmZvSW5qZWN0b3IBAARDb2RlAQAPTGluZU51bWJlclRhYmxlAQASTG9jYWxWYXJpYWJsZVRhYmxlAQAEdGhpcwEALExtZS9mZW5nbWluZy9taXhpbmpzL2NvcmUvTWl4aW5JbmZvSW5qZWN0b3I7AQARZ2V0TWl4aW5DbGFzc05vZGUBAG4oTG9yZy9zcG9uZ2Vwb3dlcmVkL2FzbS9zZXJ2aWNlL0lDbGFzc0J5dGVjb2RlUHJvdmlkZXI7TGphdmEvbGFuZy9TdHJpbmc7WkkpTG9yZy9vYmplY3R3ZWIvYXNtL3RyZWUvQ2xhc3NOb2RlOwEADGludGVybmFsTmFtZQEAEkxqYXZhL2xhbmcvU3RyaW5nOwEABWJ5dGVzAQACW0IBAAJjcgEAH0xvcmcvb2JqZWN0d2ViL2FzbS9DbGFzc1JlYWRlcjsBAAJjbgEAIkxvcmcvb2JqZWN0d2ViL2FzbS90cmVlL0NsYXNzTm9kZTsBAAJleAEAFUxqYXZhL2lvL0lPRXhjZXB0aW9uOwEAAWUBABVMamF2YS9sYW5nL0V4Y2VwdGlvbjsBAAhwcm92aWRlcgEANkxvcmcvc3BvbmdlcG93ZXJlZC9hc20vc2VydmljZS9JQ2xhc3NCeXRlY29kZVByb3ZpZGVyOwEADm1peGluQ2xhc3NOYW1lAQAPcnVuVHJhbnNmb3JtZXJzAQABWgEAC3JlYWRlckZsYWdzAQABSQEADVN0YWNrTWFwVGFibGUHAGQBABNqYXZhL2xhbmcvRXhjZXB0aW9uAQAKU291cmNlRmlsZQEAFk1peGluSW5mb0luamVjdG9yLmphdmEBABBCb290c3RyYXBNZXRob2RzCABpAQAHAS5jbGFzcw8GAGsKAGwAbQcAbgwAKwBvAQAkamF2YS9sYW5nL2ludm9rZS9TdHJpbmdDb25jYXRGYWN0b3J5AQCYKExqYXZhL2xhbmcvaW52b2tlL01ldGhvZEhhbmRsZXMkTG9va3VwO0xqYXZhL2xhbmcvU3RyaW5nO0xqYXZhL2xhbmcvaW52b2tlL01ldGhvZFR5cGU7TGphdmEvbGFuZy9TdHJpbmc7W0xqYXZhL2xhbmcvT2JqZWN0OylMamF2YS9sYW5nL2ludm9rZS9DYWxsU2l0ZTsBAAxJbm5lckNsYXNzZXMHAHIBACVqYXZhL2xhbmcvaW52b2tlL01ldGhvZEhhbmRsZXMkTG9va3VwBwB0AQAeamF2YS9sYW5nL2ludm9rZS9NZXRob2RIYW5kbGVzAQAGTG9va3VwACEARgACAAAAAAACAAEABQAGAAEASAAAAC8AAQABAAAABSq3AAGxAAAAAgBJAAAABgABAAAADABKAAAADAABAAAABQBLAEwAAAAJAE0ATgABAEgAAAFBAAQACQAAAGAqKxwduQAHBACwOgQrEC4QL7YAEToFsgAXtgAdEiG5ACMCABkFugApAAC5ACMCALgALToGuwAzWRkGK7cANToHuwA4WbcAOjoIGQcZCB22ADsZCLA6BbsAQVkZBbcAQ78AAwAAAAkACgANAAAACQAKAA8ADABTAFQADwADAEkAAAAqAAoAAAASAAoAEwAMABYAFgAXADQAGABAABkASQAaAFEAGwBUABwAVgAdAEoAAABmAAoAFgA+AE8AUAAFADQAIABRAFIABgBAABQAUwBUAAcASQALAFUAVgAIAFYACgBXAFgABQAMAFQAWQBaAAQAAABgAFsAXAAAAAAAYABdAFAAAQAAAGAAXgBfAAIAAABgAGAAYQADAGIAAAAbAAJKBwBj/wBJAAUHAAgHABIBAQcAYwABBwAPAAMAZQAAAAIAZgBnAAAACAABAGoAAQBoAHAAAAAKAAEAcQBzAHUAGQ==".getBytes());

    @Override
    public @NotNull String name() {
        return "mixinjs";
    }

    @Override
    public void initialize(IEnvironment environment) {}

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {
        LOGGER.info("[MixinJsCoreMod] Loaded MixinJs CoreMod");
        // hacky
        patchMixinConfig();
        patchMixinInfo();
    }

    private void patchMixinConfig() {
        LOGGER.info("[MixinJsCoreMod] Patching MixinConfig");
        try {
            // Construct a new MixinConfig to replace the old one
            // Allow Mixin to read config through external files
            ClassNode clazz = new ClassNode();
            ClassReader cr = new ClassReader(MIXIN_CONFIG);
            cr.accept(clazz, ClassReader.SKIP_FRAMES);
            MethodNode createMethod = clazz.methods.stream()
                    .filter(mn -> mn.name.equals("create")).findFirst().orElseThrow();

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
                if (instruction.getType() == InsnNode.TYPE_INSN
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

            loadClass(MIXIN_CONFIG, clazz);
        } catch (Throwable e) {
            LOGGER.error("Failed to patch MixinConfig class: ", e);
        }
    }

    private void patchMixinInfo() {
        LOGGER.info("[MixinJsCoreMod] Patching MixinInfo");
        try {
            // Construct a new MixinInfo to replace the old one
            // Allow Mixin to read class file through external files (redirect)
            ClassNode clazz = new ClassNode();
            ClassReader cr = new ClassReader(MIXIN_INFO);
            cr.accept(clazz, ClassReader.SKIP_FRAMES);

            // add method
            ClassNode injectNode = new ClassNode();
            ClassReader injectReader = new ClassReader(injectorBytes);
            injectReader.accept(injectNode, ClassReader.SKIP_FRAMES);
            injectNode.methods.stream().filter(m -> m.name.equals("getMixinClassNode")).forEach(clazz.methods::add);

            MethodNode method = clazz.methods.stream()
                    .filter(mn -> mn.name.equals("loadMixinClass")).findFirst().orElseThrow();

            // code: classNode = MixinClassUtils.getMixinClassNode(this.service.getBytecodeProvider(), mixinClassName, true, readerFlags);
            MethodInsnNode node = new MethodInsnNode(Opcodes.INVOKESTATIC, "org/spongepowered/asm/mixin/transformer/MixinInfo", "getMixinClassNode", "(Lorg/spongepowered/asm/service/IClassBytecodeProvider;Ljava/lang/String;ZI)Lorg/objectweb/asm/tree/ClassNode;", false);

            int index = 0;
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction.getType() == InsnNode.METHOD_INSN
                        && instruction.getOpcode() == Opcodes.INVOKEINTERFACE
                        && ((MethodInsnNode) instruction).owner.equals("org/spongepowered/asm/service/IClassBytecodeProvider")
                ) {
                    index = method.instructions.indexOf(instruction);
                    break;
                }
            }
            method.instructions.set(method.instructions.get(index), new InsnNode(Opcodes.NOP));
            method.instructions.insert(method.instructions.get(index), node);

            loadClass(MIXIN_INFO, clazz);
        } catch (Throwable e) {
            LOGGER.error("Failed to patch MixinInfo class: ", e);
        }
    }

    private void loadClass(String className, ClassNode clazz) {
        // Must recompute frames
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        clazz.accept(cw);
        byte[] bytes = cw.toByteArray();
        try {
            // Get MethodHandle in order to invoke ClassLoader#defineClass
            // Load MixinConfig in advance so that Mixin will prioritize the use of our code
            Field Field_theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            Field_theUnsafe.setAccessible(true);
            Unsafe UNSAFE = (Unsafe) Field_theUnsafe.get(null);
            Field Field_IMPL_LOOKUP = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            MethodHandles.Lookup LOOKUP = (MethodHandles.Lookup) UNSAFE.getObject(UNSAFE.staticFieldBase(Field_IMPL_LOOKUP), UNSAFE.staticFieldOffset(Field_IMPL_LOOKUP));
            // Define class, rewrite MixinConfig class
            MethodType methodType = MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class);
            MethodHandle handle = LOOKUP.findVirtual(ClassLoader.class, "defineClass", methodType);
            // Load class with our new MixinConfig
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            handle.invoke(classLoader, className, bytes, 0, bytes.length);
        } catch (Throwable e) {
            LOGGER.error("Failed to patch class " + className + ": ", e);
        }
    }

    @Override
    public @NotNull List<? extends ITransformer<?>> transformers() {
        return List.of();
    }
}
