import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Kotlin.version
    id("org.jetbrains.dokka") version Dokka.version
    id("maven-publish")
}

group = "com.creeperface.nukkit.placeholderapi"
version = "1.4-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.opencollab.dev/maven-releases/")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.apply {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf(
        "-Xjvm-default=enable",
        "-Xopt-in=kotlin.RequiresOptIn",
        "-Xenable-builder-inference",
        "-Xjvm-default=all-compatibility"
    )
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

java {
    sourceCompatibility = VERSION_1_8
    targetCompatibility = VERSION_1_8
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("snapshot") {
                from(components["kotlin"])

                pom {
                    name.set("PlaceholderAPI")
                    description.set("Placeholder API for nukkit")
                    licenses {
                        license {
                            name.set("GNU GENERAL PUBLIC LICENSE v3.0")
                            url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                        }
                    }
                }
            }
        }
        repositories {
            mavenLocal()
        }
    }
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8", Kotlin.version))
    compileOnly(kotlin("reflect", Kotlin.version))
    compileOnly("cn.nukkit:nukkit:${Nukkit.version}")

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:${Dokka.version}")

    testImplementation("junit:junit:${JUnit.version}")
    testCompileOnly("cn.nukkit:nukkit:${Nukkit.version}")
}