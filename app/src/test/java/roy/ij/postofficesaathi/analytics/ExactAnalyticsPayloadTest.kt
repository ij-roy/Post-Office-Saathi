package roy.ij.postofficesaathi.analytics

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import roy.ij.postofficesaathi.data.storage.PublicDocumentRef
import roy.ij.postofficesaathi.domain.calculator.CalculatorInput
import roy.ij.postofficesaathi.domain.calculator.CompoundFrequencyOption
import roy.ij.postofficesaathi.domain.calculator.CompoundingFrequency
import roy.ij.postofficesaathi.domain.calculator.CustomCalculatorType
import roy.ij.postofficesaathi.domain.calculator.InterestEngine
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.TDTenure
import roy.ij.postofficesaathi.domain.forms.FormItem
import roy.ij.postofficesaathi.domain.pdf.PdfLayoutType
import roy.ij.postofficesaathi.ui.calculator.scheme.SchemeCalculatorUiState
import roy.ij.postofficesaathi.ui.calculator.scheme.calculationFailedParams
import roy.ij.postofficesaathi.ui.calculator.scheme.calculationSucceededParams
import roy.ij.postofficesaathi.ui.calculator.suggest.agentSearchParams
import roy.ij.postofficesaathi.ui.calculator.suggest.planSuggestedParams
import roy.ij.postofficesaathi.ui.forms.formActionParams
import roy.ij.postofficesaathi.ui.forms.formsSearchParams
import roy.ij.postofficesaathi.ui.pdf.pdfCreateStartedParams
import roy.ij.postofficesaathi.ui.pdf.pdfCreateSucceededParams
import roy.ij.postofficesaathi.ui.pdf.requestedPdfFileName

class ExactAnalyticsPayloadTest {
    @Test
    fun formsSearchUsesExactSearchTextAndResultCount() {
        val params = formsSearchParams(query = "Post Of", resultCount = 3)

        assertEquals(AnalyticsFlow.Forms, params[AnalyticsParam.Flow])
        assertEquals("Post Of", params[AnalyticsParam.SearchText])
        assertEquals(3, params[AnalyticsParam.ResultCount])
        assertFalse(params.containsKey("query_text_safe"))
        assertFalse(params.containsKey("query_length_bucket"))
        assertFalse(params.containsKey("result_count_bucket"))
    }

    @Test
    fun formActionParamsIncludeExactSearchActionAndSavedDocumentDetails() {
        val form = FormItem(
            id = "sb3",
            title = "Savings Bank Account Opening",
            description = "Open an account",
            category = "Savings",
            language = "English",
            file = "forms/sb3.pdf",
            keywords = listOf("account"),
            isDownloaded = false
        )
        val document = PublicDocumentRef(
            displayName = "sb3-2.pdf",
            uriString = "content://forms/sb3-2.pdf",
            newlySaved = true
        )

        val params = formActionParams(form, query = "Account", actionType = "open", document = document)

        assertEquals("Account", params[AnalyticsParam.SearchText])
        assertEquals("open", params[AnalyticsParam.ActionType])
        assertEquals("sb3-2.pdf", params[AnalyticsParam.DocumentName])
        assertEquals(true, params[AnalyticsParam.NewlySaved])
    }

    @Test
    fun pdfCreateParamsUseExactCustomerRequestedNameAndFinalFileName() {
        val requested = requestedPdfFileName(
            customerName = "Sita Devi",
            layoutType = PdfLayoutType.TwoDocuments,
            date = LocalDate.parse("2026-07-02")
        )
        val document = PublicDocumentRef(
            displayName = "Sita_Devi_Two_Documents_2026-07-02-2.pdf",
            uriString = "content://pdfs/sita",
            newlySaved = true
        )

        val started = pdfCreateStartedParams(
            layoutType = PdfLayoutType.TwoDocuments,
            customerName = "Sita Devi",
            imageCount = 2,
            requestedPdfFilename = requested
        )
        val succeeded = pdfCreateSucceededParams(
            layoutType = PdfLayoutType.TwoDocuments,
            customerName = "Sita Devi",
            imageCount = 2,
            requestedPdfFilename = requested,
            document = document
        )

        assertEquals("Sita_Devi_Two_Documents_2026-07-02.pdf", requested)
        assertEquals("Sita Devi", started[AnalyticsParam.CustomerName])
        assertEquals(requested, started[AnalyticsParam.RequestedPdfFilename])
        assertEquals("Sita Devi", succeeded[AnalyticsParam.CustomerName])
        assertEquals(requested, succeeded[AnalyticsParam.RequestedPdfFilename])
        assertEquals(document.displayName, succeeded[AnalyticsParam.PdfFilename])
    }

