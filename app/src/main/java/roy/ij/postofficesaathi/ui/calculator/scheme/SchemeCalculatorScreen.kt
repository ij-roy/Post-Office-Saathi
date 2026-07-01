package roy.ij.postofficesaathi.ui.calculator.scheme

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.ln
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.domain.calculator.CalculatorResult
import roy.ij.postofficesaathi.domain.calculator.CompoundFrequencyOption
import roy.ij.postofficesaathi.domain.calculator.CustomCalculatorType
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.TDTenure
import roy.ij.postofficesaathi.domain.calculator.formatCalculatorDate
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiIconButton
import roy.ij.postofficesaathi.ui.components.SaathiPrimaryButton
import roy.ij.postofficesaathi.ui.components.SaathiScreen

data class SchemeCalculatorPresentation(
    val title: String,
    val subtitle: String?,
    val amountLabel: String,
    val amountPlaceholder: String?,
    val fromDateLabel: String,
    val toDateLabel: String,
    val showDateRange: Boolean,
    val openingDateDialogTitle: String,
    val installmentsLabel: String
) {
    companion object {
        fun forScheme(schemeType: SchemeType): SchemeCalculatorPresentation =
            when (schemeType) {
                SchemeType.RD -> SchemeCalculatorPresentation(
                    title = "RD Calculator",
                    subtitle = null,
                    amountLabel = "Monthly Amount",
                    amountPlaceholder = "Enter monthly amount (e.g. 5000)",
                    fromDateLabel = "From",
                    toDateLabel = "To",
                    showDateRange = true,
                    openingDateDialogTitle = "Select opening date",
                    installmentsLabel = "Installments"
                )
                SchemeType.TD,
                SchemeType.MIS,
                SchemeType.NSC,
                SchemeType.KVP,
                SchemeType.SCSS,
                SchemeType.MSSC -> base(
                    schemeType = schemeType,
                    amountLabel = "Deposit Amount",
                    amountPlaceholder = "Enter investment amount (e.g. 100000)"
                )
                SchemeType.PPF, SchemeType.SSY -> base(
                    schemeType = schemeType,
                    amountLabel = "Yearly Deposit",
                    amountPlaceholder = "Enter yearly deposit (e.g. 50000)"
                )
                SchemeType.SB -> base(
                    schemeType = schemeType,
                    amountLabel = "Balance Amount",
                    amountPlaceholder = "Enter balance amount (e.g. 25000)"
                )
                SchemeType.SIMPLE_INTEREST,
                SchemeType.COMPOUND_INTEREST -> SchemeCalculatorPresentation(
                    title = schemeType.displayName,
                    subtitle = null,
                    amountLabel = "Principal Amount",
                    amountPlaceholder = "Enter principal amount (e.g. 100000)",
                    fromDateLabel = "From",
                    toDateLabel = "To",
                    showDateRange = false,
                    openingDateDialogTitle = "Select opening date",
                    installmentsLabel = "Installments"
                )
                SchemeType.RD_REBATE,
                SchemeType.PMI -> base(
                    schemeType = schemeType,
                    amountLabel = "Deposit Amount",
                    amountPlaceholder = "Enter investment amount (e.g. 100000)"
                )
            }

        private fun base(
            schemeType: SchemeType,
            amountLabel: String,
            amountPlaceholder: String?,
            fromDateLabel: String = "From",
            toDateLabel: String = "To",
            showDateRange: Boolean = true
        ): SchemeCalculatorPresentation =
            SchemeCalculatorPresentation(
                title = schemeType.displayName,
                subtitle = null,
                amountLabel = amountLabel,
                amountPlaceholder = amountPlaceholder,
                fromDateLabel = fromDateLabel,
                toDateLabel = toDateLabel,
                showDateRange = showDateRange,
                openingDateDialogTitle = "Select opening date",
                installmentsLabel = "Installments"
            )
    }
}

