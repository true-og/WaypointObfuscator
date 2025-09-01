plugins {
    id("java")
	eclipse
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "ink.auth"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven(url = "https://repo.codemc.io/repository/maven-releases/")
    maven(url = "https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.9.3")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.19.4")
    }
}

val targetJavaVersion = 17
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

