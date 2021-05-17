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
  implementation("org.slf4j:slf4j-api:1.7.30")

  implementation(platform("io.projectreactor:reactor-bom:2020.0.7"))
  implementation("io.projectreactor.netty:reactor-netty-core")
  implementation("io.projectreactor.netty:reactor-netty-http")
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
  val classes = listOf(
    "io.netty.buffer.AbstractByteBufAllocator",
    "io.netty.channel.AbstractChannel",
    "io.netty.channel.AbstractChannelHandlerContext",
    "io.netty.channel.ChannelInitializer",
    "io.netty.channel.DefaultFileRegion",
    "io.netty.channel.epoll.Epoll",
    "io.netty.channel.epoll.EpollEventArray",
    "io.netty.channel.epoll.EpollEventLoop",
    "io.netty.channel.epoll.Native",
    "io.netty.util.internal.PlatformDependent",
    "io.netty.util.ResourceLeakDetector",
    "io.netty.buffer.AbstractByteBuf",
    "io.netty",
    "io.netty.util.AbstractReferenceCounted",
    "org.slf4j.LoggerFactory"
  ).joinToString(",")
  arguments(
    "--initialize-at-run-time=$classes",
    "--trace-class-initialization=$classes",
    "--no-fallback",
    "-H:+ReportExceptionStackTraces"
  )
}

generateNativeImageConfig {
  enabled = true
  byRunningApplicationWithoutArguments()
}

application {
  mainClass.set("com.miya10kei.haribote.MainKt")
}