@Composable
fun SchemeCalculatorRoute(
    analytics: SaathiAnalytics,
    schemeType: SchemeType,
    initialAmount: Double?,
    onBack: () -> Unit,
    onResult: (CalculatorResult) -> Unit
) {
    val context = LocalContext.current
    val factory = remember(context, schemeType, initialAmount, analytics) {
        SchemeCalculatorViewModel.Factory(context, schemeType, initialAmount, analytics)
    }
    val viewModel: SchemeCalculatorViewModel = viewModel(
        key = "scheme-${schemeType.name}-${initialAmount ?: 0.0}",
        factory = factory
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.externalActions.collect { action ->
            when (action) {
                is SchemeCalculatorExternalAction.ShowResult -> onResult(action.result)
            }
        }
    }
    LaunchedEffect(state.messageId) {
        state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    SchemeCalculatorScreen(
        state = state,
        onBack = onBack,
        onAmountChange = viewModel::updateAmount,
        onStartDateChange = viewModel::updateStartDate,
        onToDateChange = viewModel::updateToDate,
        onTdTenureChange = viewModel::updateTdTenure,
        onCustomTypeChange = viewModel::updateCustomType,
        onEnableRateOverride = viewModel::enableRateOverride,
        onRateOverrideChange = viewModel::updateRateOverride,
        onResetRateOverride = viewModel::resetRateOverride,
        onInstallmentsPaidChange = viewModel::updateInstallmentsPaid,
        onYearsCompletedChange = viewModel::updateYearsCompleted,
        onCustomYearsChange = viewModel::updateCustomYears,
        onCompoundFrequencyChange = viewModel::updateCompoundFrequency,
        onScssExtendedChange = viewModel::updateScssExtended,
        onCalculate = viewModel::calculate,
        onAnimationComplete = viewModel::onCalculationAnimationComplete
    )
}

@Composable
fun SchemeCalculatorScreen(
    state: SchemeCalculatorUiState,
    onBack: () -> Unit,
    onAmountChange: (String) -> Unit,
    onStartDateChange: (LocalDate) -> Unit,
    onToDateChange: (LocalDate) -> Unit,
    onTdTenureChange: (TDTenure) -> Unit,
    onCustomTypeChange: (CustomCalculatorType) -> Unit,
    onEnableRateOverride: () -> Unit,
    onRateOverrideChange: (String) -> Unit,
    onResetRateOverride: () -> Unit,
    onInstallmentsPaidChange: (String) -> Unit,
    onYearsCompletedChange: (String) -> Unit,
    onCustomYearsChange: (String) -> Unit,
    onCompoundFrequencyChange: (CompoundFrequencyOption) -> Unit,
    onScssExtendedChange: (Boolean) -> Unit,
    onCalculate: () -> Unit,
    onAnimationComplete: () -> Unit
) {
    val presentation = SchemeCalculatorPresentation.forScheme(state.schemeType)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    SaathiScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus(force = true)
                }
                .padding(PagePadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SchemeTopBar(presentation.title, presentation.subtitle, onBack)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.schemeType == SchemeType.TD) {
                        TdTenureSelector(state.tdTenure, onTdTenureChange)
                    }
                    if (state.schemeType == SchemeType.SIMPLE_INTEREST || state.schemeType == SchemeType.COMPOUND_INTEREST) {
                        CustomTypeSelector(state.customType, onCustomTypeChange)
                    }
                    if (state.schemeType == SchemeType.MSSC) {
                        WarningBanner("MSSC was discontinued on 30 Sept 2023. You can calculate returns for past investments.")
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MoneyField(
                            value = state.amount,
                            onValueChange = onAmountChange,
                            label = presentation.amountLabel,
                            placeholder = presentation.amountPlaceholder,
                            error = state.errors["amount"]
                        )
                        if (presentation.showDateRange) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DateField(
                                    label = presentation.fromDateLabel,
                                    date = state.startDate,
                                    minDate = state.minDate,
                                    onDateSelected = onStartDateChange,
                                    error = state.errors["date"],
                                    dialogTitle = presentation.openingDateDialogTitle,
                                    modifier = Modifier.weight(1f)
                                )
                                val isToDateEditable = state.schemeType == SchemeType.RD || state.schemeType == SchemeType.SB
                                DateField(
                                    label = presentation.toDateLabel,
                                    date = calculatorDisplayToDate(state),
                                    minDate = state.startDate,
                                    onDateSelected = onToDateChange,
                                    dialogTitle = "Select finish date",
                                    enabled = isToDateEditable,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            DateField(
                                label = presentation.fromDateLabel,
                                date = state.startDate,
                                minDate = state.minDate,
                                onDateSelected = onStartDateChange,
                                error = state.errors["date"],
                                dialogTitle = presentation.openingDateDialogTitle
                            )
                        }
                        RateSection(
                            state = state,
                            onEnableRateOverride = onEnableRateOverride,
                            onRateOverrideChange = onRateOverrideChange,
                            onResetRateOverride = onResetRateOverride
                        )
                        SchemeSpecificFields(
                            state = state,
                            onInstallmentsPaidChange = onInstallmentsPaidChange,
                            onYearsCompletedChange = onYearsCompletedChange,
                            onCustomYearsChange = onCustomYearsChange,
                            onCompoundFrequencyChange = onCompoundFrequencyChange,
                            onScssExtendedChange = onScssExtendedChange
                        )
                    }
                    SaathiPrimaryButton(
                        text = if (state.isCalculating) "Calculating..." else "Calculate",
                        enabled = !state.isCalculating && !state.isLoading,
                        onClick = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            onCalculate()
                        }
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
        if (state.isCalculating && state.pendingResult != null) {
            CalculatingAnimationOverlay(onAnimationComplete = onAnimationComplete)
        }
    }
}

