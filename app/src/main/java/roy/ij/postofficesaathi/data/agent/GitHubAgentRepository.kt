package roy.ij.postofficesaathi.data.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class GitHubAgentRepository(
    private val agentsUrl: String = "https://raw.githubusercontent.com/ij-roy/Post-Office-Saathi/main/public/agents.json"
) : AgentRepository {
    override suspend fun searchByPincode(pincode: String): AgentSearchResult = withContext(Dispatchers.IO) {
        runCatching {
            val normalized = pincode.filter(Char::isDigit)
            val agents = AgentParser.parse(URL(agentsUrl).readText())
                .filter { it.pincode == normalized }
            AgentSearchResult(agents = agents)
        }.getOrElse {
            AgentSearchResult(
                agents = emptyList(),
                errorMessage = "Could not load agents. Please check your connection."
            )
        }
    }
}

