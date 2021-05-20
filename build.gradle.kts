plugins {
    kotlin("jvm") version "1.4.32"
    application
    `maven-publish`
}

group = "io.heartpattern"
version = "2.0.6-SNAPSHOT"

repositories {
    maven("https://repo.heartpattern.io/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.ajalt.clikt", "clikt", "3.0.1")
    implementation("org.ow2.asm", "asm", "9.1")
    implementation("org.ow2.asm", "asm-commons", "9.1")
    implementation("org.ow2.asm", "asm-tree", "9.1")
    implementation("me.tongfei", "progressbar", "0.8.1")
    implementation("io.heartpattern","mcversions","1.0.3-SNAPSHOT")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

application {
    mainClassName = "io.heartpattern.mcremapper.commandline.MCRemapperAppKt"
}

tasks {
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-progressive")
            jvmTarget = "1.8"
        }
    }

    create<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }
}


if("maven.username" in properties && "maven.password" in properties){
    publishing{
        repositories{
            maven(
                if(version.toString().endsWith("SNAPSHOT"))
                    "https://repo.heartpattern.io/repository/maven-public-snapshots/"
                else
                    "https://repo.heartpattern.io/repository/maven-public-releases/"
            ){
                credentials{
                    username = properties["maven.username"].toString()
                    password = properties["maven.password"].toString()
                }
            }
        }

        publications{
            create<MavenPublication>("maven"){
                artifactId = "mcremapper"
                from(components["java"])
                artifact(tasks["sourcesJar"])
            }
        }
    }
}
