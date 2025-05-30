package dev.sebastianb.traits4jminecraft;

import dev.sebastianb.traits4jminecraft.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class CommonClass {

    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void init() {
        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
        Constants.LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));

        // It is common for all supported loaders to provide a similar feature that can not be used directly in the
        // common code. A popular way to get around this is using Java's built-in service loader feature to create
        // your own abstraction layer. You can learn more about this in our provided services class. In this example
        // we have an interface in the common code and use a loader specific implementation to delegate our call to
        // the platform specific approach.
        if (Services.PLATFORM.isModLoaded("traits4jminecraft")) {
            Constants.LOG.info("Hello to traits4jminecraft yeah");
        }

//        class TestTrait implements TraitExample {
//
//            public String test() {
//                return "The value is " + exampleTraitVariable().get();
//            }
//
//        }
//        System.out.println("_____");
//        TestTrait testTraitOne = new TestTrait();
//        System.out.println("The testTraitOne object hash is " + testTraitOne.hashCode());
//        System.out.println("Initial exampleTraitVariable set value is " + testTraitOne.exampleTraitVariable().get());
//        testTraitOne.exampleTraitVariable().set(32);
//        System.out.println("After exampleTraitVariable set value is " + testTraitOne.exampleTraitVariable().get());
//
//        System.out.println("_____");
//        TestTrait testTraitTwo = new TestTrait();
//        System.out.println("The testTraitTwo object hash is " + testTraitTwo.hashCode());
//        System.out.println("Initial exampleTraitVariable set value is " + testTraitTwo.exampleTraitVariable().get());
//
//        System.out.println("_____");
//        System.out.println("testTraitOne exampleTraitVariable still has the value of " + testTraitOne.exampleTraitVariable().get());
//
//
//        System.out.println("_____");
//        System.out.println("testTraitOne exampleOneTraitVariable is " + testTraitOne.exampleOneTraitVariable().get());
//        System.out.println("testTraitTwo exampleOneTraitVariable is " + testTraitTwo.exampleOneTraitVariable().get());
//        testTraitOne.exampleOneTraitVariable().set(94);
//        System.out.println("exampleOneTraitVariable set to 94");
//        System.out.println("testTraitOne exampleOneTraitVariable is " + testTraitOne.exampleOneTraitVariable().get());
//        System.out.println("testTraitTwo exampleOneTraitVariable is " + testTraitTwo.exampleOneTraitVariable().get());
//
//
//        System.out.println(testTraitOne.getClass().getName());

    }
}
