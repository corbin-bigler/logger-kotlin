plugins {
    kotlin("jvm") version "2.0.21"
    `java-library`
}

group = "com.thysmesi"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    val kotlinxCoroutineVersion = "1.10.2"
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutineVersion")
}

tasks.test {
    useJUnitPlatform()
}