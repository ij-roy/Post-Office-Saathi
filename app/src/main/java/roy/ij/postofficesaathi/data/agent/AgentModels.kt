package roy.ij.postofficesaathi.data.agent

data class Agent(
    val id: String,
    val name: String,
    val area: String,
    val district: String,
    val state: String,
    val pincode: String,
    val phone: String,
    val photoUrl: String
) {
    val locationLabel: String
        get() = listOf(area, district, state).filter { it.isNotBlank() }.joinToString(", ")
}

