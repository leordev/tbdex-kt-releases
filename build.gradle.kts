import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URL

project.group = "com.github.leordev"
project.version = "0.1.0" // x-release-please-version

plugins {
  id("org.jetbrains.kotlin.jvm") version "1.9.0"
  id("java-library")
  id("io.gitlab.arturbosch.detekt") version "1.23.1"
  `maven-publish`
  id("org.jetbrains.dokka") version "1.9.0"
  id("org.jetbrains.kotlinx.kover") version "0.7.3"
  signing
  id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

repositories {
  mavenCentral()
}

dependencies {
  api(project(":protocol"))
  api(project(":httpclient"))
  api(project(":httpserver"))
  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.1")
}

allprojects {
  group = "com.github.leordev"
}

subprojects {
  repositories {
    mavenCentral()
  }
  apply {
    plugin("io.gitlab.arturbosch.detekt")
    plugin("org.jetbrains.kotlin.jvm")
    plugin("java-library")
    plugin("maven-publish")
    plugin("org.jetbrains.dokka")
    plugin("org.jetbrains.kotlinx.kover")
    plugin("signing")
  }

  tasks.withType<Detekt>().configureEach {
    jvmTarget = "1.8"
  }
  dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.1")
  }

  detekt {
    config.setFrom("$rootDir/config/detekt.yml")
  }

  kotlin {
    jvmToolchain(11)
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
      apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
      languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
    }
  }

  java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  val publicationName = "${rootProject.name}-${project.name}"
  publishing {
    publications {
      create<MavenPublication>(publicationName) {
        groupId = project.group.toString()
        artifactId = name
        description = "Kotlin SDK for tbdex functionality"
        version = project.property("version").toString()
        from(components["java"])
      }

      withType<MavenPublication> {
        pom {
          name = publicationName
          packaging = "jar"
          description.set("tbdex kotlin SDK")
          url.set("https://github.com/leordev/tbdex-kt-releases")
          inceptionYear.set("2023")
          licenses {
            license {
              name.set("The Apache License, Version 2.0")
              url.set("https://github.com/leordev/tbdex-kt-releases/blob/main/LICENSE")
            }
          }
          developers {
            developer {
              id.set("TBD54566975")
              name.set("Block Inc.")
              email.set("tbd-releases@tbd.email")
            }
          }
          scm {
            connection.set("scm:git:git@github.com:leordev/tbdex-kt-releases.git")
            developerConnection.set("scm:git:ssh:git@github.com:leordev/tbdex-kt-releases.git")
            url.set("https://github.com/leordev/tbdex-kt-releases")
          }
        }
      }
    }

    if (!project.hasProperty("skipSigning") || project.property("skipSigning") != "true") {
      signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications[publicationName])
      }
    }
  }

  tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
      documentedVisibilities.set(
        setOf(
          DokkaConfiguration.Visibility.PUBLIC,
          DokkaConfiguration.Visibility.PROTECTED
        )
      )

      sourceLink {
        val exampleDir = "https://github.com/leordev/tbdex-kt-releases/tree/main"

        localDirectory.set(rootProject.projectDir)
        remoteUrl.set(URL(exampleDir))
        remoteLineSuffix.set("#L")
      }
    }
  }

  tasks.test {
    useJUnitPlatform()
    reports {
      junitXml
    }
    testLogging {
      events("passed", "skipped", "failed", "standardOut", "standardError")
      exceptionFormat = TestExceptionFormat.FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true
    }
  }
}

// Configures only the parent MultiModule task,
// this will not affect subprojects
tasks.dokkaHtmlMultiModule {
  moduleName.set("tbdex SDK Documentation")
}

publishing {
  publications {
    create<MavenPublication>(rootProject.name) {
      groupId = project.group.toString()
      artifactId = name
      description = "Kotlin SDK for tbdex functionality"
      version = project.property("version").toString()
      from(components["java"])

      pom {
        packaging = "jar"
        name = project.name
        description.set("tbdex kotlin SDK")
        url.set("https://github.com/leordev/tbdex-kt-releases")
        inceptionYear.set("2023")
        licenses {
          license {
            name.set("The Apache License, Version 2.0")
            url.set("https://github.com/leordev/tbdex-kt-releases/blob/main/LICENSE")
          }
        }
        developers {
          developer {
            id.set("TBD54566975")
            name.set("Block Inc.")
            email.set("tbd-releases@tbd.email")
          }
        }
        scm {
          connection.set("scm:git:git@github.com:leordev/tbdex-kt-releases.git")
          developerConnection.set("scm:git:ssh:git@github.com:leordev/tbdex-kt-releases.git")
          url.set("https://github.com/leordev/tbdex-kt-releases")
        }
      }
    }
  }
}

if (!project.hasProperty("skipSigning") || project.property("skipSigning") != "true") {
  signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["tbdex"])
  }
}

nexusPublishing {
  repositories {
    sonatype {  //only for users registered in Sonatype after 24 Feb 2021
      nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
    }
  }
}