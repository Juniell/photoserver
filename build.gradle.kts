import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
}

group = "juniell"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.alialbaali.kamel:kamel-image:0.3.0")
    {
        implementation("io.ktor:ktor-client-core:2.0.1")
        exclude(group = "io.ktor", module = "ktor-client-cio")
        exclude(group = "io.ktor", module = "ktor-client-logging")
    }
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.1.1")
    implementation("org.jetbrains.compose.components:components-splitpane-desktop:1.1.1")
    implementation("org.burnoutcrew.composereorderable:reorderable:0.7.4")
    implementation("com.godaddy.android.colorpicker:compose-color-picker:0.4.2")
    implementation("com.google.zxing:core:3.5.0")
    implementation("com.google.zxing:javase:3.5.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("javax.mail:mail:1.4.7")
    implementation("org.apache.ftpserver:ftpserver-core:1.2.0")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.0.1")
    implementation("io.ktor:ktor-server-core-jvm:2.0.1")
    implementation("io.ktor:ktor-serialization-gson-jvm:2.0.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.0.1")
    implementation("io.ktor:ktor-server-netty-jvm:2.0.1")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("io.ktor:ktor-server-partial-content:2.0.1")
    implementation("io.ktor:ktor-server-auto-head-response:2.0.1")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.jetbrains.exposed:exposed-dao:0.38.2")
    implementation("org.jetbrains.exposed:exposed-core:0.38.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.38.2")
    testImplementation("io.ktor:ktor-server-tests-jvm:2.0.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "photoserver"
            packageVersion = "1.0.0"
        }
    }
}