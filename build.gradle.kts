plugins {
    id("java")
}

group = "me.kieran"
version = "1.1.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.helpch.at/releases/")
    }
}

val mockitoAgent by configurations.creating {
    isTransitive = false
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.1")

    implementation("org.junit.jupiter:junit-jupiter:6.1.0-M1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.101.0")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("me.clip:placeholderapi:2.12.1")
    testImplementation("org.bstats:bstats-bukkit:3.2.1")
    testImplementation("org.mockito:mockito-core:5.21.0")

    mockitoAgent("org.mockito:mockito-core:5.21.0")

    testImplementation("org.slf4j:slf4j-api:2.0.17")
    testImplementation("org.slf4j:slf4j-jdk14:2.0.17")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("-javaagent:${mockitoAgent.singleFile.absolutePath}")
    })
}