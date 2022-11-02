plugins {
    id("org.jetbrains.intellij") version "0.4.21"
    id("org.jetbrains.kotlin.jvm") version "1.3.41"
}

group = "me.fornever"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks {
    wrapper {
        gradleVersion = "7.5.1"
        distributionType = Wrapper.DistributionType.ALL
    }

    intellij {
        version = "2020.1"
    }
    patchPluginXml {
        sinceBuild("201.0")
        untilBuild(null)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    patchPluginXml {
        changeNotes("""
      Fixed a bug when links stopped working after executing them in another project (not the same where the plugin tool
      window was created initially).
      """)
    }
}
