plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.serialization") version "2.2.10"
    id("io.ktor.plugin") version "3.2.0"
}

application {
    mainClass.set("com.example.mockapi.ApplicationKt")
}

ktor {
    docker {
        localImageName.set("mock-api")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core:3.2.0")
    implementation("io.ktor:ktor-server-netty:3.2.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.2.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.0")
    implementation("io.ktor:ktor-server-cors:3.2.0")
    implementation("io.ktor:ktor-server-status-pages:3.2.0")
    implementation("ch.qos.logback:logback-classic:1.5.16")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xserialization-json=pretty")
    }
}
