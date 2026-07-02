package roy.ij.postofficesaathi.domain.calculator

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.ln
import kotlin.math.pow

object InterestEngine {
    private data class InterestEvent(val date: LocalDate, val amount: Double)

    fun calculate(input: CalculatorInput): CalculatorResult =
        when (input.schemeType) {
            SchemeType.RD -> recurringDeposit(input)
            SchemeType.TD -> timeDeposit(input)
            SchemeType.MIS -> monthlyIncome(input)
            SchemeType.NSC -> fixedAnnual(input, years = 5)
            SchemeType.KVP -> kisanVikasPatra(input)
            SchemeType.PPF -> recurringAnnual(input, years = 15, depositYears = 15)
            SchemeType.SSY -> recurringAnnual(input, years = 21, depositYears = 15)
            SchemeType.SCSS -> scss(input)
            SchemeType.SB -> savingsAccount(input)
            SchemeType.MSSC -> fixedCompound(input, years = 2, periodsPerYear = 4)
            SchemeType.SIMPLE_INTEREST -> simpleInterest(input)
            SchemeType.COMPOUND_INTEREST -> customCompound(input)
            SchemeType.RD_REBATE -> rdRebate(input)
            SchemeType.PMI -> simpleInterest(input.copy(customYears = input.customYears.coerceAtLeast(0.0)))
        }

    private fun recurringDeposit(input: CalculatorInput): CalculatorResult {
        val months = 60
        val monthlyInstallment = input.amount
        val paidMonths = input.installmentsPaid.coerceIn(0, months)
        val corpus = rdMaturity(monthlyInstallment, input.ratePercent, paidMonths)
        val deposited = monthlyInstallment * paidMonths
        val result = baseResult(
            input = input,
            title = SchemeType.RD.displayName,
            totalDeposited = deposited,
            maturityAmount = corpus,
            maturityDate = input.startDate.plusMonths(months.toLong()),
            fyWiseBreakdown = rdFyRows(input.startDate, monthlyInstallment, input.ratePercent, paidMonths),
            notes = listOf("Recurring Deposit tenure is shown as 60 months.")
        )
        return result.copy(
            continuationProjections = continuationProjections(
                currentCorpus = result.maturityAmount,
                periodicContribution = monthlyInstallment,
                annualRatePercent = input.ratePercent
            )
        )
    }

    private fun timeDeposit(input: CalculatorInput): CalculatorResult =
        baseResult(
            input = input,
            title = "Time Deposit ${input.tdTenure.label}",
            totalDeposited = input.amount,
            maturityAmount = timeDepositMaturity(input.amount, input.ratePercent, input.tdTenure.years),
            maturityDate = input.startDate.plusYears(input.tdTenure.years.toLong()),
            fyWiseBreakdown = timeDepositFyRows(input.startDate, input.amount, input.ratePercent, input.tdTenure.years)
        )

    private fun monthlyIncome(input: CalculatorInput): CalculatorResult {
        val monthlyIncome = input.amount * input.ratePercent / 100.0 / 12.0
        val totalInterest = monthlyIncome * 60
        return baseResult(
            input = input,
            title = SchemeType.MIS.displayName,
            totalDeposited = input.amount,
            maturityAmount = input.amount,
            maturityDate = input.startDate.plusYears(5),
            monthlyIncome = roundMoney(monthlyIncome),
            interestEarnedOverride = totalInterest,
            totalReceivedOverride = input.amount + totalInterest,
            fyWiseBreakdown = payoutFyRows(input.startDate, monthlyIncome, payments = 60, monthsBetweenPayments = 1, expectedTotal = totalInterest),
            notes = listOf("Monthly income is shown before tax.")
        )
    }

    private fun fixedAnnual(input: CalculatorInput, years: Int): CalculatorResult =
        fixedCompound(input, years = years, periodsPerYear = 1)

