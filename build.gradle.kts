/*
 * SPDX-FileCopyrightText: 2019-2026 file-link-executor contributors <https://github.com/ForNeVeR/file-link-executor>
 *
 * SPDX-License-Identifier: MIT
 */

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.exceptions.MissingVersionException
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask

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

intellijPlatform {
    pluginConfiguration {
        val latestChangelog = try {
            changelog.getUnreleased()
        } catch (_: MissingVersionException) {
            changelog.getLatest()
        }
        changeNotes = provider {
            changelog.renderItem(
                latestChangelog
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML
            )
        }
    }
    pluginVerification {
        ides {
            select {
                channels = listOf(
                    ProductRelease.Channel.RELEASE,
                    ProductRelease.Channel.EAP
                )
                untilBuild = providers.gradleProperty("untilBuildForVerification")
            }
        }
        failureLevel.addAll(
            VerifyPluginTask.FailureLevel.COMPATIBILITY_WARNINGS,
            VerifyPluginTask.FailureLevel.DEPRECATED_API_USAGES,
            VerifyPluginTask.FailureLevel.INTERNAL_API_USAGES,
            VerifyPluginTask.FailureLevel.OVERRIDE_ONLY_API_USAGES
        )
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

    check {
        dependsOn(verifyPlugin)
    }
}
