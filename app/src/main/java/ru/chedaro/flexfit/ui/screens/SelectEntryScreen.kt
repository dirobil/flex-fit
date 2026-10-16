package ru.chedaro.flexfit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.chedaro.flexfit.data.EntryType
import ru.chedaro.flexfit.data.FlexRepository
import ru.chedaro.flexfit.data.TimerEntry
import ru.chedaro.flexfit.ui.icons.FlexIcons
import ru.chedaro.flexfit.ui.theme.FlexFitTheme

/**
 * Окно выбора сохранённых записей (интервалы таймера или числа нажатий).
 * Обычный клик по записи открывает выбор упражнения.
 * Режим удаления: кнопка «Удалить» сверху — красные крестики на карточках,
 * тап по крестику удаляет запись без подтверждения.
 */
@Composable
fun SelectEntryScreen(
    entryType: EntryType,
    onBack: () -> Unit,
    onSelect: (TimerEntry) -> Unit
) {
    val data by FlexRepository.data.collectAsState()
    val entries = data.savedTimerEntries.filter { it.type == entryType }
    val title = if (entryType == EntryType.TIMER) "Выбор времени" else "Выбор количества"

    var deleteMode by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    ScreenScaffold(title = title, onBack = onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Панель управления удалением
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { deleteMode = !deleteMode },
                    expanded = true,
                    containerColor = if (deleteMode)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (deleteMode)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    text = { Text(if (deleteMode) "Готово" else "Удалить") }
                )
                if (deleteMode && entries.isNotEmpty()) {
                    TextButton(onClick = { showClearAllDialog = true }) {
                        Text("Очистить все", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Нет сохранённых записей",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        EntryRow(
                            entry = entry,
                            deleteMode = deleteMode,
                            onClick = { onSelect(entry) },
                            onDelete = { FlexRepository.removeSavedEntry(entry.id) }
                        )
                    }
                }
            }
        }
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Очистить все записи?") },
            text = {
                Text(if (entryType == EntryType.TIMER)
                    "Будут удалены все сохранённые интервалы времени."
                else
                    "Будут удалены все сохранённые числа нажатий.")
            },
            confirmButton = {
                TextButton(onClick = {
                    if (entryType == EntryType.TIMER) FlexRepository.clearTimerEntries()
                    else FlexRepository.clearTapsEntries()
                    showClearAllDialog = false
                }) {
                    Text("Очистить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun EntryRow(
    entry: TimerEntry,
    deleteMode: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (deleteMode) {
                // Красный крестик — удаление по тапу
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить запись",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Text(
                text = entry.displayValue,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !deleteMode) { onClick() }
            )
            Text(
                text = entry.localDate.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(enabled = !deleteMode) { onClick() }
            )
            // Иконка-индикатор типа записи: часы для времени, палец для нажатий
            Icon(
                imageVector = FlexIcons.forEntryType(entry.type),
                contentDescription = null,
                tint = if (deleteMode)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyPlaceholder(padding: PaddingValues, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectEntryPreview() {
    FlexFitTheme {
        SelectEntryScreen(entryType = EntryType.TIMER, onBack = {}, onSelect = {})
    }
}
