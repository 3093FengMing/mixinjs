package me.fengming.mixinjs.script;

public class MixinScript {
    protected String name;

    public MixinScript(String scriptName) {
        this.name = scriptName;
    }

    public String getName() {
        return this.name;
    }

    public void run() {

    }
}
