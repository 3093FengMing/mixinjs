package me.fengming.mixinjs.script.js;

import org.objectweb.asm.AnnotationVisitor;

@SuppressWarnings("unused")
public class AtJS {
    public static final AtJS HEAD = new AtJS("HEAD");
    public static final AtJS RETURN = new AtJS("RETURN");
    public static final AtJS TAIL = new AtJS("TAIL");
    public static final AtJS INVOKE = new AtJS("INVOKE");
    public static final AtJS INVOKE_ASSIGN = new AtJS("INVOKE_ASSIGN");
    public static final AtJS FIELD = new AtJS("FIELD");
    public static final AtJS NEW = new AtJS("NEW");
    public static final AtJS INVOKE_STRING = new AtJS("INVOKE_STRING");
    public static final AtJS JUMP = new AtJS("JUMP");
    public static final AtJS CONSTANT = new AtJS("CONSTANT");

    public final String value;
    public String target;

    public AtJS(String value) {
        this.value = value;
    }

    public AtJS target(String target) {
        this.target = target;
        return this;
    }

    public void visit(AnnotationVisitor av) {
        av.visit("value", value);
        if (target != null) {
            av.visit("target", target);
        }
    }

}
