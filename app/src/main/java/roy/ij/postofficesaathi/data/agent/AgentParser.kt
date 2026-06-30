package roy.ij.postofficesaathi.data.agent

import org.json.JSONArray

object AgentParser {
    fun parse(json: String): List<Agent> {
        val array = JSONArray(json)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val id = item.optString("id").ifBlank { "agent_$index" }
                add(
                    Agent(
                        id = id,
                        name = item.optString("name").ifBlank { "Agent" },
                        area = item.optString("area"),
                        district = item.optString("district"),
                        state = item.optString("state"),
                        pincode = item.optString("pincode"),
                        phone = item.optString("phone").filter(Char::isDigit),
                        photoUrl = item.optString("photoUrl")
                    )
                )
            }
        }
    }
}

