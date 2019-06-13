import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":loader:common"))

    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":common"))
    implementation(project(":loader:lua"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
