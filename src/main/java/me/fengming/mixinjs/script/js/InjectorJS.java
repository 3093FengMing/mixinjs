package me.fengming.mixinjs.script.js;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class InjectorJS {
    public static final Map<String, Class<?>> injectorsMap = new HashMap<>();

    public final String name;
    public final String className;
    public String target;
    public boolean cancellable = false;
    public AtJS at;

    static {
        // SpongePowered Mixin
        injectorsMap.put("overwrite", Overwrite.class);
        injectorsMap.put("inject", Inject.class);
        injectorsMap.put("redirect", Redirect.class);
        injectorsMap.put("modifyarg", ModifyArg.class);
        injectorsMap.put("modifyargs", ModifyArgs.class);
        injectorsMap.put("modifyvariable", ModifyVariable.class);
        injectorsMap.put("modifyconstant", ModifyConstant.class);
        injectorsMap.put("modify_arg", ModifyArg.class);
        injectorsMap.put("modify_args", ModifyArgs.class);
        injectorsMap.put("modify_variable", ModifyVariable.class);
        injectorsMap.put("modify_constant", ModifyConstant.class);
        // Mixin Extras
        injectorsMap.put("modifyexpressionvalue", ModifyExpressionValue.class);
        injectorsMap.put("modifyreceiver", ModifyReceiver.class);
        injectorsMap.put("modifyreturnvalue", ModifyReturnValue.class);
        injectorsMap.put("wrapoperation", WrapOperation.class);
        injectorsMap.put("wrapmethod", WrapMethod.class);
        injectorsMap.put("modify_expression_value", ModifyExpressionValue.class);
        injectorsMap.put("modify_receiver", ModifyReceiver.class);
        injectorsMap.put("modify_return_value", ModifyReturnValue.class);
        injectorsMap.put("wrap_operation", WrapOperation.class);
        injectorsMap.put("wrap_method", WrapMethod.class);
    }

    protected InjectorJS(String name, String className) {
        Objects.requireNonNull(className);
        this.name = name;
        this.className = className;
    }

    public static InjectorJS build(String name) {
        return new InjectorJS(name.toLowerCase(), injectorsMap.getOrDefault(name, null).getName());
    }

    public InjectorJS target(String target) {
        this.target = target;
        return this;
    }

    public InjectorJS cancellable(boolean cancellable) {
        this.cancellable = cancellable;
        return this;
    }

    public InjectorJS at(AtJS at) {
        this.at = at;
        return this;
    }
}
