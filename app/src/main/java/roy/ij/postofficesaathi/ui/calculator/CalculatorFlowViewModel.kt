package roy.ij.postofficesaathi.ui.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import roy.ij.postofficesaathi.domain.calculator.CalculatorResult
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.formatCalculatorDate
import roy.ij.postofficesaathi.domain.calculator.formatIndianCurrency

class CalculatorFlowViewModel : ViewModel() {
    private val _latestResult = MutableStateFlow<CalculatorResult?>(null)
    val latestResult: StateFlow<CalculatorResult?> = _latestResult.asStateFlow()

    fun setResult(result: CalculatorResult) {
        _latestResult.value = result
    }

    fun shareText(): String? = latestResult.value?.toShareText()
}

internal fun CalculatorResult.toShareText(): String =
    buildString {
        appendLine("Post Office Saathi calculation")
        appendLine("$title (${schemeType.shortName})")
        shareRows().forEach { (label, value) ->
            appendLine("$label: $value")
        }
        appendLine()
        appendLine("Calculated with Post Office Saathi")
        append(AppLink)
    }

private fun CalculatorResult.shareRows(): List<Pair<String, String>> {
    val input = inputSummary
    val commonRate = "Rate" to "${ratePercent.formatRate()}%"
    return when (schemeType) {
        SchemeType.RD -> listOf(
            "Monthly amount" to formatIndianCurrency(input.amount),
            "Installments" to input.installmentsPaid.toString(),
            "Opening date" to formatCalculatorDate(input.startDate),
            commonRate,
            "Deposited" to formatIndianCurrency(totalDeposited),
            "Interest earned" to formatIndianCurrency(interestEarned),
            "Total payable" to formatIndianCurrency(maturityAmount),
            "Maturity date" to formatCalculatorDate(maturityDate)
        )
        SchemeType.MIS -> listOf(
            "Investment" to formatIndianCurrency(input.amount),
            commonRate,
            "Term" to "5 years",
            "Monthly payout" to formatIndianCurrency(monthlyIncome ?: 0.0),
            "Total interest" to formatIndianCurrency(interestEarned),
            "Maturity date" to formatCalculatorDate(maturityDate)
        )
        SchemeType.SCSS -> listOf(
            "Investment" to formatIndianCurrency(input.amount),
            commonRate,
            "Term" to if (input.scssExtended) "8 years" else "5 years",
            "Quarterly payout" to formatIndianCurrency(monthlyIncome ?: 0.0),
            "Total interest" to formatIndianCurrency(interestEarned),
            "Maturity date" to formatCalculatorDate(maturityDate)
        )
        SchemeType.TD -> investmentRows("Investment", input.tdTenure.label)
        SchemeType.NSC -> investmentRows("Investment", "5 years")
        SchemeType.KVP -> investmentRows("Investment", "estimated doubling term")
        SchemeType.MSSC -> investmentRows("Investment", "2 years")
        SchemeType.PPF -> yearlyDepositRows("Yearly deposit", "15 years")
        SchemeType.SSY -> yearlyDepositRows("Yearly deposit", "21 years")
        SchemeType.SB -> listOf(
            "Balance" to formatIndianCurrency(input.amount),
            "From" to formatCalculatorDate(input.startDate),
            "To" to formatCalculatorDate(input.toDate ?: maturityDate),
            commonRate,
            "Interest earned" to formatIndianCurrency(interestEarned),
            "Total payable" to formatIndianCurrency(maturityAmount)
        )
        SchemeType.SIMPLE_INTEREST,
        SchemeType.PMI -> customRows("Simple interest")
        SchemeType.COMPOUND_INTEREST -> customRows("Compound interest")
        SchemeType.RD_REBATE -> listOf(
            "Installment amount" to formatIndianCurrency(input.amount),
            "Installments" to input.installmentsPaid.toString(),
            "Rebate estimate" to formatIndianCurrency(interestEarned),
            "Payable after rebate" to formatIndianCurrency(maturityAmount)
        )
    }
}

private fun CalculatorResult.investmentRows(amountLabel: String, tenure: String): List<Pair<String, String>> =
    listOf(
        amountLabel to formatIndianCurrency(inputSummary.amount),
        "Tenure" to tenure,
        "Opening date" to formatCalculatorDate(inputSummary.startDate),
        "Rate" to "${ratePercent.formatRate()}%",
        "Interest earned" to formatIndianCurrency(interestEarned),
        "Total payable" to formatIndianCurrency(maturityAmount),
        "Maturity date" to formatCalculatorDate(maturityDate)
    )

private fun CalculatorResult.yearlyDepositRows(amountLabel: String, term: String): List<Pair<String, String>> =
    listOf(
        amountLabel to formatIndianCurrency(inputSummary.amount),
        "Term" to term,
        "Opening date" to formatCalculatorDate(inputSummary.startDate),
        "Rate" to "${ratePercent.formatRate()}%",
        "Deposited" to formatIndianCurrency(totalDeposited),
        "Interest earned" to formatIndianCurrency(interestEarned),
        "Total payable" to formatIndianCurrency(maturityAmount),
        "Maturity date" to formatCalculatorDate(maturityDate)
    )

private fun CalculatorResult.customRows(mode: String): List<Pair<String, String>> =
    listOf(
        "Principal" to formatIndianCurrency(inputSummary.amount),
        "Mode" to mode,
        "Time period" to "${inputSummary.customYears.formatRate()} years",
        "Rate" to "${ratePercent.formatRate()}%",
        "Interest earned" to formatIndianCurrency(interestEarned),
        "Total payable" to formatIndianCurrency(maturityAmount)
    )

private fun Double.formatRate(): String =
    java.math.BigDecimal(this).setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()

private const val AppLink = "https://ij-roy.github.io/postofficesaathi/"
