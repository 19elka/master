plugins {
    id("org.springframework.boot")
    id("com.google.protobuf")
}

dependencies {
    implementation(project(":common"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    implementation("net.devh:grpc-server-spring-boot-starter:2.15.0.RELEASE")
}