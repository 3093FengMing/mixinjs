package me.fengming.mixinjs.core;

import me.fengming.mixinjs.MixinJs;
import me.fengming.mixinjs.Utils;
import me.fengming.mixinjs.script.js.MixinBuilderJS;
import me.fengming.mixinjs.script.js.MixinMethod;
import org.objectweb.asm.*;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.HashMap;
import java.util.List;

public class MixinClassGenerator {

    protected static final String MIXIN_PACKAGE = "mixinjs/generated/";

    protected static final String CI = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
    protected static final String CIR = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";
    protected static final String CIRG = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable<{G}>;";

    protected static HashMap<String, Class<?>> injectorsMap = new HashMap<>();

    static {
        // SpongePowered Mixin
        injectorsMap.put("overwrite", Overwrite.class);
        injectorsMap.put("inject", Inject.class);
        // injectorsMap.put("redirect", Redirect.class);
        // injectorsMap.put("modifyarg", ModifyArg.class);
        // injectorsMap.put("modifyargs", ModifyArgs.class);
        // injectorsMap.put("modifyvariable", ModifyVariable.class);
        // injectorsMap.put("modifyconstant", ModifyConstant.class);
        // injectorsMap.put("modify_arg", ModifyArg.class);
        // injectorsMap.put("modify_args", ModifyArgs.class);
        // injectorsMap.put("modify_variable", ModifyVariable.class);
        // injectorsMap.put("modify_constant", ModifyConstant.class);
        // Mixin Extras
        // injectorsMap.put("modifyexpressionvalue", ModifyExpressionValue.class);
        // injectorsMap.put("modifyreceiver", ModifyReceiver.class);
        // injectorsMap.put("modifyreturnvalue", ModifyReturnValue.class);
        // injectorsMap.put("wrapoperation", WrapOperation.class);
        // injectorsMap.put("wrapmethod", WrapMethod.class);
        // injectorsMap.put("modify_expression_value", ModifyExpressionValue.class);
        // injectorsMap.put("modify_receiver", ModifyReceiver.class);
        // injectorsMap.put("modify_return_value", ModifyReturnValue.class);
        // injectorsMap.put("wrap_operation", WrapOperation.class);
        // injectorsMap.put("wrap_method", WrapMethod.class);
    }

    public static void generate(MixinBuilderJS builder) {
        MixinJs.LOGGER.info("[MixinJs] Generating mixin class: {}", builder.target);

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

        String annotationType = method.injector().className;
        String handlerName = MixinBuilderJS.getHandlerName(method);
        List<String> params = Utils.parseDescriptor(method.desc());
        String returnValue = params.getLast();
        params.removeLast();
        int paramsCount = params.size() - 1;

        // Visit mixin method annotation
        mv = cw.visitMethod(Opcodes.ACC_PRIVATE | (method.isStatic() ? Opcodes.ACC_STATIC : 0), handlerName,
                method.desc(), method.withReturn() ? method.desc() : null, null);
        {
            // @Inject like
            {
                av0 = mv.visitAnnotation(strRef2desc(Utils.rawPackage(annotationType)), true);
                AnnotationVisitor av1 = av0.visitArray("method");
                av1.visit(null, method.injector().target);
                av1.visitEnd();
            }
            // @At
            {
                AnnotationVisitor av1 = av0.visitArray("at");
                AnnotationVisitor av2 = av1.visitAnnotation(null, "Lorg/spongepowered/asm/mixin/injection/At;");
                av2.visit("value", method.injector().at.value());
                av2.visitEnd();
                av1.visitEnd();
            }
            av0.visitEnd();
        }

        // Visit mixin method
        {
            mv.visitCode();
            Label label0 = new Label();
            mv.visitLabel(label0);
            mv.visitLdcInsn(handlerName);
            mv.visitVarInsn(Opcodes.ALOAD, paramsCount + 1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            visitMutableInt(mv, paramsCount);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            if (paramsCount != 0) {
                mv.visitInsn(Opcodes.DUP);
                visitParamsArray(mv, params);
                mv.visitInsn(Opcodes.AASTORE);
            }
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "me/fengming/mixinjs/core/MixinClassUtils", "runScript", "(Ljava/lang/String;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;Ljava/lang/Object;[Ljava/lang/Object;)V", false);
            mv.visitInsn(Opcodes.RETURN);
            Label label2 = new Label();
            mv.visitLabel(label2);
            mv.visitLocalVariable("this", strRef2desc(thisClass), null, label0, label2, 0);
            // Load method params
            for (int i = 1; i < params.size(); i++) {
                mv.visitLocalVariable("arg" + i, params.get(i), null, label0, label2, i);
            }
            // Load ci/cir
            if (returnValue.equals("V")) {
                mv.visitLocalVariable("cir", CIR, CIRG.replace("{G}", returnValue), label0, label2, paramsCount + 1);
            } else {
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

    private static void visitParamsArray(MethodVisitor mv, List<String> params) {
        for (int i = 0; i < params.size(); i++) {
            String p = params.get(i);
            Type type = Type.getType(p);
            visitMutableInt(mv, i);
            if (type.getSort() != Type.OBJECT) {
                mv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), i + 1);
                String wrapper = Utils.primitiveToWrapper(p);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, wrapper, "valueOf",
                        "(" + p + ")" + Utils.primitiveToWrapper(p),
                        false);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, i + 1);
            }
            mv.visitInsn(Opcodes.AASTORE);
            mv.visitInsn(Opcodes.DUP);
        }
    }

    private static String strRef2desc(String clazz) {
        return "L" + clazz + ";";
    }

}
