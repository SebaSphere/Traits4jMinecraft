package dev.sebastianb.traits4jminecraft;

import com.google.common.reflect.ClassPath;
import net.terradevelopment.traits4j.annotations.Trait;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LightweightInjector implements IMixinConfigPlugin {

    Set<String> fakeMixinPath = new HashSet<>();

    private static final String GENERATED_PACKAGE = "dev.sebastianb.traits4jminecraft.gen.mixin";


    @Override
    public void onLoad(String mixinPackage) {
        // get all loaded classes in mod

        ClassLoader loader = this.getClass().getClassLoader();

        try {
            ClassPath classPath = ClassPath.from(loader);
            for (ClassPath.ClassInfo classInfo : classPath.getAllClasses()) {
                // TODO: make it any mod ID main package
                if (classInfo.getName().startsWith("dev.sebastianb.traits4jminecraft")) {
                    Class<?> clazz = classInfo.load();
                    for (Annotation annotation : clazz.getAnnotations()) {
                        // for some reason, it's not possible to use the annotation directly?
                        if (annotation.annotationType().getName().equals(Trait.class.getName())) {
                            fakeMixinPath.add(clazz.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("TEST!");

        System.out.println(fakeMixinPath);

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {


        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, GENERATED_PACKAGE, null, "java/lang/Object", null);
        writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, GENERATED_PACKAGE, null, "java/lang/Object", null);
        AnnotationVisitor mixin = writer.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
        mixin.visit("remap", false);
        AnnotationVisitor values = mixin.visitArray("value");



        values.visitEnd();
        mixin.visitEnd();
        writer.visitEnd();

        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
