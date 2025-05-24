package dev.sebastianb.traits4jminecraft;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

public class MixinTraitExtension implements IExtension {

    private final String mixinPackage;

    public MixinTraitExtension(String mixinPackage) {
        this.mixinPackage = mixinPackage;
    }


    @Override
    public boolean checkActive(MixinEnvironment environment) {
        return true; // yeah
    }

    @Override
    public void preApply(ITargetClassContext context) {
        ClassInfo info = context.getClassInfo();
        System.out.println("PREAPPLY");
        if (!info.isMixin()) {

        }
    }

    @Override
    public void postApply(ITargetClassContext context) {
        ClassInfo info = context.getClassInfo();

        if (!info.isMixin()) {
            ClassNode node = context.getClassNode();
            System.out.println("APPLY");
            System.out.println(node.signature);
        }
    }

    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {

    }
}
