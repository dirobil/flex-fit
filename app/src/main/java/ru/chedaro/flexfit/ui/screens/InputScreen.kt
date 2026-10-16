package ru.chedaro.flexfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.chedaro.flexfit.data.EntryType
import ru.chedaro.flexfit.ui.navigation.Routes
import ru.chedaro.flexfit.ui.theme.FlexFitTheme

/**
 * Окно ручного ввода числа (количество повторений).
 * Число открывается для редактирования по нажатию, клавиатура — только цифры,
 * значение не может быть меньше нуля. Кнопка «Выбрать» записывает число для упражнения.
 */
@Composable
fun InputScreen(
    onBack: () -> Unit,
    onNumberConfirmed: (Int) -> Unit
) {
    var text by remember { mutableStateOf("0") }

    ScreenScaffold(title = "Ввод", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically)
        ) {
            // Кликабельное текстовое поле с числом; допускаются только цифры (>= 0)
            OutlinedTextField(
                value = text,
                onValueChange = { raw ->
                    val filtered = raw.filter { it.isDigit() }.trimStart().ifEmpty { "0" }
                    // Ограничиваем длину, чтобы избежать переполнения Int
                    text = filtered.take(9)
                },
                label = { Text("Количество") },
                textStyle = MaterialTheme.typography.displayMedium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val value = text.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    if (value > 0) onNumberConfirmed(value)
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Выбрать")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InputPreview() {
    FlexFitTheme {
        InputScreen(onBack = {}, onNumberConfirmed = {})
    }
}