internal fun calculatorDisplayToDate(state: SchemeCalculatorUiState): LocalDate =
    when (state.schemeType) {
        SchemeType.RD -> state.startDate.plusMonths((state.installmentsPaid.toLongOrNull() ?: 60L).coerceIn(0L, 60L))
        SchemeType.TD -> state.startDate.plusYears(state.tdTenure.years.toLong())
        SchemeType.MIS, SchemeType.NSC -> state.startDate.plusYears(5)
        SchemeType.KVP -> {
            val rate = (state.activeRatePercent ?: state.officialRate?.ratePercent ?: 0.0) / 100.0
            if (rate <= 0.0) {
                state.startDate
            } else {
                val months = (ln(2.0) / ln(1.0 + rate) * 12.0).toLong().coerceAtLeast(1)
                state.startDate.plusMonths(months)
            }
        }
        SchemeType.PPF -> state.startDate.plusYears(15)
        SchemeType.SSY -> state.startDate.plusYears(21)
        SchemeType.SCSS -> state.startDate.plusYears(if (state.scssExtended) 8 else 5)
        SchemeType.SB -> state.toDate
        SchemeType.MSSC -> state.startDate.plusYears(2)
        SchemeType.SIMPLE_INTEREST,
        SchemeType.COMPOUND_INTEREST -> state.startDate.plusMonths(((state.customYears.toDoubleOrNull() ?: 1.0) * 12).toLong())
        SchemeType.RD_REBATE,
        SchemeType.PMI -> state.toDate
    }

@Composable
private fun SchemeTopBar(title: String, subtitle: String?, onBack: () -> Unit) {
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
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineLarge)
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TdTenureSelector(selected: TDTenure, onSelected: (TDTenure) -> Unit) {
    ChipRow {
        TDTenure.entries.forEach { tenure ->
            FilterChip(
                selected = selected == tenure,
                onClick = { onSelected(tenure) },
                modifier = Modifier.widthIn(min = 92.dp),
                label = { ChipText(tenure.label) }
            )
        }
    }
}

