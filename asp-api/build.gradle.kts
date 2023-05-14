plugins {
    `java-library`
}

dependencies {
    api("com.flowpowered:flow-nbt:2.0.2")
    api("org.jetbrains:annotations:23.0.0")

    compileOnly(project(":candle-api"))
}
