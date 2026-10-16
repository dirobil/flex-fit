package ru.chedaro.flexfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.chedaro.flexfit.data.FlexRepository
import ru.chedaro.flexfit.ui.theme.FlexFitTheme

/**
 * Окно «Таппер»: счётчик нажатий в центре экрана.
 * Тап по экрану увеличивает число на шаг упражнения; минимум — ноль.
 */
@Composable
fun TapperScreen(
    onBack: () -> Unit,
    onOpenSelectEntries: () -> Unit,
    onOpenSelectExercise: (String) -> Unit = { onOpenSelectEntries() }
) {
    val data by FlexRepository.data.collectAsState()
    // Шаг прироста за тап хранится в настройках (единый для окна Таппер)
    val step = data.settings.tapperStep.coerceAtLeast(1)

    var taps by remember { mutableIntStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }

    ScreenScaffold(title = "Таппер", onBack = onBack) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(step) {
                    detectTapGestures { taps = (taps + step).coerceAtLeast(0) }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
            ) {
                Text(
                    text = taps.coerceAtLeast(0).toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "+$step за нажатие",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TwoButtonsRow(
                    firstLabel = "Выбрать",
                    secondLabel = "Изменить",
                    onFirst = {
                        // Сохраняем текущее число и открываем список записей
                        // (оттуда — выбор упражнения). Единый путь для Таппера и Ввода.
                        val entryId = FlexRepository.saveTaps(taps)
                        if (entryId != null) taps = 0
                        onOpenSelectEntries()
                    },
                    onSecond = { showEditDialog = true }
                )
            }
        }
    }

    if (showEditDialog) {
        EditTapperDialog(
            currentTaps = taps,
            stepPerTap = step,
            onDismiss = { showEditDialog = false },
            onApply = { newTaps, newStep ->
                taps = newTaps.coerceAtLeast(0)
                FlexRepository.setTapperStep(newStep)
                showEditDialog = false
            }
        )
    }
}

/** Диалог изменения числа нажатий и шага прироста. */
@Composable
private fun EditTapperDialog(
    currentTaps: Int,
    stepPerTap: Int,
    onDismiss: () -> Unit,
    onApply: (Int, Int) -> Unit
) {
    var tapsText by remember { mutableStateOf(currentTaps.toString()) }
    var stepText by remember { mutableStateOf(stepPerTap.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = tapsText,
                    onValueChange = { raw -> tapsText = raw.filter { it.isDigit() } },
                    label = { Text("Количество нажатий") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = stepText,
                    onValueChange = { raw -> stepText = raw.filter { it.isDigit() } },
                    label = { Text("Шаг прироста за тап") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val t = tapsText.toIntOrNull() ?: currentTaps
                val s = stepText.toIntOrNull()?.coerceAtLeast(1) ?: stepPerTap
                onApply(t, s)
            }) { Text("Применить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun TapperPreview() {
    FlexFitTheme {
        TapperScreen(onBack = {}, onOpenSelectEntries = {})
    }
}
