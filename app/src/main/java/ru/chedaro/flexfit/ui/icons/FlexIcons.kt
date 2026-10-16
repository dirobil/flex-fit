package ru.chedaro.flexfit.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.unit.dp

/**
 * Набор иконок приложения.
 *
 * Базовые иконки (Add, Delete, Settings, ArrowBack) берутся из стандартной
 * библиотеки `androidx.compose.material:material-icons-core`.
 *
 * Остальные (Таймер, Таппер, Ввод, Графики, Упражнения) описаны здесь как
 * ImageVector по path-данным в стиле Material Design Icons — это нативный
 * механизм Compose, без сторонних библиотек. Так приложение не зависит от
 * того, какие именно расширенные иконки остались в core-пакете в конкретной
 * версии BOM.
 */
object FlexIcons {

    val ArrowBack: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val Add: ImageVector = Icons.Default.Add
    val Delete: ImageVector = Icons.Default.Delete
    val Settings: ImageVector = Icons.Default.Settings

    /** Круглые часы со стрелками — Таймер. */
    val Timer: ImageVector by lazy {
        buildIcon("timer") {
            stroked(
                // Ободок: полная окружность r=8 с центром (12,12) двумя дугами SVG.
                "M12,4 A8,8 0 1 0 12,20 A8,8 0 1 0 12,4 Z",
                "M9,2 L15,2",                   // «ушки» сверху
                "M12,7.5 L12,12 L15.5,14"       // часовая и минутная стрелки
            )
        }
    }

    /** Палец с тапом — Таппер. */
    val Tapper: ImageVector by lazy {
        buildIcon("tapper") {
            stroked("M4.5,4.5 L4.5,7 M3,5.5 L6,5.5") // знак «плюс» — вспышка тапа
            filled(
                // Палец с полукруглыми подушечками (дуги SVG).
                "M9,11.5 L9,5 A1.5,1.5 0 0 1 12,5 L12,10.5 L13,10.5 L13,4 " +
                    "A1.5,1.5 0 0 1 16,4 L16,10.5 L17,10.5 L17,6 " +
                    "A1.5,1.5 0 0 1 20,6 L20,14 C20,18 17.5,21 13.5,21 " +
                    "C11,21 9,19 7.5,16.5 L4.2,11.9 C3.7,11.1 4.3,10 5.3,10 " +
                    "C5.8,10 6.2,10.2 6.5,10.7 L9,13.5 Z"
            )
        }
    }

    /** Цифра «1» над линейкой — ручной ввод числа. */
    val InputNumber: ImageVector by lazy {
        buildIcon("input_number") {
            stroked(
                "M3,18 L21,18", // базовая линия линейки
                "M3,18 L3,15 M7,18 L7,16.5 M11,18 L11,15 M15,18 L15,16.5 M19,18 L19,15", // деления
                "M12,4 L10,6.5 M12,4 L12,12" // цифра «1» с засечкой
            )
            filled()
        }
    }

    /** Столбчатая диаграмма — Графики. */
    val Chart: ImageVector by lazy {
        buildIcon("chart") {
            stroked("M3,3 L3,20 L21,20") // оси
            filled(
                "M6.5,17 L9.5,17 L9.5,13 L6.5,13 Z",
                "M11.5,17 L14.5,17 L14.5,9 L11.5,9 Z",
                "M16.5,17 L19.5,17 L19.5,11 L16.5,11 Z"
            )
        }
    }

    /** Гантель — Упражнения. */
    val Dumbbell: ImageVector by lazy {
        buildIcon("dumbbell") {
            stroked(
                "M4,9 L4,15 M7,6.5 L7,17.5 M17,6.5 L17,17.5 M20,9 L20,15", // грифы блинов
                "M7,12 L17,12" // перекладина
            )
            filled()
        }
    }
}

/**
 * Фабрика ImageVector на единственном overload'е API Compose:
 * `ImageVector.Builder.addPath(pathData: List<PathNode>, ...)` — он существует
 * во всех версиях compose-ui. Строки path-данных конвертируются в список
 * [PathNode] нативным [PathParser] (`androidx.compose.ui.graphics.vector`,
 * public API), без сторонних библиотек.
 *
 * Штриховые path'ы идут с прозрачной заливкой и штрихом; итоговый цвет
 * подменяется tint'ом на уровне `Icon()`.
 */
private fun buildIcon(name: String, builder: IconSpecBuilder.() -> Unit): ImageVector {
    val specs = IconSpecBuilder().apply(builder).specs
    return ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        specs.forEach { spec ->
            // PathParser — публичный API ui-graphics с момента появления ImageVector DSL;
            // конвертирует SVG path-строку в List<PathNode>.
            val parser = PathParser()
            spec.pathDataList.forEach { data -> parser.parsePathString(data) }
            val nodes = parser.toNodes().let { ArrayList(it) }
            addPath(
                pathData = nodes,
                pathFillType = PathFillType.NonZero,
                fill = SolidColor(if (spec.stroke) Color.Transparent else Color.Black),
                stroke = if (spec.stroke) SolidColor(Color.Black) else null,
                strokeLineWidth = if (spec.stroke) 2f else 0f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
        }
    }.build()
}
private class IconSpecBuilder {
    val specs = mutableListOf<PathSpec>()

    fun filled(vararg pathData: String) {
        specs += PathSpec(pathData.toList(), stroke = false)
    }

    fun stroked(vararg pathData: String) {
        specs += PathSpec(pathData.toList(), stroke = true)
    }
}

private data class PathSpec(val pathDataList: List<String>, val stroke: Boolean)


