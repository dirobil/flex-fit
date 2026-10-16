package ru.chedaro.flexfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import ru.chedaro.flexfit.ui.icons.FlexIcons
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.chedaro.flexfit.ui.navigation.Routes
import ru.chedaro.flexfit.ui.theme.FlexFitTheme

/**
 * Стартовое окно: сетка из 5 крупных квадратных кнопок (3 сверху, 2 снизу),
 * между группами — полупрозрачный тонкий разделитель.
 * Кнопки всегда квадратные и целиком умещаются на экране.
 */
@Composable
fun StarterScreen(onNavigate: (String) -> Unit) {
    // Ищем доступную область: ширина или высота диктует размер плитки,
    // чтобы 5 квадратов + зазоры + разделитель гарантированно влезали в экран.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val gap = 14.dp
        val dividerSpace = 9.dp // 1.dp толщина + вертикальные отступы
        val byWidth = (maxWidth - gap * 2) / 3f
        val byHeight = (maxHeight - gap * 4 - dividerSpace) / 5f
        val tile = minOf(byWidth, byHeight)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(gap, Alignment.CenterVertically)
        ) {
            StarterTile(FlexIcons.Timer, "Таймер", tile) { onNavigate(Routes.TIMER) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
            ) {
                StarterTile(FlexIcons.Tapper, "Таппер", tile) { onNavigate(Routes.TAPPER) }
                StarterTile(FlexIcons.InputNumber, "Ввод", tile) { onNavigate(Routes.INPUT) }
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
                horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
            ) {
                StarterTile(FlexIcons.Chart, "Графики", tile) { onNavigate(Routes.GRAPHS) }
                StarterTile(FlexIcons.Dumbbell, "Упражнения", tile) { onNavigate(Routes.EXERCISES) }
            }
        }
    }
}

/** Квадратная плитка с иконкой и подписью; клик по всей поверхности. */
@Composable
private fun StarterTile(
    icon: ImageVector,
    label: String,
    size: Dp,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(if (size >= 120.dp) 56.dp else 40.dp),
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

@Preview(showBackground = true)
@Composable
private fun StarterPreview() {
    FlexFitTheme {
        StarterScreen(onNavigate = {})
    }
}
