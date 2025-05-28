package dev.sebastianb.traits4jminecraft;

import com.google.common.reflect.ClassPath;
import dev.sebastianb.traits4jminecraft.trait.MinecraftTestTrait;
import net.terradevelopment.traits4j.PreMain;
import net.terradevelopment.traits4j.annotations.Trait;
import net.terradevelopment.traits4j.clazz.TraitImplementationUtil;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionClassExporter;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// TODO: do this
public class TraitMixinInjector implements IMixinConfigPlugin {


    private Map<String, Set<Consumer<ClassNode>>> tinkerers = new HashMap<>();


    // this was so painful to figure out, tysm fabric asm for the pain of copy paste
    // TODO: actually understand the code better and refractor from there
    private static Consumer<URL> fishAddURL() {
        ClassLoader loader = TraitMixinInjector.class.getClassLoader();
        Method addUrlMethod = null;
        for (Method method : loader.getClass().getDeclaredMethods()) {
			/*System.out.println("Type: " + method.getReturnType());
			System.out.println("Params: " + method.getParameterCount() + ", " + Arrays.toString(method.getParameterTypes()));*/
            if (method.getReturnType() == Void.TYPE && method.getParameterCount() == 1 && method.getParameterTypes()[0] == URL.class) {
                addUrlMethod = method; //Probably
                break;
            }
        }
        if (addUrlMethod == null) throw new IllegalStateException("Couldn't find method in " + loader);
        try {
            addUrlMethod.setAccessible(true);
            MethodHandle handle = MethodHandles.lookup().unreflect(addUrlMethod);
            return url -> {
                try {
                    handle.invoke(loader, url);
                } catch (Throwable t) {
                    throw new RuntimeException("Unexpected error adding URL", t);
                }
            };
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Couldn't get handle for " + addUrlMethod, e);
        }
    }

    private Predicate<URL> urlers = url -> {
        fishAddURL().accept(url);
        return true;
    };

    private final List<String> mixins = new ArrayList<>();

    private static final String GENERATED_PACKAGE = "dev.sebastianb.traits4jminecraft.gen.mixin";

    @Override
    public void onLoad(String rawMixinPackage) {
        System.out.println("MIXIN INJECTION STARTED FROM " + rawMixinPackage);
        ClassLoader loader = this.getClass().getClassLoader();

        String mixinPackage = rawMixinPackage.replace('.', '/') + "/";


        Map<String, Set<String>> preTransforms = new HashMap<>();

        // after transformed
        Map<String, Set<String>> postTransforms = new HashMap<>();

        Map<String, byte[]> classGenerators = new HashMap<>();

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
                            mixins.add(clazz.getSimpleName());

                            preTransforms.computeIfAbsent(clazz.getName().replace(".", "/"), k -> new HashSet<>()).add("<*>");

                            // FIXME: each mixin should have a unique gened name rather then making a fake package
                            // like mixin 1, mixin 2, mixin 3, etc
                            String genName = GENERATED_PACKAGE
                                    .replace(".", "/") + "/" + clazz.getSimpleName();
                            System.out.println("gener name " + genName);

                            // this sets things up for the CasualStreamHandler to load stuff in the jvm
                            classGenerators.put('/' + genName.replace('.', '/') + ".class",
                                    makeMixinBlob(genName.replace('.', '/'), Collections.singleton(clazz.getName().replace('.', '/'))));

                        }
                        System.out.println(clazz.getName());
                        System.out.println("  Found annotation: " + annotation.annotationType());
                    }
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Casual stream handler created");



        for (var transform : preTransforms.entrySet()) {
            System.out.println("EXISTING REFERENCE " + transform.getKey() + " VALUE " + transform.getValue());
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

        ExtensionClassExporter exporter = extensions.getExtension(ExtensionClassExporter.class);
        CasualStreamHandler.dumper = (name, bytes) -> {
            ClassNode node = new ClassNode(); //Read the bytes in as per TreeTransformer#readClass(byte[])
            new ClassReader(bytes).accept(node, ClassReader.EXPAND_FRAMES);
            exporter.export(MixinEnvironment.getCurrentEnvironment(), name, false, node);
        };

        // TODO: I need to generate a class at runtime for getMixins. Example filled out is "dev.sebastianb.traits4jminecraft.gen.mixin.dev.sebastianb.traits4jminecraft.trait.MinecraftTestTrait"

        urlers.test(CasualStreamHandler.create(classGenerators));

        System.out.println("print class gens");
        System.out.println(classGenerators);
        classGenerators.forEach((name, bytes) -> {
            System.out.println("Adding class generator: " + name);
            System.out.println(new String(bytes));

        });

        System.out.println("TRAIT CLASSES: " + mixins);


    }

    static byte[] makeMixinBlob(String name, Collection<? extends String> targets) {
        ClassWriter cw = new ClassWriter(0);
        System.out.println("mixin blob name is " + name);

        cw.visit(52, Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE, name, null, "java/lang/Object", null);

        AnnotationVisitor mixinAnnotation = cw.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
        mixinAnnotation.visit("remap", false);

        AnnotationVisitor targetAnnotation = mixinAnnotation.visitArray("value");
        for (String target : targets) {
            targetAnnotation.visit(null, Type.getType('L' + target + ';'));
        }
        targetAnnotation.visitEnd();
        mixinAnnotation.visitEnd();


        cw.visitEnd();
        return cw.toByteArray();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        System.out.println("mixin class name is " + mixinClassName);
        System.out.println("target class name is " + targetClassName);
        return true;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {

        // print all loaded mixins
        System.out.println("FETCHING MIXINS");
        System.out.println(mixins);



        // WOOO WE REGISTERED A FAKE MIXIN MAYBE IN LOADER STUFF
        return mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // do our silly asm stuff here
        System.out.println("Pre-applying " + targetClassName + " via " + mixinClassName);
        TraitImplementationUtil.modifyClassNode(targetClass);
    }

    @Override
    public void postApply(String s, org.objectweb.asm.tree.ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
        System.out.println("POST APPLY TEST");
    }
}
