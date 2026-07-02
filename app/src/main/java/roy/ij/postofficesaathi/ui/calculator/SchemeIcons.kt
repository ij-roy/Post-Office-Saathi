package roy.ij.postofficesaathi.ui.calculator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Woman
import androidx.compose.ui.graphics.vector.ImageVector
import roy.ij.postofficesaathi.domain.calculator.SchemeType

internal fun SchemeType.schemeIcon(): ImageVector =
    when (this) {
        SchemeType.RD -> Icons.Filled.Autorenew
        SchemeType.TD -> Icons.Filled.HourglassTop
        SchemeType.MIS -> Icons.Filled.Payments
        SchemeType.NSC -> Icons.Filled.Verified
        SchemeType.KVP -> Icons.Filled.Eco
        SchemeType.PPF -> Icons.Filled.Savings
        SchemeType.SSY -> Icons.Filled.ChildCare
        SchemeType.SCSS -> Icons.Filled.Elderly
        SchemeType.SB -> Icons.Filled.AccountBalance
        SchemeType.SIMPLE_INTEREST,
        SchemeType.COMPOUND_INTEREST -> Icons.Filled.Tune
        SchemeType.MSSC -> Icons.Filled.Woman
        SchemeType.RD_REBATE,
        SchemeType.PMI -> Icons.Filled.Tune
    }
