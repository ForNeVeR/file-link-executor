plugins {
    id("me.filippov.gradle.jvm.wrapper") version "0.14.0"
    id("org.jetbrains.intellij") version "1.9.0"
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
}

group = "me.fornever"
version = "0.1.0"

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

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        sinceBuild.set("222.0")
        untilBuild.set(null as String?)

        changeNotes.set("""
      Fixed a bug when links stopped working after executing them in another project (not the same where the plugin tool
      window was created initially).
      """)
    }
}
