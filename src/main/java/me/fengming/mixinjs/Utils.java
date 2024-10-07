package me.fengming.mixinjs;

import com.google.gson.Gson;
import net.neoforged.fml.loading.FMLPaths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.service.modlauncher.ModLauncherClassTracker;
import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"deprecation", "unchecked"})
public class Utils {
    public static Gson GSON = new Gson();

    public static final Path mixinClassPath = FMLPaths.GAMEDIR.get().resolve(".mixinjs");
    public static final Path mixinScriptPath = FMLPaths.GAMEDIR.get().resolve("mixinjs");
    public static final Path configPath = mixinScriptPath.resolve("mixins.config.json");
    public static final Path mixinConfigPath = mixinScriptPath.resolve("generated.mixins.json");

    public static final IMixinService mixinService = MixinService.getService();

    public static final Unsafe unsafe;
    public static final MethodHandles.Lookup lookup;

    static {
        try {
            Field Field_theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            Field_theUnsafe.setAccessible(true);
            unsafe = (Unsafe) Field_theUnsafe.get(null);
            Field Field_IMPL_LOOKUP = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            lookup = (MethodHandles.Lookup) unsafe.getObject(unsafe.staticFieldBase(Field_IMPL_LOOKUP), unsafe.staticFieldOffset(Field_IMPL_LOOKUP));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static ClassNode getClass(String className) {
        className = rawPackage(className);
        try {
            ClassReader cr = new ClassReader(className);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);
            // MixinJs.LOGGER.info("{}", cn.methods.stream().map(c -> c.name).toList());
            return cn;
        } catch (Exception e) {
            throw new IllegalArgumentException("Not found class: " + className, e);
        }
    }

    public static String rawPackage(String className) {
        return className.replace('.', '/');
    }

    public static Set<String> getLoadedClasses() {
        IClassTracker tracker = mixinService.getClassTracker();
        Set<String> set = Set.of();
        try {
            if (!(tracker instanceof ModLauncherClassTracker mlct)) return set;
            Field Field_loadedClasses = mlct.getClass().getDeclaredField("loadedClasses");
            Field_loadedClasses.setAccessible(true);
            set = (Set<String>) Field_loadedClasses.get(mlct);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return set;
    }

    public static void addToFile(String name, byte[] bytes) {
        try {
            Path path = mixinClassPath.resolve(name + ".class");
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, bytes);
        } catch (IOException e) {
            MixinJs.LOGGER.info("Failed to write generated mixin class: {}", name, e);
        }
    }

    /**
     * Parse a descriptor as a parameter and return value type
     * @param descriptor descriptor that will be parsed
     * @return the last one is the return value type, the rest are parameter types
     */
    public static List<String> parseDescriptor(String descriptor) {
        // From org.objectweb.asm.Type#getArgumentsAndReturnSizes
        List<String> params = new ArrayList<>();
        int currentOffset = 1;
        char currentChar = descriptor.charAt(currentOffset);
        while (currentChar != ')') {
            int start = currentOffset, end = currentOffset;
            if (currentChar == '[') {
                while (descriptor.charAt(currentOffset) == '[') {
                    currentOffset++;
                }
            }
            currentChar = descriptor.charAt(currentOffset);
            // ZCBSIFJD
            if (currentChar == 'Z' || currentChar == 'C' ||
                    currentChar == 'B' || currentChar == 'S' ||
                    currentChar == 'I' || currentChar == 'F' ||
                    currentChar == 'J' || currentChar == 'D'
            ) {
                currentOffset++;
                end = currentOffset;
            } else if (descriptor.charAt(currentOffset) == 'L') {
                end = descriptor.indexOf(';', currentOffset) + 1;
                currentOffset = Math.max(currentOffset, end);
            }
            params.add(descriptor.substring(start, end));
            currentChar = descriptor.charAt(currentOffset);
        }
        params.add(descriptor.substring(currentOffset + 1));
        return params;
    }

    public static boolean isPrimitive(String descriptor) {
        return "ZCBSIFJD".contains(descriptor);
    }

    public static String primitiveToWrapper(String className) {
        return switch (className) {
            case "Z" -> "Ljava/lang/Boolean;";
            case "C" -> "Ljava/lang/Character;";
            case "B" -> "Ljava/lang/Byte;";
            case "S" -> "Ljava/lang/Short;";
            case "I" -> "Ljava/lang/Integer;";
            case "F" -> "Ljava/lang/Float;";
            case "J" -> "Ljava/lang/Long;";
            case "D" -> "Ljava/lang/Double;";
            default -> throw new IllegalStateException("Unexpected value: " + className);
        };
    }

    /**
     * @see #loadClass(String, byte[])
     * @param className the class name to be loaded
     * @param classNode the class node
     * @return the loaded class
     */
    public static Class<?> loadClass(String className, ClassNode classNode) {
        // Must recompute frames
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        byte[] bytes = cw.toByteArray();
        return loadClass(className, bytes);
    }

    /**
     * The update of Java 17 no longer allows reflection calls to non-self methods in module.
     * So these codes below are invalid.
     * <blockquote><pre>{@code
     * Class<?> Instance_ClassLoader = Class.forName("java.lang.ClassLoader");
     * Method Method_defineClass = Instance_ClassLoader.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
     * Method_defineClass.setAccessible(true); // No access permission
     * Class<?> loadedClass = (Class<?>) Method_defineClass.invoke(classLoader, className, bytes, 0, bytes.length);
     * loadedClass.newInstance();
     * }</pre></blockquote>
     * @param className the class name to be loaded
     * @param bytes The class bytes to be loaded
     * @return the loaded class
     */
    public static Class<?> loadClass(String className, byte[] bytes) {
        try {
            // Get MethodHandle and define class
            MethodType methodType = MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class);
            MethodHandle handle = lookup.findVirtual(ClassLoader.class, "defineClass", methodType);
            // Load class with our new codes
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return (Class<?>) handle.invoke(classLoader, className, bytes, 0, bytes.length);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
