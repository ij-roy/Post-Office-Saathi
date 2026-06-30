package roy.ij.postofficesaathi.ui.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import roy.ij.postofficesaathi.domain.calculator.CalculatorResult
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

private fun CalculatorResult.toShareText(): String =
    buildString {
        appendLine("Post Office Savings Calculation")
        appendLine("Scheme: $title")
        appendLine("Rate: $rateLabel")
        appendLine("Total deposited: ${formatIndianCurrency(totalDeposited)}")
        appendLine("Interest earned: ${formatIndianCurrency(interestEarned)}")
        appendLine("Total payable: ${formatIndianCurrency(maturityAmount)}")
        appendLine("Maturity date: ${formatCalculatorDate(maturityDate)}")
        appendLine()
        append("Created with Post Office Saathi")
    }

