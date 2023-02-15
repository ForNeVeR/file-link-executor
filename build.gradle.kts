plugins {
    id("me.filippov.gradle.jvm.wrapper") version "0.14.0"
    id("org.jetbrains.changelog") version "2.0.0"
    id("org.jetbrains.intellij") version "1.13.0"
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
}

group = "me.fornever"
version = "1.0.2"

repositories {
    mavenCentral()
}

intellij {
    version.set("2022.2")
}

tasks {
    wrapper {
        gradleVersion = "7.5.1"
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
        sinceBuild.set("222.0")
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
