package me.fengming.mixinjs.script.js;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.*;

import java.util.*;

@SuppressWarnings("unused")
public class InjectorJS {
    protected static final Map<String, InjectorJS> injectorsMap = new HashMap<>();
    protected static final Set<String> ciSets = new HashSet<>();

    protected AtFlag atFlag;

    public final String className;
    public String target;
    public boolean cancellable = false;
    public AtJS at;

    static {
        // SpongePowered Mixin
        injectorsMap.put("overwrite", new InjectorJS(Overwrite.class.getName(), AtFlag.NO_AT));
        injectorsMap.put("inject", new InjectorJS(Inject.class.getName(), AtFlag.MULTI));
        injectorsMap.put("redirect", new InjectorJS(Redirect.class.getName(), AtFlag.SINGLE));
        injectorsMap.put("modifyarg", new InjectorJS(ModifyArg.class.getName(), AtFlag.SINGLE));
        injectorsMap.put("modifyargs", new InjectorJS(ModifyArgs.class.getName(), AtFlag.SINGLE));
        injectorsMap.put("modifyvariable", new InjectorJS(ModifyVariable.class.getName(), AtFlag.SINGLE));
        injectorsMap.put("modifyconstant", new InjectorJS(ModifyConstant.class.getName(), AtFlag.NO_AT));
        injectorsMap.put("modify_arg", new InjectorJS(ModifyArg.class.getName(), AtFlag.SINGLE));
        injectorsMap.put("modify_args", new InjectorJS(ModifyArgs.class.getName(), AtFlag.SINGLE));
        injectorsMap.put("modify_variable", new InjectorJS(ModifyVariable.class.getName(), AtFlag.SINGLE));
        injectorsMap.put("modify_constant", new InjectorJS(ModifyConstant.class.getName(), AtFlag.NO_AT));
        // Mixin Extras
        injectorsMap.put("modifyexpressionvalue", new InjectorJS(ModifyExpressionValue.class.getName(), AtFlag.MULTI));
        injectorsMap.put("modifyreceiver", new InjectorJS(ModifyReceiver.class.getName(), AtFlag.MULTI));
        injectorsMap.put("modifyreturnvalue", new InjectorJS(ModifyReturnValue.class.getName(), AtFlag.MULTI));
        injectorsMap.put("wrapoperation", new InjectorJS(WrapOperation.class.getName(), AtFlag.MULTI));
        injectorsMap.put("wrapmethod", new InjectorJS(WrapMethod.class.getName(), AtFlag.NO_AT));
        injectorsMap.put("modify_expression_value", new InjectorJS(ModifyExpressionValue.class.getName(), AtFlag.MULTI));
        injectorsMap.put("modify_receiver", new InjectorJS(ModifyReceiver.class.getName(), AtFlag.MULTI));
        injectorsMap.put("modify_return_value", new InjectorJS(ModifyReturnValue.class.getName(), AtFlag.MULTI));
        injectorsMap.put("wrap_operation", new InjectorJS(WrapOperation.class.getName(), AtFlag.MULTI));
        injectorsMap.put("wrap_method", new InjectorJS(WrapMethod.class.getName(), AtFlag.NO_AT));
    }

    protected InjectorJS(String className, AtFlag atFlag) {
        this.className = className;
        this.atFlag = atFlag;
    }

    public static InjectorJS build(String name) {
        return injectorsMap.get(name);
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

    public boolean hasCI() {
        return Inject.class.getName().equals(className);
    }

    public boolean isMultiAt() {
        return atFlag == AtFlag.MULTI;
    }

    @Override
    public String toString() {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    public enum AtFlag {
        MULTI, SINGLE, NO_AT
    }
}
