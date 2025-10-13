plugins {
    java
    alias(libs.plugins.fabric.loom)
}

val modId = property("mod.id").toString()
val modGroup = property("mod.group").toString()
version = property("mod.version").toString()

base.archivesName = "${modId}-${version}"

loom {
    splitEnvironmentSourceSets()

    mods.create(modId) {
        sourceSet(sourceSets.getByName("main"))
        sourceSet(sourceSets.getByName("client"))
    }
}

repositories {
    mavenCentral()

    exclusiveContent {
        forRepository {
            maven("https://maven.terraformersmc.com/") {
                name = "Terraformers"
            }
        }

        filter {
            includeGroup("com.terraformersmc")
        }
    }

    exclusiveContent {
        forRepository {
            maven("https://maven.azureaaron.net/releases") {
                name = "Aaron's Maven"
            }
        }

        filter {
            includeGroup("net.azureaaron")
        }
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn) { classifier("v2") })

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.hm.api)
    include(libs.hm.api)
    modImplementation(libs.modmenu)
}

tasks.processResources {
    val map = mapOf(
        "mod_id" to modId,
        "mod_version" to version,
        "fabric_loader_version" to libs.versions.fabric.loader.get(),
        "fabric_api_version" to libs.versions.fabric.api.get(),
        "minecraft_version" to libs.versions.minecraft.get(),
        "skyblocker_version" to libs.versions.skyblocker.get()
    )

    inputs.properties(map)
    filesMatching("fabric.mod.json") { expand(map) }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.jar { from("LICENSE") { rename { "${it}_${base.archivesName}" } } }