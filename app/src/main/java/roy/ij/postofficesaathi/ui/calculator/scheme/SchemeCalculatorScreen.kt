package roy.ij.postofficesaathi.ui.calculator.scheme

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import roy.ij.postofficesaathi.analytics.SaathiAnalytics
import roy.ij.postofficesaathi.domain.calculator.CalculatorResult
import roy.ij.postofficesaathi.domain.calculator.CompoundFrequencyOption
import roy.ij.postofficesaathi.domain.calculator.CustomCalculatorType
import roy.ij.postofficesaathi.domain.calculator.SchemeType
import roy.ij.postofficesaathi.domain.calculator.TDTenure
import roy.ij.postofficesaathi.domain.calculator.formatCalculatorDate
import roy.ij.postofficesaathi.ui.components.PagePadding
import roy.ij.postofficesaathi.ui.components.SaathiCard
import roy.ij.postofficesaathi.ui.components.SaathiIconButton
import roy.ij.postofficesaathi.ui.components.SaathiPrimaryButton
import roy.ij.postofficesaathi.ui.components.SaathiScreen

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
        onCalculate = viewModel::calculate
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
    onCalculate: () -> Unit
) {
    SaathiScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PagePadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SchemeTopBar(state.title, onBack)
            if (state.schemeType == SchemeType.TD) {
                TdTenureSelector(state.tdTenure, onTdTenureChange)
            }
            if (state.schemeType == SchemeType.SIMPLE_INTEREST || state.schemeType == SchemeType.COMPOUND_INTEREST) {
                CustomTypeSelector(state.customType, onCustomTypeChange)
            }
            if (state.schemeType == SchemeType.MSSC) {
                WarningBanner("MSSC was discontinued on 30 Sept 2023. You can calculate returns for past investments.")
            }
            SaathiCard {
                MoneyField(
                    value = state.amount,
                    onValueChange = onAmountChange,
                    label = when (state.schemeType) {
                        SchemeType.RD -> "Monthly installment"
                        SchemeType.PPF, SchemeType.SSY -> "Annual deposit"
                        SchemeType.SB -> "Current balance"
                        else -> "Deposit amount"
                    },
                    error = state.errors["amount"]
                )
                DateField(
                    label = if (state.schemeType == SchemeType.SB) "From date" else "Start date",
                    date = state.startDate,
                    minDate = state.minDate,
                    onDateSelected = onStartDateChange,
                    error = state.errors["date"]
                )
                if (state.schemeType == SchemeType.SB) {
                    DateField(
                        label = "To date",
                        date = state.toDate,
                        minDate = state.startDate,
                        onDateSelected = onToDateChange
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
                onClick = onCalculate
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SchemeTopBar(title: String, onBack: () -> Unit) {
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
            Text(
                "Estimate returns with saved rate history",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                label = { Text(tenure.label) }
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
            label = { Text("Simple Interest") }
        )
        FilterChip(
            selected = selected == CustomCalculatorType.Compound,
            onClick = { onSelected(CustomCalculatorType.Compound) },
            label = { Text("Compound Interest") }
        )
    }
}

@Composable
private fun ChipRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun MoneyField(value: String, onValueChange: (String) -> Unit, label: String, error: String?) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        prefix = { Text("₹") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = error != null,
        supportingText = { error?.let { Text(it) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    date: LocalDate,
    minDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    error: String? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (error == null) 0.34f else 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
            DatePicker(state = pickerState)
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
    if (state.isRateOverridden) {
        OutlinedTextField(
            value = state.rateOverride,
            onValueChange = onRateOverrideChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Interest rate") },
            suffix = { Text("%") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEnableRateOverride() },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val rate = state.officialRate
                Text(
                    text = if (rate == null) "Loading official rate..." else "Rate: ${rate.ratePercent}% p.a. (${rate.compoundingFrequency.label})",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (rate == null) {
                        "Tap to enter a custom rate."
                    } else {
                        "Effective from ${formatCalculatorDate(rate.effectiveFrom)}. Tap to override."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    when (state.schemeType) {
        SchemeType.RD -> {
            OutlinedTextField(
                value = state.installmentsPaid,
                onValueChange = onInstallmentsPaidChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Installments paid so far") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.errors["installments"] != null,
                supportingText = { Text(state.errors["installments"] ?: "Total installments: 60 months") }
            )
        }
        SchemeType.PPF, SchemeType.SSY -> {
            OutlinedTextField(
                value = state.yearsCompleted,
                onValueChange = onYearsCompletedChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Years completed so far") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                            label = { Text(option.label) }
                        )
                    }
                }
            }
        }
        else -> Unit
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
