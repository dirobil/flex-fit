package ru.chedaro.flexfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.chedaro.flexfit.data.FlexRepository
import ru.chedaro.flexfit.ui.navigation.FlexNavHost
import ru.chedaro.flexfit.ui.theme.FlexFitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FlexRepository.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            FlexFitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FlexNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    FlexFitTheme {
        FlexNavHost()
    }
}