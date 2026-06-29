package roy.ij.postofficesaathi.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import roy.ij.postofficesaathi.data.preferences.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = NightRed,
    secondary = NightBlue,
    tertiary = NightAmber,
    background = NightBackground,
    surface = NightSurface,
    surfaceVariant = NightSurfaceContainer,
    primaryContainer = Color(0xFF531415),
    secondaryContainer = Color(0xFF13385F),
    tertiaryContainer = Color(0xFF4D3909),
    onPrimary = Color(0xFF2B0505),
    onSecondary = Color(0xFF061827),
    onBackground = NightText,
    onSurface = NightText,
    onSurfaceVariant = NightTextVariant,
    outline = NightOutline,
    outlineVariant = NightOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = PostalRed,
    secondary = PostalAmber,
    tertiary = PostalAmber,
    background = WarmBackground,
    surface = WarmSurface,
    surfaceVariant = WarmSurfaceContainer,
    primaryContainer = PostalRedContainer,
    secondaryContainer = PostalBlueContainer,
    tertiaryContainer = Color(0xFFFFE7B0),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = PostalText,
    onSurface = PostalText,
    onSurfaceVariant = PostalTextVariant,
    outline = PostalOutline,
    outlineVariant = PostalOutlineVariant

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun PostOfficeSaathiTheme(
    themeMode: ThemeMode = ThemeMode.System,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
