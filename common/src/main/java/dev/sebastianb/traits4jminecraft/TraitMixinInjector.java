package dev.sebastianb.traits4jminecraft;

import dev.sebastianb.traits4jminecraft.trait.MinecraftTestTrait;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

// TODO: do this
public class TraitMixinInjector implements IMixinConfigPlugin {
    @Override
    public void onLoad(String s) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String s, String s1) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {
        for (String cls : set) {
            System.out.println("Seen class: " + cls);
        }

    }

    @Override
    public List<String> getMixins() {
        System.out.println("MEOWR MIXINS");
        return List.of(MinecraftTestTrait.class.getSimpleName());
    }

    @Override
    public void preApply(String s, org.objectweb.asm.tree.ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, org.objectweb.asm.tree.ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}
