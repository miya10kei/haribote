import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.5.0"
  kotlin("plugin.serialization") version "1.5.0"
  id("application")
  id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
  id("org.mikeneck.graalvm-native-image") version "1.4.0"
}

group = "com.miya10kei.haribote"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib")
  implementation("com.github.ajalt.clikt:clikt:3.2.0")
  implementation("com.charleskorn.kaml:kaml:0.33.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.2.1")

  implementation(platform("io.netty:netty-bom:4.1.64.Final"))
  implementation("io.netty:netty-transport")
  implementation("io.netty:netty-handler")
  implementation("io.netty:netty-codec-http")
}

ktlint {
  version.set("0.41.0")
  outputColorName.set("RED")
  additionalEditorconfigFile.set(File(".editorconfig"))
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

nativeImage {
  graalVmHome = System.getenv("JAVA_HOME") ?: "hoge"
  buildType {
    it.executable(main = "com.miya10kei.haribote.MainKt")
  }
  mainClass = "com.miya10kei.haribote.MainKt"
  executableName = "haribote"
  outputDirectory = file("$buildDir/executable")
  arguments(
    "--allow-incomplete-classpath",
    "--initialize-at-build-time",
    "--install-exit-handlers",
    "--no-fallback",
    "--no-server",
    "--static",
    "--verbose",
    "-H:+PrintClassInitialization",
    "-H:+ReportExceptionStackTraces",
    "-H:IncludeResources=./(haribote|default-spec).yaml",
    "-H:Log=registerResource:",
    "-J-Xms2g",
    "-J-Xmx2g"
  )
}

generateNativeImageConfig {
  enabled = true
  byRunningApplicationWithoutArguments()
}

application {
  mainClass.set("com.miya10kei.haribote.MainKt")
}
