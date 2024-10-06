package me.fengming.mixinjs.core;

import me.fengming.mixinjs.MixinJs;
import me.fengming.mixinjs.Utils;
import me.fengming.mixinjs.script.js.AtJS;
import me.fengming.mixinjs.script.js.InjectorJS;
import me.fengming.mixinjs.script.js.MixinBuilderJS;
import me.fengming.mixinjs.script.js.MixinMethod;
import org.objectweb.asm.*;

import java.util.List;

public class MixinClassGenerator {

    protected static final String MIXIN_PACKAGE = "mixinjs/generated/";

    protected static final String CI = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";

    public static void generate(MixinBuilderJS builder) {
        MixinJs.LOGGER.info("[MixinJs] Generating mixin class: {} for {}", builder.id, builder.target);

        String className = MIXIN_PACKAGE + builder.id;
        String target = builder.target;
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, className, null, "java/lang/Object", null);

        // Visit @Mixin annotation
        AnnotationVisitor av0;
        av0 = cw.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
        AnnotationVisitor av1 = av0.visitArray("value");
        av1.visit(null, Type.getType(strRef2desc(Utils.rawPackage(target))));
        av1.visitEnd();
        av0.visitEnd();

        for (MixinMethod method : builder.methods) {
            generateMethod(cw, method, className);
        }

        // Utils.getLoadedClasses();
        // Load it!!!
        Utils.addToFile(className, cw.toByteArray());
    }

    public static void generateMethod(ClassWriter cw, MixinMethod method, String thisClass) {
        MethodVisitor mv;
        AnnotationVisitor av0;

        // Utils.getLoadedClasses();

        InjectorJS injector = method.injector();

        String annotationType = injector.className;
        String handlerName = MixinBuilderJS.getHandlerName(method);
        List<String> params = Utils.parseDescriptor(method.desc());
        String returnValue = params.getLast();
        boolean withReturn = !returnValue.equals("V");
        params.removeLast();
        int paramsCount = params.size() - 1;


        // Visit mixin method annotation
        mv = cw.visitMethod(Opcodes.ACC_PRIVATE | (method.isStatic() ? Opcodes.ACC_STATIC : 0), handlerName,
                method.desc(), null, null);
        {
            // @Inject like
            {
                av0 = mv.visitAnnotation(strRef2desc(Utils.rawPackage(annotationType)), true);
                AnnotationVisitor av1 = av0.visitArray("method");
                av1.visit(null, injector.target);
                av1.visitEnd();
                av0.visit("cancellable", injector.cancellable);
            }
            // @At
            {
                AnnotationVisitor av1, av2;
                if (injector.isMultiAt()) {
                    av2 = av0.visitArray("at");
                    av1 = av2.visitAnnotation(null, "Lorg/spongepowered/asm/mixin/injection/At;");
                    visitAt(injector.at, av1);
                    av1.visitEnd();
                    av2.visitEnd();
                } else {
                    av1 = av0.visitAnnotation("at", "Lorg/spongepowered/asm/mixin/injection/At;");
                    visitAt(injector.at, av1);
                    av1.visitEnd();
                }
            }
            av0.visitEnd();
        }

        // Visit mixin method
        {
            mv.visitCode();
            Label label0 = new Label();
            mv.visitLabel(label0);
            // scriptName
            mv.visitLdcInsn(handlerName);
            // callbackInfo
            if (injector.hasCI()) {
                mv.visitVarInsn(Opcodes.ALOAD, paramsCount + 1);
            } else {
                mv.visitInsn(Opcodes.ACONST_NULL);
            }
            // thisObject
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            // args
            visitMutableInt(mv, paramsCount);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            if (paramsCount != 0) {
                visitParamsArray(mv, params, withReturn);
            }
            // call runScript
            if (withReturn) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "me/fengming/mixinjs/core/MixinClassUtils", "runScriptR", "(Ljava/lang/String;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
                mv.visitTypeInsn(Opcodes.CHECKCAST, desc2ref(returnValue));
            } else {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "me/fengming/mixinjs/core/MixinClassUtils", "runScript", "(Ljava/lang/String;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;Ljava/lang/Object;[Ljava/lang/Object;)V", false);
            }
            // return
            mv.visitInsn(withReturn ? Opcodes.ARETURN : Opcodes.RETURN);
            Label label2 = new Label();
            mv.visitLabel(label2);
            mv.visitLocalVariable("this", strRef2desc(thisClass), null, label0, label2, 0);
            // Load method params
            for (int i = 1; i < params.size(); i++) {
                mv.visitLocalVariable("arg" + i, params.get(i), null, label0, label2, i);
            }
            if (injector.hasCI()) {
                // Load ci, don't worry about CallbackInfoReturnable, as Mixin will automatically cast it
                mv.visitLocalVariable("ci", CI, null, label0, label2, paramsCount + 1);
            }
            mv.visitMaxs(0, 0); // ignored
            mv.visitEnd();
        }

    }

    private static void visitMutableInt(MethodVisitor mv, int value) {
        if (value > 32767 || value < -32768) {
            mv.visitLdcInsn(value);
        } else if (value > 127 || value < -128) {
            mv.visitIntInsn(Opcodes.SIPUSH, value);
        } else if (value > 5 || value < -1) {
            mv.visitIntInsn(Opcodes.BIPUSH, value);
        } else {
            mv.visitInsn(Opcodes.ICONST_0 + value);
        }
    }

    private static void visitParamsArray(MethodVisitor mv, List<String> params, boolean includeLast) {
        int l = includeLast ? params.size() : params.size() - 1;
        for (int i = 0; i < l; i++) {
            String p = params.get(i);
            Type type = Type.getType(p);
            mv.visitInsn(Opcodes.DUP);
            visitMutableInt(mv, i);
            if (type.getSort() != Type.OBJECT) {
                mv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), i + 1);
                String wrapper = desc2ref(Utils.primitiveToWrapper(p));
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, wrapper, "valueOf",
                        "(" + p + ")" + Utils.primitiveToWrapper(p),
                        false);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, i + 1);
            }
            mv.visitInsn(Opcodes.AASTORE);
        }
    }

    private static void visitAt(AtJS at, AnnotationVisitor av) {
        av.visit("value", at.value);
        if (at.target != null) {
            av.visit("target", at.target);
        }
    }

    private static String strRef2desc(String clazz) {
        return "L" + clazz + ";";
    }

    private static String desc2ref(String desc) {
        return desc.substring(1, desc.length() - 1);
    }

}
