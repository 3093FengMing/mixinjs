package me.fengming.mixinjs.script.js;

import me.fengming.mixinjs.core.MixinClassGenerator;
import me.fengming.mixinjs.script.MixinScriptManager;

import java.util.ArrayList;
import java.util.List;

public class MixinBuilderJS {
    public final String id;
    public final String target;
    public final List<MixinMethod> methods = new ArrayList<>();

    public MixinBuilderJS(String id, String target) {
        this.id = id;
        this.target = target;
    }

    public MixinBuilderJS method(String target, InjectorJS injector, MixinHandler<?> caller, String desc, boolean isStatic, boolean withReturn) {
        methods.add(new MixinMethod(target, injector, caller, desc, isStatic, withReturn));
        return this;
    }

    public void build() {
        MixinClassGenerator.generate(this);
        methods.forEach(e -> MixinScriptManager.addHandler(getHandlerName(e), e.handler()));
    }

    public static String getHandlerName(MixinMethod m) {
        // handler_init_Inject_RETURN
        return "handler_" + m.target() + "_" + m.injector().name + "_" + m.injector().at.value();
    }
}
