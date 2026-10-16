package ru.chedaro.flexfit.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.chedaro.flexfit.data.FlexRepository
import ru.chedaro.flexfit.ui.navigation.Routes
import ru.chedaro.flexfit.ui.theme.FlexFitTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row

/**
 * Окно упражнений: список/карточки (настройка в правом верхнем углу),
 * удаление долгим удержанием, клик открывает информацию об упражнении,
 * круглая кнопка «плюс» внизу справа добавляет новое упражнение.
 */
@Composable
fun ExercisesScreen(
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit = {}
) {
    val data by FlexRepository.data.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    ScreenScaffold(
        title = "Упражнения",
        onBack = onBack,
        actions = {
            IconButton(onClick = { showSettings = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Настройки")
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (data.exercises.isEmpty()) {
                Text(
                    text = "Пока пусто.\nНажмите «+», чтобы добавить упражнение.",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                ExerciseListView(
                    exercises = data.exercises,
                    asCards = data.settings.showAsCards,
                    subtitleFor = { ex -> "+${ex.stepPerTap}/тап" },
                    onClick = { exercise -> onOpenDetail(exercise.id) },
                    onLongPress = { exercise -> pendingDelete = exercise.id }
                )
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить упражнение")
            }
        }
    }

    // Настройки: список или карточки
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Настройки отображения") },
            text = {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Карточки", modifier = Modifier.weight(1f))
                    Switch(
                        checked = data.settings.showAsCards,
                        onCheckedChange = { FlexRepository.setShowAsCards(it) }
                    )
                    Text("Список", modifier = Modifier.padding(start = 8.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) { Text("Готово") }
            }
        )
    }

    // Добавление упражнения
    if (showAddDialog) {
        AddExerciseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name ->
                FlexRepository.addExercise(name)
                showAddDialog = false
            }
        )
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

@Composable
private fun AddExerciseDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое упражнение") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onAdd(name) }) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ExercisesPreview() {
    FlexFitTheme {
        ExercisesScreen(onBack = {})
    }
}