@Composable
private fun CustomTypeSelector(selected: CustomCalculatorType, onSelected: (CustomCalculatorType) -> Unit) {
    ChipRow {
        FilterChip(
            selected = selected == CustomCalculatorType.Simple,
            onClick = { onSelected(CustomCalculatorType.Simple) },
            modifier = Modifier.widthIn(min = 150.dp),
            label = { ChipText("Simple Interest") }
        )
        FilterChip(
            selected = selected == CustomCalculatorType.Compound,
            onClick = { onSelected(CustomCalculatorType.Compound) },
            modifier = Modifier.widthIn(min = 170.dp),
            label = { ChipText("Compound Interest") }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun ChipText(text: String) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun MoneyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String?,
    error: String?
) {
    val focusManager = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { placeholder?.let { Text(it) } },
            prefix = { Text("₹") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(force = true) }),
            isError = error != null,
            supportingText = { error?.let { Text(it) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    date: LocalDate,
    minDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    error: String? = null,
    dialogTitle: String = "Select date",
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { showDialog = true },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (error == null) 0.34f else 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatCalculatorDate(date), style = MaterialTheme.typography.titleMedium)
                error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
    if (showDialog) {
        val zoneId = ZoneId.systemDefault()
        val minMillis = minDate?.atStartOfDay(zoneId)?.toInstant()?.toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli(),
            yearRange = (minDate?.year ?: 1900)..(LocalDate.now().year + 80),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    minMillis == null || utcTimeMillis >= minMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let {
                            onDateSelected(Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate())
                        }
                        showDialog = false
                    }
                ) { Text("Done") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        ) {
            DatePicker(
                state = pickerState,
                title = {
                    Text(
                        dialogTitle,
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun RateSection(
    state: SchemeCalculatorUiState,
    onEnableRateOverride: () -> Unit,
    onRateOverrideChange: (String) -> Unit,
    onResetRateOverride: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    if (state.isRateOverridden) {
        OutlinedTextField(
            value = state.rateOverride,
            onValueChange = onRateOverrideChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Interest rate") },
            suffix = { Text("%") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(force = true) }),
            trailingIcon = {
                if (state.officialRate != null) {
                    IconButton(onClick = onResetRateOverride) {
                        Icon(Icons.Filled.Restore, contentDescription = "Use official rate")
                    }
                }
            },
            supportingText = {
                Text(
                    if (state.officialRate == null) {
                        "Using custom rate"
                    } else {
                        "Using custom rate. Official rate: ${state.officialRate.ratePercent}% p.a."
                    }
                )
            },
            isError = state.errors["rate"] != null
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEnableRateOverride() }
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val rate = state.officialRate
            Text(
                text = if (rate == null) "Loading rate" else "Rate: ${rate.ratePercent}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            rate?.let {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = it.compoundingFrequency.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SchemeSpecificFields(
    state: SchemeCalculatorUiState,
    onInstallmentsPaidChange: (String) -> Unit,
    onYearsCompletedChange: (String) -> Unit,
    onCustomYearsChange: (String) -> Unit,
    onCompoundFrequencyChange: (CompoundFrequencyOption) -> Unit,
    onScssExtendedChange: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    when (state.schemeType) {
        SchemeType.RD -> {
            InstallmentsField(
                value = state.installmentsPaid,
                onValueChange = onInstallmentsPaidChange,
                error = state.errors["installments"]
            )
        }
        SchemeType.PPF, SchemeType.SSY -> {
            OutlinedTextField(
                value = state.yearsCompleted,
                onValueChange = onYearsCompletedChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Years completed so far") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(force = true) })
            )
        }
        SchemeType.SCSS -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Extension", style = MaterialTheme.typography.titleMedium)
                    Text("Add 3 years beyond 5 years", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.scssExtended, onCheckedChange = onScssExtendedChange)
            }
        }
        SchemeType.SIMPLE_INTEREST, SchemeType.COMPOUND_INTEREST -> {
            OutlinedTextField(
                value = state.customYears,
                onValueChange = onCustomYearsChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Time in years") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(force = true) }),
                isError = state.errors["years"] != null,
                supportingText = { state.errors["years"]?.let { Text(it) } }
            )
            if (state.customType == CustomCalculatorType.Compound) {
                Text("Compounding", style = MaterialTheme.typography.titleSmall)
                ChipRow {
                    CompoundFrequencyOption.entries.forEach { option ->
                        FilterChip(
                            selected = state.compoundFrequencyOption == option,
                            onClick = { onCompoundFrequencyChange(option) },
                            modifier = Modifier.widthIn(min = 108.dp),
                            label = { ChipText(option.label) }
                        )
                    }
                }
            }
        }
        else -> Unit
    }
}

@Composable
private fun InstallmentsField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?
) {
    val focusManager = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Installments :",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            BasicTextField(
                value = value.ifBlank { "60" },
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(force = true) })
            )
        }
        error?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun WarningBanner(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
        contentColor = MaterialTheme.colorScheme.error,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.24f))
    ) {
        Text(text, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CalculatingAnimationOverlay(
    onAnimationComplete: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("calculating.json"))
    val animationState = animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    LaunchedEffect(animationState.isAtEnd, animationState.progress) {
        if (animationState.isAtEnd && animationState.progress == 1f) {
            onAnimationComplete()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Full screen glassmorphic layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = true, onClick = {}) // Block clicks underneath
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.94f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LottieAnimation(
                composition = composition,
                progress = { animationState.progress },
                modifier = Modifier.size(260.dp)
            )
            Text(
                text = "Calculating...",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.graphicsLayer { alpha = textAlpha }
            )
        }
    }
}
