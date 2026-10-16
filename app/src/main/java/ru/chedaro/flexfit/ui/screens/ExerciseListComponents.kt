package ru.chedaro.flexfit.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ru.chedaro.flexfit.data.Exercise

/**
 * Единый список/сетка карточек упражнений (настройка «карточки или список»
 * применяется и здесь, и в окнах выбора).
 * Долгое удержание открывает подтверждение удаления.
 */
@Composable
fun ExerciseListView(
    exercises: List<Exercise>,
    asCards: Boolean,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    subtitleFor: (Exercise) -> String? = { null },
    onClick: (Exercise) -> Unit,
    onLongPress: (Exercise) -> Unit
) {
    if (asCards) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exercises, key = { it.id }) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    subtitle = subtitleFor(exercise),
                    onClick = { onClick(exercise) },
                    onLongPress = { onLongPress(exercise) }
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exercises, key = { it.id }) { exercise ->
                ExerciseListItem(
                    exercise = exercise,
                    subtitle = subtitleFor(exercise),
                    onClick = { onClick(exercise) },
                    onLongPress = { onLongPress(exercise) }
                )
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    subtitle: String?,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(exercise.id) {
                detectTapGestures(onTap = { onClick() }, onLongPress = { onLongPress() })
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ExerciseListItem(
    exercise: Exercise,
    subtitle: String?,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(exercise.id) {
                detectTapGestures(onTap = { onClick() }, onLongPress = { onLongPress() })
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
