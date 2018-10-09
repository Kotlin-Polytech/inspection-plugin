import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.idea.inspections.*

buildscript {
    extra["kotlinVersion"] = "1.2.0"
    val kotlinVersion: String by extra

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.github.jengelman.gradle.plugins:shadow:2.0.1")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    }
}

val kotlinVersion: String by extra

plugins {
    java
}

apply {
    plugin("kotlin")
    plugin("maven-publish")
    plugin("com.github.johnrengelman.shadow")
    plugin("com.jfrog.bintray")
}

val projectName = "inspection-runner"

configure<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "org.jetbrains.idea.inspections.ProxyRunnerImpl"
    }
}

configure<ShadowJar>("shadowJar") {
    baseName = projectName
    classifier = ""
}

configure<PublishingExtension> {
    repositories {
        maven {
            url = uri("build/repository")
        }
    }
    publications {
        create<MavenPublication>("Runner") {
            configure<ShadowExtension> {
                component(this@create)
            }
            version = projectVersion
            groupId = projectGroup
            artifactId = projectName
        }
    }
}

configure<BintrayExtension> {
    user = System.getProperty("bintray.user")
    key = System.getProperty("bintray.pass")

    pkg = PackageConfig().apply {
        userOrg = "vorpal-research"
        repo = "kotlin-maven"
        name = "inspections"
        desc = "IDEA inspection offline running tool"
        vcsUrl = "https://github.com/Kotlin-Polytech/inspection-plugin.git"
        setLicenses("Apache-2.0")
        version = VersionConfig().apply {
            name = projectVersion
        }
    }

    setPublications("Runner")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { setUrl("https://www.jetbrains.com/intellij-repository/releases") }
    maven { setUrl("https://www.jetbrains.com/intellij-repository/snapshots") }
}

val ideaVersion = "ideaIC:2018.2"
val ideaDirectory = File(buildDir, ideaVersion.replace(":.", '_'))

configurations {
    create("idea")
    create("kotlin-plugin")

    dependencies {
        add("idea", create("com.jetbrains.intellij.idea:$ideaVersion@zip"))
    }
}

val unzipIdea = task<Sync>("unzip-idea") {
    with(configurations.getByName("idea")) {
        dependsOn(this)
        from(zipTree(singleFile))
        into(ideaDirectory)
    }
}

tasks {
    withType<KotlinCompile> {
        dependsOn(unzipIdea)
        kotlinOptions {
            jvmTarget = "1.8"
            languageVersion = "1.0"
            apiVersion = "1.0"
        }
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compile("org.jdom:jdom2:2.0.6")
    compileOnly(fileTree(mapOf("dir" to "$ideaDirectory/lib", "include" to "*.jar")))
    compile(project(":interface"))
}
