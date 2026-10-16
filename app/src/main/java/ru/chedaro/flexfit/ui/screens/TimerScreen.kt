package ru.chedaro.flexfit.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.chedaro.flexfit.data.FlexRepository
import ru.chedaro.flexfit.ui.theme.FlexFitTheme

/** Яркие «популярные» цвета; меняются после каждого полного оборота полосы (1 минута). */
private val StripColors = listOf(
    Color(0xFFFF4532), // red
    Color(0xFF28973A), // green
    Color(0xFF009DE6), // blue
    Color(0xFFFFC632), // yellow
    Color(0xFFFF6B00), // orange
    Color(0xFFAF52DE), // purple
    Color(0xFFEC4899)  // pink
)

/**
 * Окно таймера: круговой дисплей «00:00:00», запуск/остановка по нажатию,
 * бегущая полоса (полный оборот за минуту), кнопки «Выбрать» и «Сброс/Очистить».
 */
@Composable
fun TimerScreen(
    onBack: () -> Unit,
    onOpenSelect: () -> Unit
) {
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var colorIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // Смена яркого цвета после каждого полного оборота
    LaunchedEffect(elapsedSeconds) {
        if (elapsedSeconds > 0 && elapsedSeconds % 60 == 0) {
            colorIndex = (colorIndex + 1) % StripColors.size
        }
    }

    ScreenScaffold(title = "Таймер", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically)
        ) {
            TimerDial(
                seconds = elapsedSeconds,
                running = isRunning,
                stripColor = StripColors[colorIndex],
                onClick = {
                    if (isRunning) {
                        // Остановили: сохранить интервал и обнулить значения
                        isRunning = false
                        FlexRepository.saveTimerSeconds(elapsedSeconds)
                        elapsedSeconds = 0
                        colorIndex = 0
                    } else {
                        isRunning = true
                    }
                }
            )

            TwoButtonsRow(
                firstLabel = "Выбрать",
                secondLabel = "Сброс/Очистить",
                onFirst = {
                    if (!isRunning && elapsedSeconds > 0) {
                        FlexRepository.saveTimerSeconds(elapsedSeconds)
                        elapsedSeconds = 0
                    }
                    onOpenSelect()
                },
                onSecond = {
                    isRunning = false
                    elapsedSeconds = 0
                    colorIndex = 0
                    FlexRepository.clearSavedEntries()
                }
            )
        }
    }
}

/**
 * Круглый циферблат: фоновое кольцо + цветная полоса, доля которой равна
 * секунде текущего минуты. Плавность между секундами даёт animateFloatAsState.
 */
@Composable
private fun TimerDial(
    seconds: Int,
    running: Boolean,
    stripColor: Color,
    onClick: () -> Unit
) {
    val targetFraction = if (running) (seconds % 60) / 60f else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 950),
        label = "sweep"
    )
    // Значения темы читаем ДО Canvas: внутри DrawScope (@Composable вызовы недоступны)
    val dialBackground = MaterialTheme.colorScheme.surfaceVariant
    val dialTrack = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .size(280.dp)
            .background(dialBackground, CircleShape)
            .pointerInput(Unit) { detectTapGestures { onClick() } },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14.dp.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val trackColor = dialTrack

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = stroke)
            )

            val sweepDegrees = animatedFraction * 360f
            if (sweepDegrees > 0.5f) {
                drawArc(
                    color = stripColor,
                    startAngle = -90f,
                    sweepAngle = sweepDegrees,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = stroke)
                )
            }
        }

        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        Text(
            text = "%02d:%02d:%02d".format(h, m, s),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerPreview() {
    FlexFitTheme {
        TimerScreen(onBack = {}, onOpenSelect = {})
    }
}
