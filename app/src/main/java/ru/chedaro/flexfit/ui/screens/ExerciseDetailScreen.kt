package ru.chedaro.flexfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

        var nameText by remember(exercise.id) { mutableStateOf(exercise.name) }
        var stepText by remember(exercise.id) { mutableStateOf(exercise.stepPerTap.toString()) }

        val history = data.logs
            .filter { it.exerciseId == exercise.id }
            .sortedByDescending { it.dateEpochDay }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
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
                Text(
                    text = "История (${history.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(history, key = { it.id }) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (log.type == EntryType.TIMER) {
                            "%02d:%02d".format(log.value / 60, log.value % 60)
                        } else log.value.toString(),
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
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseDetailPreview() {
    FlexFitTheme {
        ExerciseDetailScreen(exerciseId = "none", onBack = {})
    }
}
