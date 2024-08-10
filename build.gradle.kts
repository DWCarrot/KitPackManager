import io.papermc.paperweight.util.path
//import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    id("java")
    id("maven-publish")
    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("xyz.jpenilla.run-paper") version "2.3.0" // Adds runServer and runMojangMappedServer tasks for testing
    //id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1" // Generates plugin.yml based on the Gradle config
}

val pluginName = "KitPackManager"
val pluginVersion = "1.1"
val paperApiName = "1.20.4-R0.1-SNAPSHOT"
val javaVersion = 17

val buildNumber = System.getenv("BUILD_NUMBER") ?: "local"
val mavenDirectory = System.getenv("MAVEN_DIR") ?: layout.buildDirectory.dir("repo").path.toString()
val javaDocDirectory = System.getenv("JAVADOC_DIR") ?: layout.buildDirectory.dir("javadoc").path.toString()

group = "io.github.dwcarrot"
version = "$pluginVersion-${getMcVersion(paperApiName)}-b$buildNumber"
description = "simple minecraft paper/spigot plugin for manage and distribute kitpack; for 买买镇2022"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    paperweight.paperDevBundle(paperApiName)

}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
}

tasks {

    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(javaVersion)
    }

    javadoc {
        with((options as StandardJavadocDocletOptions)) {
            options.encoding =
                    Charsets.UTF_8.name() // We want UTF-8 for everything
            links("https://docs.oracle.com/en/java/javase/${javaVersion}/docs/api/")
            links("https://guava.dev/releases/21.0/api/docs/")
            links("https://ci.md-5.net/job/BungeeCord/ws/chat/target/apidocs/")
            links("https://jd.papermc.io/paper/1.18/")
            options.locale = "en_US"
            options.encoding = "UTF-8"
            (options as StandardJavadocDocletOptions).addBooleanOption(
                    "keywords",
                    true
            )
            (options as StandardJavadocDocletOptions).addStringOption(
                    "Xdoclint:none",
                    "-quiet"
            )
            (options as StandardJavadocDocletOptions).addBooleanOption(
                    "html5",
                    true
            )
            options.windowTitle = "$pluginName Javadoc"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(getComponents()["java"])
            afterEvaluate {
                artifactId = pluginName.lowercase()
                groupId = "$group"
                version = "$pluginVersion.$buildNumber-${getMcVersion(paperApiName)}"
            }
        }
    }
    repositories {
        maven {
            name = "PublishMaven"
            url = uri(mavenDirectory)
            val mavenUserName = System.getenv("MAVEN_USERNAME")
            val mavenPassword = System.getenv("MAVEN_PASSWORD")
            if(mavenUserName != null && mavenPassword != null) {
                credentials {
                    username = mavenUserName
                    password = mavenPassword
                }
            }
        }
    }
}

private fun getMcVersion(apiNameString: String): String {
    return apiNameString.split('-')[0]
}