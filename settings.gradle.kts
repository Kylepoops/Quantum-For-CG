rootProject.name = "Quantum"

listOf("API", "Spawn").forEach {
    include(":Quantum$it")
    project(":Quantum$it").projectDir = file("Quantum$it")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")