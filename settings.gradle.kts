rootProject.name = "Quantum"

listOf("API", "Spawn", "Wild").forEach {
    include(":Quantum$it")
    project(":Quantum$it").projectDir = file("Quantum$it")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")