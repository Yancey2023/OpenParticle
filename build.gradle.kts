plugins {
    // If you're using Kotlin, it needs to be applied before the multi-version plugin
    // kotlin("jvm")
    // Apply the multi-version plugin, this does all the configuration necessary for the preprocessor to
    // work. In particular it also applies `com.replaymod.preprocess`.
    // In addition it primarily also provides a `platform` extension which you can use in this build script
    // to get the version and mod loader of the current project.
    id("gg.essential.multi-version")
    // If you do not care too much about the details, you can just apply essential-gradle-toolkits' defaults for
    // Minecraft, fabric-loader, forge, mappings, etc. versions.
    // You can also overwrite some of these if need be. See the `gg.essential.defaults.loom` README section.
    // Otherwise you'll need to configure those as usual for (architectury) loom.
    id("gg.essential.defaults")
}

val mcVersion = platform.mcVersion

version = "${project.name}-${rootProject.version}"
base.archivesName.set("openparticle")
java.withSourcesJar()

val fabricApiVersion = when (mcVersion) {
    11600, 11601, 11602, 11603, 11604, 11605 -> "0.42.0+1.16"
    11700, 11701 -> "0.46.1+1.17"
    11800, 11801 -> "0.44.0+1.18"
    11802 -> "0.77.0+1.18.2"
    11900 -> "0.58.0+1.19"
    11901 -> "0.58.5+1.19.1"
    11902 -> "0.77.0+1.19.2"
    11903 -> "0.76.1+1.19.3"
    11904 -> "0.87.2+1.19.4"
    12000 -> "0.83.0+1.20"
    12001 -> "0.92.2+1.20.1"
    12002 -> "0.91.6+1.20.2"
    12003 -> "0.91.1+1.20.3"
    12004 -> "0.97.1+1.20.4"
    12005 -> "0.97.8+1.20.5"
    12006 -> "0.100.8+1.20.6"
    12100 -> "0.102.0+1.21"
    12101 -> "0.107.0+1.21.1"
    12102 -> "0.106.1+1.21.2"
    12103 -> "0.107.0+1.21.3"
    else -> throw UnsupportedOperationException()
}

dependencies {
    // If you are depending on a multi-version library following the same scheme as the Essential libraries (that is
    // e.g. `elementa-1.8.9-forge`), you can `toString` `platform` directly to get the respective artifact id.
    // modImplementation("gg.essential:elementa-$platform:428")
    if (platform.isFabric) {
        val fabricApiModules = mutableListOf(
            "fabric-api-base",
            "fabric-networking-v0",
            "fabric-command-api-v1",
            "fabric-rendering-v1",
            "fabric-lifecycle-events-v1",
            "fabric-keybindings-v0",
        )
        if (mcVersion >= 11604) {
            fabricApiModules.remove("fabric-networking-v0")
            fabricApiModules.add("fabric-networking-api-v1")
        }
        if (mcVersion >= 11700) {
            fabricApiModules.remove("fabric-rendering-v1")
            fabricApiModules.add("fabric-renderer-registries-v1")
        }
        if (mcVersion >= 11900) {
            fabricApiModules.remove("fabric-command-api-v1")
            fabricApiModules.add("fabric-command-api-v2")
        }
        if (mcVersion >= 11802) {
            fabricApiModules.remove("fabric-renderer-registries-v1")
            fabricApiModules.add("fabric-rendering-v1")
        }

        for (module in fabricApiModules) {
            val dep = fabricApi.module(module, fabricApiVersion)
            modImplementation(dep)
            "include"(dep)
        }
    }
}

loom {
    // If you need to use a tweaker on legacy (1.12.2 and below) forge:
    // if (platform.isLegacyForge) {
    //     launchConfigs.named("client") {
    //         arg("--tweakClass", "gg.essential.loader.stage0.EssentialSetupTweaker")
    //         // And maybe a core mod?
    //         //  property("fml.coreMods.load", "com.example.asm.CoreMod")
    //     }
    // }
    // Mixin on forge? (for legacy forge you will still need to register a tweaker to set up mixin)
    // if (platform.isForge) {
    //     forge {
    //         mixinConfig("openparticle.mixins.json")
    //         // And maybe an access transformer?
    //         // Though try to avoid these, cause they are not automatically translated to Fabric's access widener
    //         accessTransformer(project.parent.file("src/main/resources/openparticle.cfg"))
    //     }
    // }
}