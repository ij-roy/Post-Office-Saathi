package roy.ij.postofficesaathi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import roy.ij.postofficesaathi.ui.PostOfficeSaathiApp
import roy.ij.postofficesaathi.ui.theme.PostOfficeSaathiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PostOfficeSaathiTheme {
                PostOfficeSaathiApp()
            }
        }
    }
}
