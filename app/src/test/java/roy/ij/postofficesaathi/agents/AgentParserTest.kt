package roy.ij.postofficesaathi.agents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import roy.ij.postofficesaathi.data.agent.AgentParser

class AgentParserTest {
    @Test
    fun parsesDummyAgentEntry() {
        val agents = AgentParser.parse(
            """
            [
              {
                "id": "agent_dummy",
                "name": "Demo Agent",
                "area": "Sample Area",
                "district": "Sample District",
                "state": "West Bengal",
                "pincode": "700001",
                "phone": "9000000000",
                "photoUrl": ""
              }
            ]
            """.trimIndent()
        )

        assertEquals(1, agents.size)
        assertEquals("agent_dummy", agents.first().id)
        assertEquals("700001", agents.first().pincode)
    }

    @Test
    fun toleratesMissingAgentFields() {
        val agents = AgentParser.parse("""[{"id":"agent_partial"}]""")

        assertEquals(1, agents.size)
        assertEquals("agent_partial", agents.first().id)
        assertEquals("Agent", agents.first().name)
        assertTrue(agents.first().phone.isBlank())
    }
}

