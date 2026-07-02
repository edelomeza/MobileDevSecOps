plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor.plugin)
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
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization.json)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.serialization.json)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xserialization-json=pretty")
    }
}
