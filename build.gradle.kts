import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Kotlin.version
    id("org.jetbrains.dokka") version Dokka.version
}

group = "com.creeperface.nukkit.placeholderapi"
version = "1.4-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.opencollab.dev/maven-releases/")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.apply {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xjvm-default=enable", "-Xopt-in=kotlin.RequiresOptIn")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

java {
    sourceCompatibility = VERSION_1_8
    targetCompatibility = VERSION_1_8
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8", Kotlin.version))
    compileOnly(kotlin("reflect", Kotlin.version))
    compileOnly("cn.nukkit:nukkit:${Nukkit.version}")
    compileOnly("org.projectlombok:lombok:${Lombok.version}")

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:${Dokka.version}")

    annotationProcessor("org.projectlombok:lombok:${Lombok.version}")

    testImplementation("junit:junit:${JUnit.version}")
    testCompileOnly("cn.nukkit:nukkit:${Nukkit.version}")
}