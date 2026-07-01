package roy.ij.postofficesaathi.domain.calculator

import java.time.LocalDate
import kotlin.math.ln
import kotlin.math.pow

object InterestEngine {
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
            maturityDate = input.startDate.plusYears(input.tdTenure.years.toLong())
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
            interestEarnedOverride = interest
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
            maturityDate = input.startDate.plusYears(years.toLong())
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
            interestEarnedOverride = interest
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
            maturityDate = input.startDate.plusMonths((input.customYears * 12).toLong())
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
            fyWiseBreakdown = fyRows(input.startDate, maturityDate, roundedInterest),
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

    private fun fyRows(startDate: LocalDate, maturityDate: LocalDate, totalInterest: Double): List<FYInterestRow> {
        val years = (maturityDate.year - startDate.year).coerceAtLeast(1)
        val perYear = totalInterest / years
        var cumulative = 0.0
        return (0 until years).map { index ->
            cumulative += perYear
            FYInterestRow(
                financialYear = financialYearLabel(startDate.plusYears(index.toLong())),
                interestAccrued = roundMoney(perYear),
                cumulativeTotal = roundMoney(cumulative)
            )
        }
    }
}
