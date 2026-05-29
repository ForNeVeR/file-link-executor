/*
 * SPDX-FileCopyrightText: 2019-2026 file-link-executor contributors <https://github.com/ForNeVeR/file-link-executor>
 *
 * SPDX-License-Identifier: MIT
 */

plugins {
    alias(libs.plugins.jvm.wrapper)
    alias(libs.plugins.changelog)
    alias(libs.plugins.intellij.platform)
    alias(libs.plugins.kotlin.jvm)
}

group = "me.fornever"
version = "1.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(libs.versions.intellij)
    }
}

tasks {
    wrapper {
        gradleVersion = "9.5.1"
        distributionType = Wrapper.DistributionType.ALL
    }

    withType<JavaCompile> {
        java.targetCompatibility = JavaVersion.VERSION_17
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        sinceBuild.set("233.0")
        untilBuild.set(provider { null })

        changeNotes.set(provider {
            changelog.renderItem(
                changelog
                    .getLatest()
                    .withHeader(false)
                    .withEmptySections(false),
                org.jetbrains.changelog.Changelog.OutputType.HTML
            )
        })
    }
}
