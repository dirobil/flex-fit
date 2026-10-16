package ru.chedaro.flexfit.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.chedaro.flexfit.data.EntryType
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
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
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
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
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
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Выбор упражнения: read-only поле + выпадающий список (стабильный DropdownMenu).
        // Клик обрабатывает Box с clickable: в этой версии Material3 у
        // OutlinedTextField нет параметра onClick.
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selected.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Упражнение") },
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать упражнение")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = metric.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Метрика") },
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать метрику")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { metricExpanded = true }
            )
            DropdownMenu(
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

        // Метка оси Y — максимальное значение метрики на графике.
        Text(
            text = "Максимум: ${formatMetric(metric, perDay.values.maxOrNull() ?: 0)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Горизонтальная прокрутка: содержимое шире экрана — график можно
        // двигать пальцем; если умещается — центрируется от левого края.
        val chartValues = (1..currentMonth.lengthOfMonth()).map { day ->
            perDay[currentMonth.atDay(day).toEpochDay()] ?: 0
        }
        val minSlotPx = with(LocalDensity.current) { 26.dp.toPx() }
        val contentWidthDp = with(LocalDensity.current) {
            ((44.dp.toPx() + chartValues.size * minSlotPx + 8.dp.toPx()).toDp())
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .width(if (contentWidthDp > 300.dp) contentWidthDp else 300.dp)
                    .fillMaxHeight()
            ) {
                BarChart(
                    values = chartValues,
                    barColor = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    valueFormatter = { v -> formatMetric(metric, v) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

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

/**
 * Столбчатый график без сторонних библиотек:
 * ось Y с делениями и подписями значений, ось X с подписями дней (каждый 3-й день),
 * горизонтальные линии сетки, значения над ненулевыми столбцами.
 */
@Composable
private fun BarChart(
    values: List<Int>,
    barColor: Color,
    labelColor: Color,
    gridColor: Color,
    valueFormatter: (Int) -> String,
    modifier: Modifier = Modifier
) {
    val maxValue = remember(values) { max(1, values.maxOrNull() ?: 1) }
    // Минимальная ширина одного «слота» под столбец — чтобы при прокрутии
    // подписи дней не слипались на длинных месяцах.
    val minSlotWidth = 26.dp

    Canvas(modifier = modifier) {
        val n = values.size
        if (n == 0) return@Canvas

        val labelSizePx = 10.sp.toPx()
        val leftGutter = 44.dp.toPx()   // место под подписи оси Y
        val bottomGutter = 18.dp.toPx() // место под подписи оси X
        val topPad = 14.dp.toPx()       // место под значения над столбцами

        val chartWidth = size.width - leftGutter
        val slot = max(minSlotWidth.toPx(), chartWidth / n)
        val chartHeight = size.height - bottomGutter - topPad
        val baselineY = topPad + chartHeight

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (labelColor.alpha * 255).toInt(),
                (labelColor.red * 255).toInt(),
                (labelColor.green * 255).toInt(),
                (labelColor.blue * 255).toInt()
            )
            textSize = labelSizePx
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        val valuePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (barColor.alpha * 255).toInt(),
                (barColor.red * 255).toInt(),
                (barColor.green * 255).toInt(),
                (barColor.blue * 255).toInt()
            )
            textSize = 9.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }

        // --- Горизонтальная сетка + подписи оси Y (0, 25%, 50%, 75%, 100% от максимума)
        for (step in 0..4) {
            val fraction = step / 4f
            val y = baselineY - fraction * chartHeight
            drawLine(
                color = if (step == 0) gridColor.copy(alpha = 1f) else gridColor,
                start = Offset(leftGutter, y),
                end = Offset(size.width, y),
                strokeWidth = if (step == 0) 2f else 1f
            )
            val labelValue = (maxValue * fraction).toInt()
            drawContext.canvas.nativeCanvas.drawText(
                valueFormatter(labelValue),
                leftGutter - 6.dp.toPx(),
                y + labelSizePx / 3f,
                paint.apply { textAlign = android.graphics.Paint.Align.RIGHT }
            )
            paint.textAlign = android.graphics.Paint.Align.CENTER
        }

        // --- Ось Y (вертикальная линия)
        drawLine(
            color = gridColor.copy(alpha = 1f),
            start = Offset(leftGutter, topPad),
            end = Offset(leftGutter, baselineY),
            strokeWidth = 2f
        )

        // --- Столбцы и подписи оси X
        val barWidth = (slot * 0.6f).coerceAtLeast(4.dp.toPx())
        values.forEachIndexed { i, v ->
            val cx = leftGutter + i * slot + slot / 2f
            if (v > 0) {
                val h = (v.toFloat() / maxValue) * chartHeight
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(cx - barWidth / 2f, baselineY - h),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                // Значение над столбцом (только если он достаточно широкий)
                if (slot >= 22.dp.toPx()) {
                    drawContext.canvas.nativeCanvas.drawText(
                        valueFormatter(v),
                        cx,
                        baselineY - h - 3.dp.toPx(),
                        valuePaint
                    )
                }
            } else {
                // Пустой день — маленькая серая засечка на базовой линии
                drawRoundRect(
                    color = Color.Gray.copy(alpha = 0.25f),
                    topLeft = Offset(cx - barWidth / 2f, baselineY - 2.dp.toPx()),
                    size = Size(barWidth, 2.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
            // Подпись дня: каждый 3-й день или первый/последний, если слоты узкие
            val day = i + 1
            val showLabel = slot >= 18.dp.toPx() && (day % 3 == 1 || day == n)
            if (showLabel) {
                drawContext.canvas.nativeCanvas.drawText(
                    day.toString(),
                    cx,
                    baselineY + labelSizePx + 4.dp.toPx(),
                    paint
                )
            }
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
