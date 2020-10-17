buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.squareup.sqldelight:gradle-plugin:1.4.3")
    }
}

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.4.10"
    id("com.squareup.sqldelight") version "1.4.3"
}

group = "me.geek.tom"
version = "0.0.1"

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://jitpack.io/") }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")

    // Scripting
//    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlin_version")
//    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlin_version")
//    implementation("org.jetbrains.kotlin:kotlin-script-runtime:$kotlin_version")
//    implementation("org.jetbrains.kotlin:kotlin-script-util:$kotlin_version")
    implementation("org.mozilla:rhino:1.7.13")

    // Webserver stuff
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("io.ktor:ktor-gson:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")

    // Database
    implementation("com.squareup.sqldelight:coroutines-extensions-jvm:1.4.3")
    implementation("com.squareup.sqldelight:jdbc-driver:1.4.3")
    implementation("com.zaxxer:HikariCP:3.4.5")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.6.+")

    // Config
    implementation("com.uchuhimo:konf-toml:0.23.0")
}

sqldelight {
    database("Storage") {
        packageName = "me.geek.tom.mcchatserver.storage"
        dialect = "mysql"
        sourceFolders = listOf("sqldelight")
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
