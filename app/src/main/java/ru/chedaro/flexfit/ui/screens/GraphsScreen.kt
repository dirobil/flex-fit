package ru.chedaro.flexfit.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.chedaro.flexfit.data.FlexRepository
import ru.chedaro.flexfit.ui.theme.FlexFitTheme
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.max

/**
 * Окно графиков:
 * 1) «Год» — тепловая карта активности в стиле GitHub (зелёный/серый);
 * 2) «Упражнение» — столбцы повторений по дням + сравнение с прошлым месяцем.
 */
@Composable
fun GraphsScreen(onBack: () -> Unit) {
    val data by FlexRepository.data.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    ScreenScaffold(title = "Графики", onBack = onBack) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Активность за год") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("По упражнению") }
                )
            }

            when (selectedTab) {
                0 -> YearHeatmap(logs = data.logs, modifier = Modifier.fillMaxSize())
                else -> ExerciseBarChart(
                    exercises = data.exercises,
                    logs = data.logs,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ---------- Идея 1: тепловая карта года (GitHub-style) ----------

private fun activityLevel(count: Int): Int = when {
    count <= 0 -> 0
    count < 3 -> 1
    count < 7 -> 2
    count < 15 -> 3
    else -> 4
}

@Composable
private fun YearHeatmap(logs: List<ru.chedaro.flexfit.data.LogEntry>, modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    val year = today.year
    val counts: Map<Long, Int> = remember(logs) {
        logs.groupingBy { it.dateEpochDay }.eachCount()
    }

    // Все дни года, выровненные по неделям (столбцы — недели, строки — дни недели)
    val days = remember(year) {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        generateSequence(start) { d -> if (d.isBefore(end)) d.plusDays(1) else null }.toList()
    }

    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
    val activeColor = Color(0xFF28973A) // systemGreen

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Достигнуто в $year году",
            style = MaterialTheme.typography.titleMedium
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val cell = 14.dp.toPx()
            val gap = 3.dp.toPx()
            val offsetDay = days.first().dayOfWeek.value % 7 // сдвиг первого дня (вс=0)
            days.forEachIndexed { index, date ->
                val slot = index + offsetDay
                val col = slot / 7
                val row = slot % 7
                val level = activityLevel(counts[date.toEpochDay()] ?: 0)
                val color = if (level == 0) inactiveColor.copy(alpha = 0.4f)
                else activeColor.copy(alpha = 0.25f + 0.18f * level)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(col * (cell + gap), row * (cell + gap)),
                    size = Size(cell, cell),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Меньше", style = MaterialTheme.typography.bodySmall)
            GapSpacer()
            for (level in 0..4) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(
                            if (level == 0) inactiveColor.copy(alpha = 0.4f)
                            else activeColor.copy(alpha = 0.25f + 0.18f * level),
                            RoundedCornerShape(3.dp)
                        )
                )
                GapSpacer()
            }
            Text("Больше", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun GapSpacer() {
    Spacer(modifier = Modifier.size(4.dp, 1.dp))
}

// ---------- Идея 2: повторения/время по дням + сравнение с прошлым месяцем ----------

/** Что показывать на графике: количество повторений или затраченное время. */
private enum class Metric(val label: String) {
    REPS("Повторения"),
    TIME("Время")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseBarChart(
    exercises: List<ru.chedaro.flexfit.data.Exercise>,
    logs: List<ru.chedaro.flexfit.data.LogEntry>,
    modifier: Modifier = Modifier
) {
    if (exercises.isEmpty()) {
        EmptyPlaceholder(PaddingValues(), "Нет упражнений для анализа")
        return
    }

    var expanded by remember { mutableStateOf(false) }
    var metricExpanded by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf(exercises.first().id) }
    var metric by remember { mutableStateOf(Metric.REPS) }
    val selected = exercises.firstOrNull { it.id == selectedId } ?: exercises.first()

    val currentMonth = YearMonth.now()
    val previousMonth = currentMonth.minusMonths(1)

    // Значение записи в выбранной метрике: секунды таймера не учитываются в повторениях,
    // а нажатия/ввод — во времени.
    fun metricValue(log: ru.chedaro.flexfit.data.LogEntry): Int = when (metric) {
        Metric.REPS -> if (log.type == EntryType.TAPS) log.value else 0
        Metric.TIME -> if (log.type == EntryType.TIMER) log.value else 0
    }

    val perDay: Map<Long, Int> = remember(logs, selected.id, metric) {
        logs.filter { it.exerciseId == selected.id }
            .groupBy { it.dateEpochDay }
            .mapValues { (_, list) -> list.sumOf { metricValue(it) } }
    }

    val currentTotal = perDay.filter { (day, _) ->
        YearMonth.from(LocalDate.ofEpochDay(day)) == currentMonth
    }.values.sum()

    val previousTotal = perDay.filter { (day, _) ->
        YearMonth.from(LocalDate.ofEpochDay(day)) == previousMonth
    }.values.sum()

    val trendText = when {
        previousTotal == 0 && currentTotal == 0 -> "Нет данных за эти месяцы"
        previousTotal == 0 -> "Новый рекорд! В прошлом месяце не было занятий"
        currentTotal >= previousTotal ->
            "Больше, чем в ${previousMonth.monthValue} мес.: +${formatMetric(metric, currentTotal - previousTotal)} (+${percent(currentTotal, previousTotal)}%)"
        else ->
            "Меньше, чем в ${previousMonth.monthValue} мес.: −${formatMetric(metric, previousTotal - currentTotal)}"
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selected.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Упражнение") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                exercises.forEach { ex ->
                    DropdownMenuItem(
                        text = { Text(ex.name) },
                        onClick = {
                            selectedId = ex.id
                            expanded = false
                        }
                    )
                }
            }
        }

        // Выбор метрики: повторения (таппер/ввод) или затраченное время (таймер).
        ExposedDropdownMenuBox(expanded = metricExpanded, onExpandedChange = { metricExpanded = it }) {
            OutlinedTextField(
                value = metric.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Метрика") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(metricExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = metricExpanded,
                onDismissRequest = { metricExpanded = false }
            ) {
                Metric.entries.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m.label) },
                        onClick = {
                            metric = m
                            metricExpanded = false
                        }
                    )
                }
            }
        }

        Text(
            text = "${metric.label} по дням (${currentMonth.monthValue}.${currentMonth.year})",
            style = MaterialTheme.typography.titleMedium
        )

        BarChart(
            values = (1..currentMonth.lengthOfMonth()).map { day ->
                perDay[currentMonth.atDay(day).toEpochDay()] ?: 0
            },
            barColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .horizontalScroll(rememberScrollState())
        )

        Text(text = trendText, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "Текущий месяц: ${formatMetric(metric, currentTotal)} · " +
                    "Прошлый месяц: ${formatMetric(metric, previousTotal)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Форматирование значения под выбранную метрику: секунды — как mm:ss. */
private fun formatMetric(metric: Metric, value: Int): String =
    if (metric == Metric.TIME) "%02d:%02d".format(value / 60, value % 60) else value.toString()

private fun percent(current: Int, previous: Int): Int =
    if (previous == 0) 0 else ((current - previous) * 100) / previous

/** Простой столбчатый график без сторонних библиотек. */
@Composable
private fun BarChart(values: List<Int>, barColor: Color, modifier: Modifier = Modifier) {
    val maxValue = remember(values) { max(1, values.maxOrNull() ?: 1) }
    Canvas(modifier = modifier) {
        val n = values.size
        if (n == 0) return@Canvas
        val slot = size.width / n
        val barWidth = slot * 0.7f
        values.forEachIndexed { i, v ->
            val h = if (v == 0) 2.dp.toPx() else (v.toFloat() / maxValue) * size.height
            drawRoundRect(
                color = if (v == 0) Color.Gray.copy(alpha = 0.3f) else barColor,
                topLeft = Offset(i * slot + (slot - barWidth) / 2f, size.height - h),
                size = Size(barWidth.coerceAtLeast(2.dp.toPx()), h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GraphsPreview() {
    FlexFitTheme {
        GraphsScreen(onBack = {})
    }
}
