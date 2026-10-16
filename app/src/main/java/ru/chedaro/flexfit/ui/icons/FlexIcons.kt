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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Набор иконок приложения.
 *
 * Базовые иконки (Add, Delete, Settings, ArrowBack) берутся из стандартной
 * библиотеки `androidx.compose.material:material-icons-core`.
 *
 * Остальные (Таймер, Таппер, Ввод, Графики, Упражнения) описаны здесь как
 * ImageVector по path-данным Material Design Icons — это нативный механизм
 * Compose, без сторонних библиотек. Так приложение не зависит от того, какие
 * именно расширенные иконки остались в core-пакете в конкретной версии BOM.
 */
object FlexIcons {

    val ArrowBack: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val Add: ImageVector = Icons.Default.Add
    val Delete: ImageVector = Icons.Default.Delete
    val Settings: ImageVector = Icons.Default.Settings

    /** Круглые часы — Таймер. */
    val Timer: ImageVector by lazy {
        buildIcon("timer") {
            filled(
                "M12,2A10,10 0,0 0,2 12a10,10 0,0 0,10 10a10,10 0,0 0,10 -10A10,10 0,0 0,12 2M12,20A8,8 0,0 1,4 12A8,8 0,0 1,12 4a8,8 0,0 1,8 8a8,8 0,0 1,-8 8M12.5,7H11v6l4.75,2.85l0.75,-1.23l-4,-2.37V7Z"
            )
        }
    }

    /** Палец с тапом — Таппер. */
    val Tapper: ImageVector by lazy {
        buildIcon("tapper") {
            stroked("M4.5,4.5 L4.5,7 M3,5.5 L6,5.5")
            filled(
                "M9,11.5V5a1.5,1.5 0,0 1,3 0v5.5h1V4a1.5,1.5 0,0 1,3 0v6.5h1V6a1.5,1.5 0,0 1,3 0v8c0,4 -2.5,7 -6.5,7C11,21 9,19 7.5,16.5L4.2,11.9C3.7,11.1 4.3,10 5.3,10c0.5,0 0.9,0.2 1.2,0.7L9,13.5V11.5Z"
            )
        }
    }

    /** Цифра «1» над линейкой — ручной ввод числа. */
    val InputNumber: ImageVector by lazy {
        buildIcon("input_number") {
            stroked("M3,18 L21,18 M3,18 L3,15 M7,18 L7,16.5 M11,18 L11,15 M15,18 L15,16.5 M19,18 L19,15")
            filled("M11,5 h2 v8 h-2 Z M9.5,6.8 L11,5.2 v1.6 Z")
        }
    }

    /** Столбчатая диаграмма — Графики. */
    val Chart: ImageVector by lazy {
        buildIcon("chart") {
            stroked("M3,3 L3,20 L21,20")
            filled("M6.5,17h3v-4h-3v4Z M11.5,17h3V9h-3v8Z M16.5,17h3v-7h-3v7Z")
        }
    }

    /** Гантель — Упражнения. */
    val Dumbbell: ImageVector by lazy {
        buildIcon("dumbbell") {
            stroked("M4,9 L4,15 M7,6.5 L7,17.5 M17,6.5 L17,17.5 M20,9 L20,15 M7,12 L17,12")
        }
    }
}

private class IconSpecBuilder {
    val specs = mutableListOf<PathSpec>()

    fun filled(pathData: String) {
        specs += PathSpec(pathData.trim(), stroke = false)
    }

    fun stroked(pathData: String) {
        specs += PathSpec(pathData.trim(), stroke = true)
    }
}

private data class PathSpec(val pathData: String, val stroke: Boolean)

/**
 * Минимальная фабрика ImageVector.
 *
 * В текущем compose-BOM у `ImageVector.Builder.addPath` больше нет
 * overload'а со строкой `pathData` — есть только `addPath(List<PathNode>)`,
 * куда Kotlin не может подставить String (это и была ошибка компиляции).
 * Поэтому path-данные Material Design парсятся нативным
 * `androidx.compose.ui.graphics.vector.PathParser` в `List<PathNode>`
 * и передаются в nodes-перегрузку. Сторонних библиотек не используется.
 *
 * Заливка — NonZero, чтобы фигуры с «дырками» (циферблат часов)
 * рисовались корректно. Штриховые path'ы идут с прозрачной заливкой и
 * чёрным штрихом; итоговый цвет подменяется tint'ом на уровне `Icon()`.
 */
private fun buildIcon(name: String, builder: IconSpecBuilder.() -> Unit): ImageVector {
    val specs = IconSpecBuilder().apply(builder).specs
    val parser = PathParser()
    return ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        specs.forEach { spec ->
            addPath(
                path = parser.parseToPathNodes(spec.pathData),
                fill = if (spec.stroke) SolidColor(Color.Transparent) else SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = if (spec.stroke) SolidColor(Color.Black) else null,
                strokeAlpha = 1f,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 1f,
                fillRule = PathFillType.NonZero
            )
        }
    }.build()
}
