package roy.ij.postofficesaathi.data.agent

interface AgentRepository {
    suspend fun searchByPincode(pincode: String): AgentSearchResult
}

data class AgentSearchResult(
    val agents: List<Agent>,
    val errorMessage: String? = null
)

