package roy.ij.postofficesaathi.data.calculator

import java.time.LocalDate
import org.json.JSONObject
import roy.ij.postofficesaathi.domain.calculator.CompoundingFrequency
import roy.ij.postofficesaathi.domain.calculator.RateHistory
import roy.ij.postofficesaathi.domain.calculator.SchemeRate
import roy.ij.postofficesaathi.domain.calculator.SchemeType

object RateHistoryParser {
    fun parse(json: String): RateHistory {
        val root = JSONObject(json)
        val version = root.getString("version")
        val rates = buildList {
            val schemes = root.getJSONArray("schemes")
            for (schemeIndex in 0 until schemes.length()) {
                val scheme = schemes.getJSONObject(schemeIndex)
                val schemeType = SchemeType.valueOf(scheme.getString("schemeType"))
                val schemeRates = scheme.getJSONArray("rates")
                for (rateIndex in 0 until schemeRates.length()) {
                    val item = schemeRates.getJSONObject(rateIndex)
                    add(
                        SchemeRate(
                            schemeType = schemeType,
                            effectiveFrom = LocalDate.parse(item.getString("effectiveFrom")),
                            effectiveTo = item.optString("effectiveTo").takeUnless { it.isBlank() || it == "null" }
                                ?.let(LocalDate::parse),
                            rate = if (item.has("rate")) item.getDouble("rate") else 0.0,
                            tdTenureRates = item.optJSONObject("tenureRates")?.toDoubleMap().orEmpty(),
                            compoundingFrequency = CompoundingFrequency.valueOf(item.getString("compoundingFrequency"))
                        )
                    )
                }
            }
        }
        return RateHistory(version = version, rates = rates)
    }

    private fun JSONObject.toDoubleMap(): Map<String, Double> =
        keys().asSequence().associateWith { getDouble(it) }
}

