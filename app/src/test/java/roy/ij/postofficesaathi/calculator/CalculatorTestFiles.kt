package roy.ij.postofficesaathi.calculator

import java.io.File

fun publicRatesJson(): String {
    val candidates = listOf(
        File("public/rates.json"),
        File("../public/rates.json"),
        File("../../public/rates.json")
    )
    val file = candidates.firstOrNull { it.exists() }
        ?: error("Could not find public/rates.json from ${File(".").absolutePath}")
    return file.readText()
}