    private fun kisanVikasPatra(input: CalculatorInput): CalculatorResult {
        val rate = input.ratePercent / 100.0
        val yearsToDouble = ln(2.0) / ln(1.0 + rate)
        val monthsToDouble = (yearsToDouble * 12.0).toLong().coerceAtLeast(1)
        return baseResult(
            input = input,
            title = SchemeType.KVP.displayName,
            totalDeposited = input.amount,
            maturityAmount = input.amount * 2.0,
            maturityDate = input.startDate.plusMonths(monthsToDouble),
            fyWiseBreakdown = kisanVikasPatraFyRows(input.startDate, input.amount, input.ratePercent, monthsToDouble),
            notes = listOf("KVP maturity is estimated from the active annual rate.")
        )
    }

    private fun recurringAnnual(input: CalculatorInput, years: Int, depositYears: Int): CalculatorResult {
        var corpus = 0.0
        val contributionYears = (depositYears - input.yearsCompleted).coerceIn(0, years)
        repeat(years) { index ->
            if (index < contributionYears) corpus += input.amount
            corpus *= (1 + input.ratePercent / 100.0)
        }
        val deposited = input.amount * contributionYears
        val note = if (input.schemeType == SchemeType.SSY && input.girlsBirthDate != null) {
            val ageAtStart = input.startDate.year - input.girlsBirthDate.year
            if (ageAtStart > 10) {
                listOf("SSY accounts are normally opened for girls aged 10 or below. Showing calculation anyway.")
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
        return baseResult(
            input = input,
            title = input.schemeType.displayName,
            totalDeposited = deposited,
            maturityAmount = corpus,
            maturityDate = input.startDate.plusYears(years.toLong()),
            fyWiseBreakdown = emptyList(),
            notes = note
        )
    }

    private fun scss(input: CalculatorInput): CalculatorResult {
        val years = if (input.scssExtended) 8 else 5
        val quarterlyPayout = input.amount * input.ratePercent / 100.0 / 4.0
        val interest = quarterlyPayout * years * 4
        return baseResult(
            input = input,
            title = SchemeType.SCSS.displayName,
            totalDeposited = input.amount,
            maturityAmount = input.amount,
            maturityDate = input.startDate.plusYears(years.toLong()),
            monthlyIncome = roundMoney(quarterlyPayout),
            interestEarnedOverride = interest,
            totalReceivedOverride = input.amount + interest,
            fyWiseBreakdown = payoutFyRows(input.startDate, quarterlyPayout, payments = years * 4, monthsBetweenPayments = 3, expectedTotal = interest),
            notes = listOf("SCSS interest is shown as quarterly payout.")
        )
    }

    private fun savingsAccount(input: CalculatorInput): CalculatorResult {
        val endDate = input.toDate ?: input.startDate.plusYears(1)
        val days = java.time.temporal.ChronoUnit.DAYS.between(input.startDate, endDate).coerceAtLeast(0)
        val interest = input.amount * input.ratePercent / 100.0 * days / 365.0
        return baseResult(
            input = input,
            title = SchemeType.SB.displayName,
            totalDeposited = input.amount,
            maturityAmount = input.amount + interest,
            maturityDate = endDate,
            interestEarnedOverride = interest,
            fyWiseBreakdown = dailySimpleFyRows(input.startDate, endDate, interest)
        )
    }

    private fun fixedCompound(
        input: CalculatorInput,
        years: Int,
        periodsPerYear: Int,
        title: String = input.schemeType.displayName
    ): CalculatorResult {
        val periods = years * periodsPerYear
        val maturity = input.amount * (1 + input.ratePercent / 100.0 / periodsPerYear).pow(periods)
        return baseResult(
            input = input,
            title = title,
            totalDeposited = input.amount,
            maturityAmount = maturity,
            maturityDate = input.startDate.plusYears(years.toLong()),
            fyWiseBreakdown = compoundFyRows(
                startDate = input.startDate,
                principal = input.amount,
                annualRatePercent = input.ratePercent,
                periodsPerYear = periodsPerYear,
                periods = periods.toDouble(),
                expectedTotal = maturity - input.amount
            )
        )
    }

    private fun simpleInterest(input: CalculatorInput): CalculatorResult {
        val interest = input.amount * input.ratePercent * input.customYears / 100.0
        return baseResult(
            input = input,
            title = "Simple Interest",
            totalDeposited = input.amount,
            maturityAmount = input.amount + interest,
            maturityDate = input.startDate.plusMonths((input.customYears * 12).toLong()),
            interestEarnedOverride = interest,
            fyWiseBreakdown = dailySimpleFyRows(
                startDate = input.startDate,
                endDate = input.startDate.plusMonths((input.customYears * 12).toLong()),
                totalInterest = interest
            )
        )
    }

    private fun customCompound(input: CalculatorInput): CalculatorResult {
        val periodsPerYear = input.compoundFrequencyOption.periodsPerYear
        val periods = input.customYears * periodsPerYear
        val maturity = input.amount * (1 + input.ratePercent / 100.0 / periodsPerYear).pow(periods)
        return baseResult(
            input = input,
            title = "Compound Interest",
            totalDeposited = input.amount,
            maturityAmount = maturity,
            maturityDate = input.startDate.plusMonths((input.customYears * 12).toLong()),
            fyWiseBreakdown = compoundFyRows(
                startDate = input.startDate,
                principal = input.amount,
                annualRatePercent = input.ratePercent,
                periodsPerYear = periodsPerYear,
                periods = periods,
                expectedTotal = maturity - input.amount
            )
        )
    }

    private fun rdRebate(input: CalculatorInput): CalculatorResult {
        val rebate = input.amount * 0.02 * input.installmentsPaid.coerceAtLeast(0)
        return CalculatorResult(
            schemeType = SchemeType.RD_REBATE,
            title = SchemeType.RD_REBATE.displayName,
            ratePercent = input.ratePercent,
            rateLabel = "${input.ratePercent}% p.a.",
            totalDeposited = input.amount * input.installmentsPaid,
            interestEarned = roundMoney(rebate),
            maturityAmount = roundMoney(input.amount * input.installmentsPaid - rebate),
            maturityDate = input.startDate,
            notes = listOf("Rebate is an estimate for missed RD installments."),
            inputSummary = input.toSummary()
        )
    }

    private fun baseResult(
        input: CalculatorInput,
        title: String,
        totalDeposited: Double,
        maturityAmount: Double,
        maturityDate: LocalDate,
        monthlyIncome: Double? = null,
        interestEarnedOverride: Double? = null,
        totalReceivedOverride: Double? = null,
        fyWiseBreakdown: List<FYInterestRow> = emptyList(),
        notes: List<String> = emptyList()
    ): CalculatorResult {
        val interestEarned = interestEarnedOverride ?: (maturityAmount - totalDeposited)
        val roundedInterest = roundMoney(interestEarned)
        val roundedMaturity = roundMoney(maturityAmount)
        return CalculatorResult(
            schemeType = input.schemeType,
            title = title,
            ratePercent = input.ratePercent,
            rateLabel = "${input.ratePercent}% p.a.",
            totalDeposited = roundMoney(totalDeposited),
            interestEarned = roundedInterest,
            maturityAmount = roundedMaturity,
            totalReceived = roundMoney(totalReceivedOverride ?: roundedMaturity),
            maturityDate = maturityDate,
            monthlyIncome = monthlyIncome,
            fyWiseBreakdown = fyWiseBreakdown,
            notes = notes,
            inputSummary = input.toSummary()
        )
    }

    private fun CalculatorInput.toSummary(): CalculatorInputSummary =
        CalculatorInputSummary(
            schemeType = schemeType,
            amount = amount,
            startDate = startDate,
            toDate = toDate,
            installmentsPaid = installmentsPaid,
            tdTenure = tdTenure,
            customType = customType,
            customYears = customYears,
            compoundFrequencyOption = compoundFrequencyOption,
            compoundingFrequency = compoundingFrequency,
            scssExtended = scssExtended
        )

    private fun rdMaturity(monthlyDeposit: Double, annualRatePercent: Double, months: Int): Double {
        if (months <= 0 || monthlyDeposit <= 0.0) return 0.0
        val quarterlyRate = annualRatePercent / 400.0
        val quarters = months / 3.0
        return monthlyDeposit * ((1 + quarterlyRate).pow(quarters) - 1) / (1 - (1 + quarterlyRate).pow(-1.0 / 3.0))
    }

    private fun timeDepositAnnualInterest(principal: Double, annualRatePercent: Double): Double {
        if (principal <= 0.0) return 0.0
        val quarterlyRate = annualRatePercent / 400.0
        return principal * ((1 + quarterlyRate).pow(4) - 1)
    }

    private fun timeDepositMaturity(principal: Double, annualRatePercent: Double, years: Int): Double =
        principal + timeDepositAnnualInterest(principal, annualRatePercent) * years

    private fun timeDepositFyRows(startDate: LocalDate, principal: Double, annualRatePercent: Double, years: Int): List<FYInterestRow> {
        val annualInterest = timeDepositAnnualInterest(principal, annualRatePercent)
        val events = (1..years).map { year ->
            InterestEvent(startDate.plusYears(year.toLong()), annualInterest)
        }
        return groupEventsByFinancialYear(events, expectedTotal = annualInterest * years)
    }

    private fun payoutFyRows(
        startDate: LocalDate,
        payout: Double,
        payments: Int,
        monthsBetweenPayments: Int,
        expectedTotal: Double
    ): List<FYInterestRow> {
        val events = (1..payments).map { payment ->
            InterestEvent(startDate.plusMonths((payment * monthsBetweenPayments).toLong()), payout)
        }
        return groupEventsByFinancialYear(events, expectedTotal = expectedTotal)
    }

    private fun rdFyRows(
        startDate: LocalDate,
        monthlyDeposit: Double,
        annualRatePercent: Double,
        installmentsPaid: Int
    ): List<FYInterestRow> {
        val paidMonths = installmentsPaid.coerceAtLeast(0)
        if (paidMonths == 0 || monthlyDeposit <= 0.0) return emptyList()

        val rows = mutableListOf<FYInterestRow>()
        var previousInterest = 0.0
        var previousRoundedCumulative = 0.0
        var fyStartYear = if (startDate.monthValue >= 4) startDate.year else startDate.year - 1
        val startMonth = YearMonth.from(startDate)

        while (true) {
            val fyEnd = LocalDate.of(fyStartYear + 1, 3, 31)
            val monthsAtFyEnd = (ChronoUnit.MONTHS.between(startMonth, YearMonth.from(fyEnd)) + 1).toInt()
            val monthsPaid = monthsAtFyEnd.coerceIn(0, paidMonths)
            if (monthsPaid > 0) {
                val cumulativeInterest = rdMaturity(monthlyDeposit, annualRatePercent, monthsPaid) - monthlyDeposit * monthsPaid
                val interestForFy = cumulativeInterest - previousInterest
                val roundedInterest = roundMoney(interestForFy)
                val roundedCumulative = roundMoney(previousRoundedCumulative + roundedInterest)
                val labelDate = if (monthsPaid == paidMonths) startDate.plusMonths(monthsPaid.toLong()) else fyEnd
                rows += FYInterestRow(
                    financialYear = financialYearLabel(labelDate),
                    interestAccrued = roundedInterest,
                    cumulativeTotal = roundedCumulative
                )
                previousInterest = cumulativeInterest
                previousRoundedCumulative = roundedCumulative
            }
            if (monthsPaid >= paidMonths) break
            fyStartYear += 1
        }

        return adjustFinalRow(rows, expectedTotal = rdMaturity(monthlyDeposit, annualRatePercent, paidMonths) - monthlyDeposit * paidMonths)
    }

    private fun compoundFyRows(
        startDate: LocalDate,
        principal: Double,
        annualRatePercent: Double,
        periodsPerYear: Int,
        periods: Double,
        expectedTotal: Double
    ): List<FYInterestRow> {
        if (periods <= 0 || periodsPerYear <= 0 || principal <= 0.0) return emptyList()
        val monthsBetweenPeriods = 12 / periodsPerYear
        val periodicRate = annualRatePercent / 100.0 / periodsPerYear
        var balance = principal
        val fullPeriods = periods.toInt()
        val events = (1..fullPeriods).map { period ->
            val interest = balance * periodicRate
            balance += interest
            InterestEvent(startDate.plusMonths((period * monthsBetweenPeriods).toLong()), interest)
        }.toMutableList()
        val partialPeriod = periods - fullPeriods
        if (partialPeriod > 0.000001) {
            val interest = balance * ((1 + periodicRate).pow(partialPeriod) - 1)
            events += InterestEvent(startDate.plusMonths((periods * monthsBetweenPeriods).toLong()), interest)
        }
        return groupEventsByFinancialYear(events, expectedTotal = expectedTotal)
    }

    private fun kisanVikasPatraFyRows(
        startDate: LocalDate,
        principal: Double,
        annualRatePercent: Double,
        maturityMonths: Long
    ): List<FYInterestRow> {
        if (principal <= 0.0 || maturityMonths <= 0) return emptyList()
        val events = mutableListOf<InterestEvent>()
        var balance = principal
        var month = 12L
        while (month < maturityMonths) {
            val interest = balance * annualRatePercent / 100.0
            balance += interest
            events += InterestEvent(startDate.plusMonths(month), interest)
            month += 12
        }
        val remainingInterest = principal - events.sumOf { it.amount }
        if (remainingInterest > 0.0) {
            events += InterestEvent(startDate.plusMonths(maturityMonths), remainingInterest)
        }
        return groupEventsByFinancialYear(events, expectedTotal = principal)
    }

    private fun dailySimpleFyRows(startDate: LocalDate, endDate: LocalDate, totalInterest: Double): List<FYInterestRow> {
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).coerceAtLeast(0)
        if (totalDays == 0L || totalInterest == 0.0) return emptyList()
        val events = mutableListOf<InterestEvent>()
        var cursor = startDate
        while (cursor.isBefore(endDate)) {
            val fyStartYear = if (cursor.monthValue >= 4) cursor.year else cursor.year - 1
            val nextFyStart = LocalDate.of(fyStartYear + 1, 4, 1)
            val segmentEnd = minOf(endDate, nextFyStart)
            val segmentDays = ChronoUnit.DAYS.between(cursor, segmentEnd)
            val segmentInterest = totalInterest * segmentDays / totalDays
            events += InterestEvent(segmentEnd.minusDays(1), segmentInterest)
            cursor = segmentEnd
        }
        return groupEventsByFinancialYear(events, expectedTotal = totalInterest)
    }

    private fun continuationProjections(
        currentCorpus: Double,
        periodicContribution: Double,
        annualRatePercent: Double
    ): Map<Int, ContinuationProjection> =
        (6..10).associateWith { year ->
            val months = year * 12
            val monthlyRate = annualRatePercent / 100.0 / 12.0
            var withDeposits = currentCorpus
            var withoutDeposits = currentCorpus
            repeat(months) {
                withDeposits = (withDeposits + periodicContribution) * (1 + monthlyRate)
                withoutDeposits *= (1 + monthlyRate)
            }
            ContinuationProjection(
                year = year,
                withDeposits = roundMoney(withDeposits),
                withoutDeposits = roundMoney(withoutDeposits)
            )
        }

    private fun groupEventsByFinancialYear(events: List<InterestEvent>, expectedTotal: Double): List<FYInterestRow> {
        if (events.isEmpty()) return emptyList()
        val grouped = linkedMapOf<String, Double>()
        events.sortedBy { it.date }.forEach { event ->
            val label = financialYearLabel(event.date)
            grouped[label] = (grouped[label] ?: 0.0) + event.amount
        }
        return adjustFinalRow(
            grouped.map { (label, amount) ->
                FYInterestRow(
                    financialYear = label,
                    interestAccrued = roundMoney(amount),
                    cumulativeTotal = 0.0
                )
            },
            expectedTotal = expectedTotal
        )
    }

    private fun adjustFinalRow(rows: List<FYInterestRow>, expectedTotal: Double): List<FYInterestRow> {
        if (rows.isEmpty()) return rows
        val roundedExpected = roundMoney(expectedTotal)
        val roundedRows = rows.toMutableList()
        val roundedSum = roundMoney(roundedRows.sumOf { it.interestAccrued })
        val delta = roundMoney(roundedExpected - roundedSum)
        if (delta != 0.0) {
            val last = roundedRows.last()
            roundedRows[roundedRows.lastIndex] = last.copy(interestAccrued = roundMoney(last.interestAccrued + delta))
        }
        var cumulative = 0.0
        return roundedRows.map { row ->
            cumulative = roundMoney(cumulative + row.interestAccrued)
            row.copy(cumulativeTotal = cumulative)
        }
    }
}
