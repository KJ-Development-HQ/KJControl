import java.util.jar.JarFile

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

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.1")
    implementation("org.junit.jupiter:junit-jupiter:6.1.0-M1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.101.0")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("org.slf4j:slf4j-api:2.0.17")
    testImplementation("org.slf4j:slf4j-jdk14:2.0.17")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}