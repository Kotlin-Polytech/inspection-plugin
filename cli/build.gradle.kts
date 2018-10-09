import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.idea.inspections.*

buildscript {
    extra["kotlinVersion"] = "1.2.0"
    extra["kotlinArgParserVersion"] = "2.0.7"
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
val kotlinArgParserVersion: String by extra

plugins {
    java
}

apply {
    plugin("kotlin")
    plugin("maven-publish")
    plugin("com.github.johnrengelman.shadow")
    plugin("com.jfrog.bintray")
}

val projectName = "inspection-cli"

configure<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "org.jetbrains.intellij.InspectionTool"
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
        create<MavenPublication>("Cli") {
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

    setPublications("Cli")
}

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compile("com.xenomachina:kotlin-argparser:$kotlinArgParserVersion")
    implementation("com.googlecode.json-simple:json-simple:1.1")
    compile("org.jdom:jdom2:2.0.6")
    compile(project(":interface"))
    compile(project(":frontend"))
}