    @Test
    fun rdCalculationParamsIncludeExactInputsAndResultsWithoutBuckets() {
        val input = CalculatorInput(
            schemeType = SchemeType.RD,
            amount = 500.0,
            startDate = LocalDate.parse("2026-07-02"),
            ratePercent = 6.7,
            compoundingFrequency = CompoundingFrequency.QUARTERLY,
            installmentsPaid = 36
        )
        val result = InterestEngine.calculate(input)

        val params = calculationSucceededParams(
            input = input,
            result = result,
            ratesVersion = "2026-07-01",
            usedFallback = false
        )

        assertEquals(SchemeType.RD.name, params[AnalyticsParam.SchemeType])
        assertEquals(500.0, params[AnalyticsParam.MonthlyDeposit])
        assertEquals(36, params[AnalyticsParam.InstallmentsPaid])
        assertEquals(6.7, params[AnalyticsParam.InterestRate])
        assertEquals(result.totalDeposited, params[AnalyticsParam.TotalDeposited])
        assertEquals(result.interestEarned, params[AnalyticsParam.InterestEarned])
        assertEquals(result.maturityAmount, params[AnalyticsParam.MaturityAmount])
        assertFalse(params.containsKey("investment_amount_bucket"))
        assertFalse(params.containsKey("tenure_bucket"))
    }

    @Test
    fun tdCalculationParamsIncludeExactTenureAndDepositAmount() {
        val input = CalculatorInput(
            schemeType = SchemeType.TD,
            amount = 10000.0,
            startDate = LocalDate.parse("2026-07-02"),
            ratePercent = 7.5,
            compoundingFrequency = CompoundingFrequency.QUARTERLY,
            tdTenure = TDTenure.ThreeYears
        )
        val result = InterestEngine.calculate(input)

        val params = calculationSucceededParams(input, result, ratesVersion = null, usedFallback = true)

        assertEquals(10000.0, params[AnalyticsParam.DepositAmount])
        assertEquals(TDTenure.ThreeYears.jsonKey, params[AnalyticsParam.TDTenure])
        assertEquals(true, params[AnalyticsParam.UsedFallback])
    }

