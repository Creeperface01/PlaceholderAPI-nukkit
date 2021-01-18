import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.4.21"
val lombokVersion = "1.18.16"
val junitVersion = "4.12"

plugins {
    kotlin("jvm") version "1.4.21"
    id("org.jetbrains.dokka") version "1.4.20"
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

dependencies {
    compileOnly(kotlin("stdlib-jdk8", kotlinVersion))
    compileOnly(kotlin("reflect", kotlinVersion))
    compileOnly("cn.nukkit:nukkit:1.0-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:$lombokVersion")

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.4.20")

    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testImplementation("junit:junit:$junitVersion")
}