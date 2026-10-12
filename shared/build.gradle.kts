import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.androidx.room3)
    alias(libs.plugins.wire)
    alias(libs.plugins.aboutLibraries)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    android {
        namespace = "com.chisacwnu.hicwnu.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonMain {
            dependencies {

                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.material3.adaptive)
                implementation(libs.compose.material3.adaptive.navigation.suite)
                implementation(libs.compose.ui)
                implementation(libs.compose.animation)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.ui.tooling.preview)

                implementation(libs.material.kolor)

                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.androidx.navigation3.ui)
                implementation(libs.androidx.lifecycle.viewmodel.navigation3)

                implementation(libs.coil.compose)
                implementation(libs.aboutlibraries.compose.m3)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.koin.compose.navigation3)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.cbor)
                implementation(libs.kotlinx.datetime)
                implementation(libs.okio)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.auth)
                implementation(libs.koin.annotations)

                implementation(libs.androidx.room3.runtime)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidx.datastore.core)

                implementation(libs.wire.runtime)
            }
        }

        androidMain.dependencies {
            implementation(libs.androidx.sqlite.framework)
            implementation(libs.ktor.client.cio)
        }

        jvmMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.ktor.client.cio)
        }

        iosMain.dependencies {
            implementation(libs.androidx.sqlite.framework)
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspJvm", libs.androidx.room3.compiler)
    add("kspIosArm64", libs.androidx.room3.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room3.compiler)
}

aboutLibraries {
    export {
        outputPath = file("src/commonMain/composeResources/files/aboutlibraries.json")
        prettyPrint = true
    }
    library {
        duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

wire {
    sourcePath {
        srcDir("src/commonMain/proto")
    }

    kotlin {
        escapeKotlinKeywords = true
        enumMode = "enum_class"
        rpcRole = "none"
    }
}

val packSchoolsZip = tasks.register<Zip>("packSchoolsZip") {
    group = "build"
    description = "将离线适配资源打包为 composeResources ZIP 资源文件。"

    from(layout.projectDirectory.dir("assets/offline_repo"))
    destinationDirectory.set(layout.projectDirectory.dir("src/commonMain/composeResources/files"))
    archiveFileName.set("offline_schools.zip")
}

val exportLibraryDefinitions = tasks.named("exportLibraryDefinitions")

tasks.matching {
    it.name.startsWith("generateComposeResClass") ||
            it.name.startsWith("copyNonXmlValueResources") ||
            it.name.startsWith("prepareComposeResources")
}.configureEach {
    dependsOn(packSchoolsZip)
    dependsOn(exportLibraryDefinitions)
}