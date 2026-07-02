package roy.ij.postofficesaathi.ui.calculator.scheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import roy.ij.postofficesaathi.R
import roy.ij.postofficesaathi.domain.calculator.CalculatorResult
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.formatCalculatorDate
import roy.ij.postofficesaathi.domain.calculator.formatIndianCurrency
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiCard
import roy.ij.postofficesaathi.ui.components.SaathiIconButton
import roy.ij.postofficesaathi.ui.components.SaathiScreen
import roy.ij.postofficesaathi.ui.calculator.schemeIcon

@Composable
fun CalculatorResultScreen(
    result: CalculatorResult?,
    onBack: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onShareMore: () -> Unit
) {
    SaathiScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PagePadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ResultHeader(onBack = onBack)
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
                ResultContent(
                    result = result,
                    onShareWhatsApp = onShareWhatsApp,
                    onShareMore = onShareMore
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ResultHeader(onBack: () -> Unit) {
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
        Text("Result", style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
private fun ResultContent(
    result: CalculatorResult,
    onShareWhatsApp: () -> Unit,
    onShareMore: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HeroResultCard(result = result)
        CalculationBasedOnCard(result = result)
        ShareActions(
            onShareWhatsApp = onShareWhatsApp,
            onShareMore = onShareMore
        )
        if (result.continuationProjections.isNotEmpty()) {
            ContinuationCard(result)
        }
        if (result.fyWiseBreakdown.isNotEmpty()) {
            YearWiseInterestCard(result)
        }
    }
}

@Composable
private fun HeroResultCard(result: CalculatorResult) {
    val darkScheme = isDarkScheme()
    val shape = RoundedCornerShape(18.dp)
    val hero = result.heroMetric()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = if (darkScheme) 0.86f else 0.99f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (darkScheme) 0.34f else 0.26f)),
        shadowElevation = if (darkScheme) 6.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SchemeContext(result)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    hero.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatResultCurrency(hero.value),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            DepositInterestBar(result)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                result.summaryRows().forEach { row ->
                    CompactResultRow(
                        label = row.label,
                        value = row.value,
                        valueColor = if (row.emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SchemeContext(result: CalculatorResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(11.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f),
            contentColor = MaterialTheme.colorScheme.primary,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = result.schemeType.schemeIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(23.dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                result.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DepositInterestBar(result: CalculatorResult) {
    val total = (result.totalDeposited + result.interestEarned).takeIf { it > 0.0 } ?: 1.0
    val depositedWeight = (result.totalDeposited / total).toFloat().coerceIn(0.05f, 0.95f)
    val interestWeight = (result.interestEarned / total).toFloat().coerceIn(0.05f, 0.95f)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .weight(depositedWeight)
                    .height(10.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
            )
            Box(
                modifier = Modifier
                    .weight(interestWeight)
                    .height(10.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendText("Deposited")
            LegendText("Interest")
        }
    }
}

@Composable
private fun LegendText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CompactResultRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CalculationBasedOnCard(result: CalculatorResult) {
    val context = result.contextDisplay()
    DetailCard {
        Text(
            "Calculation based on",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        PrimaryContextBlock(context.primary)
        if (context.supporting.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                context.supporting.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { row ->
                            SupportingContextTile(
                                row = row,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrimaryContextBlock(row: ResultDisplayRow) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDarkScheme()) 0.28f else 0.62f),
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                row.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                row.value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SupportingContextTile(row: ResultDisplayRow, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            row.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            row.value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ShareActions(onShareWhatsApp: () -> Unit, onShareMore: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WhatsAppShareButton(
            onClick = onShareWhatsApp,
            modifier = Modifier.weight(1f)
        )
        MoreAppsShareButton(
            onClick = onShareMore,
            modifier = Modifier
        )
    }
}

@Composable
private fun WhatsAppShareButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(16.dp),
        color = WhatsAppGreen,
        contentColor = Color.White,
        shadowElevation = if (isDarkScheme()) 5.dp else 2.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_whatsapp),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = Color.Unspecified
                )
            }
            Text(
                "Share on WhatsApp",
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MoreAppsShareButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(52.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDarkScheme()) 0.80f else 0.98f),
        contentColor = MaterialTheme.colorScheme.primary,
        shadowElevation = if (isDarkScheme()) 4.dp else 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.34f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share with other apps",
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun ContinuationCard(result: CalculatorResult) {
    DetailCard {
        Text("If deposits continue", style = MaterialTheme.typography.titleLarge)
        result.continuationProjections.values.forEach { projection ->
            CompactResultRow(
                label = "${projection.year} years",
                value = formatResultCurrency(projection.withDeposits)
            )
        }
        Text(
            "Includes continued monthly deposits.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun YearWiseInterestCard(result: CalculatorResult) {
    DetailCard {
        Text("Year-wise interest", style = MaterialTheme.typography.titleLarge)
        result.fyWiseBreakdown.take(8).forEach { row ->
            CompactResultRow(
                label = "FY ${row.financialYear}",
                value = formatResultCurrency(row.interestAccrued),
                valueColor = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            "Interest is taxable as per applicable rules. Consult a tax professional for exact liability.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDarkScheme()) 0.78f else 0.98f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDarkScheme()) 0.38f else 0.28f)),
        shadowElevation = if (isDarkScheme()) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = { content() }
        )
    }
}

private data class ResultMetric(val label: String, val value: Double)

private data class ResultDisplayRow(
    val label: String,
    val value: String,
    val emphasize: Boolean = false
)

private data class ResultContextDisplay(
    val primary: ResultDisplayRow,
    val supporting: List<ResultDisplayRow>
)

private fun CalculatorResult.heroMetric(): ResultMetric =
    when (schemeType) {
        SchemeType.MIS -> ResultMetric("Monthly payout", monthlyIncome ?: 0.0)
        SchemeType.SCSS -> ResultMetric("Quarterly payout", monthlyIncome ?: 0.0)
        else -> ResultMetric("Total payable", maturityAmount)
    }

private fun CalculatorResult.summaryRows(): List<ResultDisplayRow> =
    when (schemeType) {
        SchemeType.MIS -> listOf(
            ResultDisplayRow("Investment", formatResultCurrency(totalDeposited)),
            ResultDisplayRow("Total interest", formatResultCurrency(interestEarned), emphasize = true),
            ResultDisplayRow("Principal at maturity", formatResultCurrency(maturityAmount)),
            ResultDisplayRow("Total received", formatResultCurrency(totalReceived)),
            ResultDisplayRow("Maturity date", formatCalculatorDate(maturityDate))
        )
        SchemeType.SCSS -> listOf(
            ResultDisplayRow("Investment", formatResultCurrency(totalDeposited)),
            ResultDisplayRow("Total interest", formatResultCurrency(interestEarned), emphasize = true),
            ResultDisplayRow("Principal at maturity", formatResultCurrency(maturityAmount)),
            ResultDisplayRow("Total received", formatResultCurrency(totalReceived)),
            ResultDisplayRow("Maturity date", formatCalculatorDate(maturityDate))
        )
        else -> listOf(
            ResultDisplayRow("Deposited", formatResultCurrency(totalDeposited)),
            ResultDisplayRow("Interest earned", formatResultCurrency(interestEarned), emphasize = true),
            ResultDisplayRow("Maturity date", formatCalculatorDate(maturityDate))
        )
    }

private fun CalculatorResult.contextDisplay(): ResultContextDisplay {
    val input = inputSummary
    val rate = ResultDisplayRow("Rate", "${ratePercent.formatRate()}%")
    val opening = ResultDisplayRow("Opening date", input.startDate.formatIfAvailable())
    return when (schemeType) {
        SchemeType.RD -> ResultContextDisplay(
            primary = ResultDisplayRow("Monthly amount", formatResultCurrency(input.amount)),
            supporting = listOf(
                ResultDisplayRow("Installments", input.installmentsPaid.toString()),
                ResultDisplayRow("From", input.startDate.formatIfAvailable()),
                ResultDisplayRow("Maturity date", formatCalculatorDate(maturityDate)),
                rate
            )
        )
        SchemeType.TD -> investmentContextRows("Investment", input.tdTenure.label, opening, rate)
        SchemeType.NSC -> investmentContextRows("Investment", "5 years", opening, rate)
        SchemeType.KVP -> investmentContextRows("Investment", "Estimated doubling term", opening, rate)
        SchemeType.MSSC -> investmentContextRows("Investment", "2 years", opening, rate)
        SchemeType.MIS -> ResultContextDisplay(
            primary = ResultDisplayRow("Investment", formatResultCurrency(input.amount)),
            supporting = listOf(
                ResultDisplayRow("Monthly payout", formatResultCurrency(monthlyIncome ?: 0.0)),
                ResultDisplayRow("Term", "5 years"),
                ResultDisplayRow("Total interest", formatResultCurrency(interestEarned)),
                rate
            )
        )
        SchemeType.PPF -> yearlyDepositContextRows("Yearly deposit", "15 years", opening, rate)
        SchemeType.SSY -> yearlyDepositContextRows("Yearly deposit", "21 years", opening, rate)
        SchemeType.SCSS -> ResultContextDisplay(
            primary = ResultDisplayRow("Investment", formatResultCurrency(input.amount)),
            supporting = listOf(
                ResultDisplayRow("Quarterly payout", formatResultCurrency(monthlyIncome ?: 0.0)),
                ResultDisplayRow("Term", if (input.scssExtended) "8 years" else "5 years"),
                ResultDisplayRow("Maturity date", formatCalculatorDate(maturityDate)),
                rate
            )
        )
        SchemeType.SB -> ResultContextDisplay(
            primary = ResultDisplayRow("Balance", formatResultCurrency(input.amount)),
            supporting = listOf(
                ResultDisplayRow("From", input.startDate.formatIfAvailable()),
                ResultDisplayRow("To", (input.toDate ?: maturityDate).formatIfAvailable()),
                ResultDisplayRow("Interest earned", formatResultCurrency(interestEarned)),
                rate
            )
        )
        SchemeType.SIMPLE_INTEREST,
        SchemeType.PMI -> customContextRows("Simple interest", rate)
        SchemeType.COMPOUND_INTEREST -> customContextRows("Compound interest", rate)
        SchemeType.RD_REBATE -> ResultContextDisplay(
            primary = ResultDisplayRow("Installment amount", formatResultCurrency(input.amount)),
            supporting = listOf(
                ResultDisplayRow("Installments", input.installmentsPaid.toString()),
                ResultDisplayRow("Rebate estimate", formatResultCurrency(interestEarned)),
                rate
            )
        )
    }
}

private fun CalculatorResult.investmentContextRows(
    amountLabel: String,
    tenure: String,
    opening: ResultDisplayRow,
    rate: ResultDisplayRow
): ResultContextDisplay =
    ResultContextDisplay(
        primary = ResultDisplayRow(amountLabel, formatResultCurrency(inputSummary.amount)),
        supporting = listOf(
            ResultDisplayRow("Tenure", tenure),
            opening,
            ResultDisplayRow("Maturity date", formatCalculatorDate(maturityDate)),
            rate
        )
    )

private fun CalculatorResult.yearlyDepositContextRows(
    amountLabel: String,
    term: String,
    opening: ResultDisplayRow,
    rate: ResultDisplayRow
): ResultContextDisplay =
    ResultContextDisplay(
        primary = ResultDisplayRow(amountLabel, formatResultCurrency(inputSummary.amount)),
        supporting = listOf(
            ResultDisplayRow("Term", term),
            opening,
            ResultDisplayRow("Maturity date", formatCalculatorDate(maturityDate)),
            rate
        )
    )

private fun CalculatorResult.customContextRows(mode: String, rate: ResultDisplayRow): ResultContextDisplay =
    ResultContextDisplay(
        primary = ResultDisplayRow("Principal", formatResultCurrency(inputSummary.amount)),
        supporting = listOf(
            ResultDisplayRow("Time period", "${inputSummary.customYears.formatRate()} years"),
            ResultDisplayRow("Mode", mode),
            rate
        )
    )

private val WhatsAppGreen = Color(0xFF25D366)

private fun Double.formatRate(): String =
    BigDecimal(this).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()

internal fun formatResultCurrency(value: Double): String = formatIndianCurrency(value)

private fun LocalDate.formatIfAvailable(): String =
    if (this == LocalDate.MIN) "-" else formatCalculatorDate(this)

@Composable
private fun isDarkScheme(): Boolean =
    MaterialTheme.colorScheme.background.luminance() < 0.5f
