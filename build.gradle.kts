plugins {
    java
    `maven-publish`

    // Nothing special about this, just keep it up to date
    id("com.github.johnrengelman.shadow") version "8.1.0" apply false

    // In general, keep this version in sync with upstream. Sometimes a newer version than upstream might work, but an older version is extremely likely to break.
    id("io.papermc.paperweight.patcher") version "1.5.3"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl) {
        content { onlyForConfigurations(configurations.paperclip.name) }
    }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.8.6:fat")
    decompiler("net.minecraftforge:forgeflower:2.0.627.2")
    paperclip("io.papermc:paperclip:3.0.2")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven("https://repo.codemc.io/repository/nms/") // Required for AdvancedSlimePaper
        maven("https://repo.rapture.pw/repository/maven-releases/") // Required for AdvancedSlimePaper
        maven("https://repo.glaremasters.me/repository/concuncan/") // Required for AdvancedSlimePaper
        maven("https://jitpack.io") // Required for Pufferfish (Simple-YAML)
    }
}

paperweight {
    serverProject.set(project(":candle-server"))

    remapRepo.set(paperMavenPublicUrl)
    decompileRepo.set(paperMavenPublicUrl)
    useStandardUpstream("purpur") {
        url.set(github("PurpurMC", "Purpur"))
        ref.set(providers.gradleProperty("purpurRef"))

        withStandardPatcher {
            apiSourceDirPath.set("Purpur-API")
            serverSourceDirPath.set("Purpur-Server")

            apiPatchDir.set(layout.projectDirectory.dir("patches/api"))
            serverPatchDir.set(layout.projectDirectory.dir("patches/server"))

            apiOutputDir.set(layout.projectDirectory.dir("candle-api"))
            serverOutputDir.set(layout.projectDirectory.dir("candle-server"))
        }
    }
}


tasks.generateDevelopmentBundle {
    apiCoordinates.set("de.beyondblocks.candle:candle-api")
    mojangApiCoordinates.set("io.papermc.paper:paper-mojangapi")
    libraryRepositories.set(
        listOf(
            "https://repo.maven.apache.org/maven2/",
            paperMavenPublicUrl,
            "https://repo.byquanton.eu/candle"
        )
    )
}

allprojects {
    publishing {
        repositories {
            maven {
                name = "byquantonRepo"
                url = uri("https://repo.byquanton.eu/candle/")
                credentials(PasswordCredentials::class)
            }
        }
    }
}

publishing {
    if (project.hasProperty("publishDevBundle")) {
        publications.create<MavenPublication>("devBundle") {
            artifact(tasks.generateDevelopmentBundle) {
                artifactId = "dev-bundle"
            }
        }
    }
}
