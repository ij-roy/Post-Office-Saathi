package roy.ij.postofficesaathi.domain.calculator

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val DateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

fun formatCalculatorDate(date: LocalDate): String = date.format(DateFormatter)

fun roundMoney(value: Double): Double =
    BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toDouble()

fun formatIndianCurrency(value: Double): String {
    val rounded = BigDecimal(value).setScale(2, RoundingMode.HALF_UP).abs()
    val parts = rounded.toPlainString().split('.')
    val whole = parts[0]
    val decimals = parts.getOrElse(1) { "00" }.padEnd(2, '0').take(2)
    val grouped = if (whole.length <= 3) {
        whole
    } else {
        val lastThree = whole.takeLast(3)
        val prefix = whole.dropLast(3)
        prefix.reversed()
            .chunked(2)
            .joinToString(",")
            .reversed() + "," + lastThree
    }
    val sign = if (value < 0 && abs(value) >= 0.005) "-" else ""
    return "${sign}₹$grouped.$decimals"
}

fun financialYearLabel(date: LocalDate): String {
    val startYear = if (date.monthValue >= 4) date.year else date.year - 1
    val nextYearShort = ((startYear + 1) % 100).toString().padStart(2, '0')
    return "$startYear-$nextYearShort"
}
