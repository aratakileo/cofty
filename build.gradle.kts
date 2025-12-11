plugins {
    id("java")
}

group = "cofty"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("org.jetbrains:annotations:26.0.2-1")
}

tasks.test {
    useJUnitPlatform()
}