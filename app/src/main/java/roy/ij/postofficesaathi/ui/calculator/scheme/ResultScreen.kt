package roy.ij.postofficesaathi.ui.calculator.scheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import roy.ij.postofficesaathi.domain.calculator.CalculatorResult
import roy.ij.postofficesaathi.domain.calculator.formatCalculatorDate
import roy.ij.postofficesaathi.domain.calculator.formatIndianCurrency
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiCard
import roy.ij.postofficesaathi.ui.components.SaathiChip
import roy.ij.postofficesaathi.ui.components.SaathiIconButton
import roy.ij.postofficesaathi.ui.components.SaathiScreen
import roy.ij.postofficesaathi.ui.components.SaathiSecondaryButton

@Composable
fun CalculatorResultScreen(
    result: CalculatorResult?,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    SaathiScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PagePadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SaathiIconButton(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    onClick = onBack
                )
                Text("Calculation Result", style = MaterialTheme.typography.headlineLarge)
            }
            if (result == null) {
                SaathiCard {
                    Text("No calculation available", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Go back and calculate a scheme first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                ResultContent(result = result, onShare = onShare)
            }
        }
    }
}

@Composable
private fun ResultContent(result: CalculatorResult, onShare: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            result.title,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            SaathiChip(result.rateLabel, accent = MaterialTheme.colorScheme.primary)
            SaathiChip(result.schemeType.shortName, accent = MaterialTheme.colorScheme.secondary)
        }
        SummaryCard(result)
        SaathiSecondaryButton(text = "Share this calculation", onClick = onShare, modifier = Modifier.fillMaxWidth())
        if (result.notes.isNotEmpty()) {
            SaathiCard {
                result.notes.forEach {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (result.continuationProjections.isNotEmpty()) {
            SaathiCard {
                Text("If you continue your account", style = MaterialTheme.typography.titleLarge)
                result.continuationProjections.values.forEach { projection ->
                    ResultRow(
                        label = "${projection.year} years",
                        value = "${formatIndianCurrency(projection.withDeposits)} with deposits"
                    )
                }
            }
        }
        if (result.fyWiseBreakdown.isNotEmpty()) {
            SaathiCard {
                Text("FY-wise interest breakdown", style = MaterialTheme.typography.titleLarge)
                result.fyWiseBreakdown.take(8).forEach { row ->
                    ResultRow(
                        label = row.financialYear,
                        value = "${formatIndianCurrency(row.interestAccrued)} interest"
                    )
                }
                Text(
                    "Interest is taxable as per applicable rules. Consult a tax professional for exact liability.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SummaryCard(result: CalculatorResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ResultRow("Total Deposited", formatIndianCurrency(result.totalDeposited))
            ResultRow("Interest Earned", formatIndianCurrency(result.interestEarned))
            result.monthlyIncome?.let { ResultRow("Payout", formatIndianCurrency(it)) }
            ResultRow("Total Payable", formatIndianCurrency(result.maturityAmount), highlight = true)
            ResultRow("Maturity Date", formatCalculatorDate(result.maturityDate))
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = if (highlight) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

