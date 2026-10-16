package ru.chedaro.flexfit.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.chedaro.flexfit.data.EntryType
import ru.chedaro.flexfit.data.FlexRepository
import ru.chedaro.flexfit.data.TimerEntry
import ru.chedaro.flexfit.ui.theme.FlexFitTheme

/**
 * Окно выбора упражнения для сохранённой записи (время/количество).
 * Клик по упражнению записывает значение в историю упражнения.
 */
@Composable
fun SelectExerciseScreen(
    entry: TimerEntry,
    onBack: () -> Unit,
    onPicked: (String) -> Unit
) {
    val data by FlexRepository.data.collectAsState()
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    ScreenScaffold(title = "Выбор упражнения", onBack = onBack) { padding ->
        if (data.exercises.isEmpty()) {
            EmptyPlaceholder(padding, "Сначала добавьте упражнения")
        } else {
            ExerciseListView(
                exercises = data.exercises,
                asCards = data.settings.showAsCards,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                onClick = { exercise -> onPicked(exercise.id) },
                onLongPress = { exercise -> pendingDelete = exercise.id }
            )
        }
    }

    DeleteExerciseDialog(
        exerciseId = pendingDelete,
        onDismiss = { pendingDelete = null },
        onConfirm = { id ->
            FlexRepository.deleteExercise(id)
            pendingDelete = null
        }
    )
}

/** Диалог подтверждения удаления упражнения (используется на нескольких экранах). */
@Composable
fun DeleteExerciseDialog(
    exerciseId: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (exerciseId == null) return
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить упражнение?") },
        text = { Text("Упражнение и его история будут удалены.") },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { onConfirm(exerciseId) }) {
                Text("Удалить", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectExercisePreview() {
    FlexFitTheme {
        SelectExerciseScreen(
            entry = TimerEntry("x", EntryType.TIMER, seconds = 45),
            onBack = {},
            onPicked = {}
        )
    }
}
