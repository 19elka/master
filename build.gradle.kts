import org.gradle.api.tasks.testing.Test
import org.gradle.api.JavaVersion

plugins {
    id("org.springframework.boot") version "3.3.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("com.google.protobuf") version "0.9.4" apply false
}

allprojects {
    group = "com.example"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependencies {
        add("compileOnly", "org.projectlombok:lombok:1.18.26")
        add("annotationProcessor", "org.projectlombok:lombok:1.18.26")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}