plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.catconnect"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    filteringCharset = "UTF-8"
}

repositories {
    mavenCentral()
}

val javafxVersion = "21.0.2"

dependencies {
    implementation("org.openjfx:javafx-web:$javafxVersion")
    implementation("org.openjfx:javafx-controls:$javafxVersion")
    implementation("org.openjfx:javafx-fxml:$javafxVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.6")
}

javafx {
    version = javafxVersion
    modules("javafx.controls", "javafx.fxml", "javafx.web")
}

application {
    mainClass.set("com.catconnect.app.CatConnectApp")
}

tasks.named<JavaExec>("run") {
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED", "-Dfile.encoding=UTF-8")
}
