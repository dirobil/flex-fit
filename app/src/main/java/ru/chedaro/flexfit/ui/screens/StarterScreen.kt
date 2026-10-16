package ru.chedaro.flexfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Addchart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.chedaro.flexfit.ui.navigation.Routes
import ru.chedaro.flexfit.ui.theme.FlexFitTheme

/**
 * Стартовое окно: 3 крупных квадратных кнопки, полупрозрачный разделитель, 2 кнопки.
 */
@Composable
fun StarterScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StarterTile(Icons.Default.AccessTime, "Таймер", Modifier.weight(1f)) {
            onNavigate(Routes.TIMER)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StarterTile(Icons.Default.TouchApp, "Таппер", Modifier.weight(1f)) {
                onNavigate(Routes.TAPPER)
            }
            StarterTile(Icons.Default.Calculate, "Ввод", Modifier.weight(1f)) {
                onNavigate(Routes.INPUT)
            }
        }

        // Полупрозрачный тонкий разделитель
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StarterTile(Icons.Default.Addchart, "Графики", Modifier.weight(1f)) {
                onNavigate(Routes.GRAPHS)
            }
            StarterTile(Icons.Default.FitnessCenter, "Упражнения", Modifier.weight(1f)) {
                onNavigate(Routes.EXERCISES)
            }
        }
    }
}

/** Квадратная плитка с иконкой и подписью. weight управляет высотой, aspectRatio — шириной. */
@Composable
private fun StarterTile(
    icon: ImageVector,
    label: String,
    weightModifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = weightModifier
            .fillMaxWidth()
            .aspectRatioSquare(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Плитка остаётся квадратной и умещается в экран: ограничиваем размер по ширине/высоте. */
private fun Modifier.aspectRatioSquare(): Modifier =
    this.then(Modifier.sizeIn(maxHeight = 420.dp))

@Preview(showBackground = true)
@Composable
private fun StarterPreview() {
    FlexFitTheme {
        StarterScreen(onNavigate = {})
    }
}