    @Test
    fun calculationParamsCoverEverySchemeType() {
        SchemeType.entries.forEach { schemeType ->
            val input = calculatorInputFor(schemeType)
            val result = InterestEngine.calculate(input)

            val params = calculationSucceededParams(
                input = input,
                result = result,
                ratesVersion = "2026-07-01",
                usedFallback = false
            )

            assertEquals(schemeType.name, params[AnalyticsParam.SchemeType])
            assertEquals(input.amount, params[AnalyticsParam.Amount])
            assertEquals(input.ratePercent, params[AnalyticsParam.InterestRate])
            assertEquals(result.totalDeposited, params[AnalyticsParam.TotalDeposited])
            assertEquals(result.interestEarned, params[AnalyticsParam.InterestEarned])
            assertEquals(result.maturityAmount, params[AnalyticsParam.MaturityAmount])
            assertEquals(result.maturityDate.toString(), params[AnalyticsParam.MaturityDate])

            when (schemeType) {
                SchemeType.RD,
                SchemeType.RD_REBATE -> {
                    assertEquals(input.amount, params[AnalyticsParam.MonthlyDeposit])
                    assertEquals(input.installmentsPaid, params[AnalyticsParam.InstallmentsPaid])
                }
                SchemeType.TD -> {
                    assertEquals(input.amount, params[AnalyticsParam.DepositAmount])
                    assertEquals(input.tdTenure.jsonKey, params[AnalyticsParam.TDTenure])
                }
                SchemeType.MIS,
                SchemeType.NSC,
                SchemeType.KVP,
                SchemeType.SCSS,
                SchemeType.MSSC -> assertEquals(input.amount, params[AnalyticsParam.DepositAmount])
                SchemeType.PPF,
                SchemeType.SSY -> {
                    assertEquals(input.amount, params[AnalyticsParam.YearlyDeposit])
                    assertEquals(input.yearsCompleted, params[AnalyticsParam.YearsCompleted])
                }
                SchemeType.SB -> {
                    assertEquals(input.amount, params[AnalyticsParam.BalanceAmount])
                    assertEquals(input.toDate.toString(), params[AnalyticsParam.ToDate])
                }
                SchemeType.SIMPLE_INTEREST,
                SchemeType.COMPOUND_INTEREST,
                SchemeType.PMI -> {
                    assertEquals(input.amount, params[AnalyticsParam.PrincipalAmount])
                    assertEquals(input.customType.name, params[AnalyticsParam.CustomType])
                    assertEquals(input.customYears, params[AnalyticsParam.CustomYears])
                }
            }
        }
    }

    @Test
    fun calculationFailedParamsKeepExactOverriddenRateText() {
        val state = SchemeCalculatorUiState(
            schemeType = SchemeType.COMPOUND_INTEREST,
            amount = "10000.50",
            isRateOverridden = true,
            rateOverride = "7.10",
            isLoading = false
        )

        val params = calculationFailedParams(state, mapOf("rate" to "Please enter a valid rate."))

        assertEquals("10000.50", params[AnalyticsParam.Amount])
        assertEquals("7.10", params[AnalyticsParam.InterestRate])
    }

    @Test
    fun planSuggestionParamsUseExactAmountAndTopResult() {
        val params = planSuggestedParams(
            investmentAmount = 25000.0,
            resultCount = 3,
            topScheme = SchemeType.KVP,
            topMaturityAmount = 50000.0
        )

        assertEquals(25000.0, params[AnalyticsParam.InvestmentAmount])
        assertEquals(3, params[AnalyticsParam.ResultCount])
        assertEquals(SchemeType.KVP.name, params[AnalyticsParam.TopScheme])
        assertEquals(50000.0, params[AnalyticsParam.TopMaturityAmount])
        assertFalse(params.containsKey("investment_amount_bucket"))
        assertFalse(params.containsKey("result_count_bucket"))
    }

    @Test
    fun agentSearchParamsUseExactPincode() {
        val params = agentSearchParams(
            pincode = "125055",
            resultCount = 2,
            errorMessage = null
        )

        assertEquals("125055", params[AnalyticsParam.Pincode])
        assertEquals(2, params[AnalyticsParam.ResultCount])
        assertFalse(params.containsKey("pincode_prefix"))
        assertFalse(params.containsKey("result_count_bucket"))
    }

    private fun calculatorInputFor(schemeType: SchemeType): CalculatorInput =
        CalculatorInput(
            schemeType = schemeType,
            amount = 10000.0,
            startDate = LocalDate.parse("2026-07-02"),
            ratePercent = 7.5,
            compoundingFrequency = CompoundingFrequency.QUARTERLY,
            tdTenure = TDTenure.TwoYears,
            customType = if (schemeType == SchemeType.COMPOUND_INTEREST) {
                CustomCalculatorType.Compound
            } else {
                CustomCalculatorType.Simple
            },
            customYears = 2.5,
            compoundFrequencyOption = CompoundFrequencyOption.Quarterly,
            installmentsPaid = 24,
            yearsCompleted = 2,
            toDate = LocalDate.parse("2027-07-02"),
            scssExtended = true
        )
}
