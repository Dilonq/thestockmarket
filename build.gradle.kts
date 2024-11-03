plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "djh.stockmarket.Main"
        )
    }
}

tasks.shadowJar {
    manifest {
        attributes(
            "Main-Class" to "djh.stockmarket.Main"
        )
    }
}

group = "djh.stockmarket"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.h2database:h2:2.2.220")
    implementation("at.favre.lib:bcrypt:0.10.2")
}



tasks.test {
    useJUnitPlatform()
}