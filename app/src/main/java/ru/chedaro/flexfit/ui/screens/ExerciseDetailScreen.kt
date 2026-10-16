package ru.chedaro.flexfit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import ru.chedaro.flexfit.data.FlexRepository
import ru.chedaro.flexfit.ui.theme.FlexFitTheme
import java.time.LocalDate

/**
 * Информация об упражнении: редактирование названия и шага, история подходов.
 */
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onBack: () -> Unit
) {
    val data by FlexRepository.data.collectAsState()
    val exercise = data.exercises.firstOrNull { it.id == exerciseId }

    ScreenScaffold(title = exercise?.name ?: "Упражнение", onBack = onBack) { padding ->
        if (exercise == null) {
            EmptyPlaceholder(padding, "Упражнение не найдено")
            return@ScreenScaffold
        }

        // Режим удаления записей истории (как в окне выбора записей).
        var deleteMode by remember { mutableStateOf(false) }
        var showClearAllDialog by remember { mutableStateOf(false) }

        val history = data.logs
            .filter { it.exerciseId == exercise.id }
            .sortedByDescending { it.dateEpochDay }

        // Контейнер занимает всю область Scaffold и принимает padding,
        // иначе контент уходит за границы экрана.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            var nameText by remember(exercise.id) { mutableStateOf(exercise.name) }
            var stepText by remember(exercise.id) { mutableStateOf(exercise.stepPerTap.toString()) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            label = { Text("Название") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = stepText,
                            onValueChange = { raw -> stepText = raw.filter { it.isDigit() }.take(4) },
                            label = { Text("Шаг прироста за тап") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                FlexRepository.renameExercise(exercise.id, nameText)
                                FlexRepository.setStepPerTap(
                                    exercise.id,
                                    stepText.toIntOrNull() ?: exercise.stepPerTap
                                )
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Сохранить") }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "История (${history.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (history.isNotEmpty()) {
                        TextButton(onClick = { deleteMode = !deleteMode }) {
                            Text(if (deleteMode) "Готово" else "Удалить")
                        }
                    }
                }
            }

            items(history, key = { it.id }) { log ->
                val timeText = if (log.type == EntryType.TIMER) {
                    "%02d:%02d".format(log.value / 60, log.value % 60)
                } else log.value.toString()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .then(
                            if (deleteMode) {
                                Modifier.clickable { FlexRepository.deleteLog(log.id) }
                            } else {
                                Modifier
                            }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (deleteMode) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (deleteMode) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить запись",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = LocalDate.ofEpochDay(log.dateEpochDay).toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (deleteMode && history.isNotEmpty()) {
                item {
                    Button(
                        onClick = { showClearAllDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) { Text("Очистить всю историю") }
                }
            }
            } // конец LazyColumn
        } // конец Box

        if (showClearAllDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllDialog = false },
                title = { Text("Очистить историю?") },
                text = { Text("Будут удалены все записи упражнения «${exercise.name}». Это действие необратимо.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            history.forEach { FlexRepository.deleteLog(it.id) }
                            showClearAllDialog = false
                            deleteMode = false
                        }
                    ) { Text("Удалить всё") }
                },
                dismissButton = {
                    TextButton(onClick = { showClearAllDialog = false }) { Text("Отмена") }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseDetailPreview() {
    FlexFitTheme {
        ExerciseDetailScreen(exerciseId = "none", onBack = {})
    }
}
