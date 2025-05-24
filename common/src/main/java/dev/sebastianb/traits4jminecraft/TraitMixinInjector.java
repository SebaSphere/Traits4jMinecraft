package dev.sebastianb.traits4jminecraft;

import com.google.common.reflect.ClassPath;
import dev.sebastianb.traits4jminecraft.trait.MinecraftTestTrait;
import net.terradevelopment.traits4j.PreMain;
import net.terradevelopment.traits4j.annotations.Trait;
import org.objectweb.asm.*;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionClassExporter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

// TODO: do this
public class TraitMixinInjector implements IMixinConfigPlugin {

    private final List<String> mixins = new ArrayList<>();

    private static final String GENERATED_PACKAGE = "dev.sebastianb.traits4jminecraft.gen.mixin";

    @Override
    public void onLoad(String rawMixinPackage) {
        System.out.println("MIXIN INJECTION STARTED FROM " + rawMixinPackage);
        ClassLoader loader = TraitMixinInjector.class.getClassLoader();

        String mixinPackage = rawMixinPackage.replace('.', '/');
        ArrayList<String> classes = new ArrayList<>();
        // get all loaded classes in mod
        try {
            ClassPath classPath = ClassPath.from(loader);
            for (ClassPath.ClassInfo classInfo : classPath.getAllClasses()) {
                // TODO: make it any mod ID main package
                if (classInfo.getName().startsWith("dev.sebastianb.traits4jminecraft")) {
                    Class<?> clazz = classInfo.load();
                    for (Annotation annotation : clazz.getAnnotations()) {
                        // for some reason, it's not possible to use the annotation directly?
                        if (annotation.annotationType().getName().equals(Trait.class.getName())) {
                            mixins.add(clazz.getName());

                            // FIXME: maybe each mixin could have a unique gened name rather then making a fake package
                            String genName = "dev.sebastianb.traits4jminecraft.gen.mixin." + clazz.getName();
                            System.out.println("gener name " + genName);

                            // TODO: somehow generate classes here for each mixin
                        }
                        System.out.println(clazz.getName());
                        System.out.println("  Found annotation: " + annotation.annotationType());
                    }
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Object transformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
        if (transformer == null) throw new IllegalStateException("Not running with a transformer?");

        // Block of code from FabricASM, I think this is transformer stuff on mixins?
        Extensions extensions = null;
        try {
            for (Field f : transformer.getClass().getDeclaredFields()) {
                if (f.getType() == Extensions.class) {
                    f.setAccessible(true);
                    extensions = (Extensions) f.get(transformer);
                    break;
                }
            }

            if (extensions == null) {
                String foundFields = Arrays.stream(transformer.getClass().getDeclaredFields()).map(f -> f.getType() + " " + f.getName()).collect(Collectors.joining(", "));
                throw new NoSuchFieldError("Unable to find extensions field, only found " + foundFields);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Running with a transformer that doesn't have extensions?", e);
        }


        extensions.add(new MixinTraitExtension(GENERATED_PACKAGE));


        // FabricASM uses ExtensionClassExporter after this, I think it's debug stuff??? Not 100% sure

        // TODO: I need to generate a class at runtime for getMixins. Example filled out is "dev.sebastianb.traits4jminecraft.gen.mixin.dev.sebastianb.traits4jminecraft.trait.MinecraftTestTrait"

        System.out.println("TRAIT CLASSES: " + mixins);


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

        return mixins;
    }

    @Override
    public void preApply(String s, org.objectweb.asm.tree.ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, org.objectweb.asm.tree.ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}
