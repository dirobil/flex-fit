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
            // Внешний ободок + «ушки» сверху, рисуемые штрихом (как у оригинала MDI).
            stroked("M12,4 a8,8 0,1 0,0.001,0 Z M9,2 L15,2 M12,4 L12,2")
            // Стрелки: часовая и минутная.
            stroked("M12,12 L12,7.5 M12,12 L15.5,14")
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
 * Используется единственный стабильный overload API Compose —
 * `ImageVector.Builder.addPath(...) { ... }` с DSL-лямбдой `PathBuilder`,
 * где команды SVG/Material path транслируются в `moveTo/lineTo/cubicTo/
 * quadraticBezierTo/close`. Строковые и nodes-перегрузки `addPath` в
 * compose-BOM 2026.09.00 компилируются с ошибкой, поэтому не используются.
 * Сторонних библиотек нет.
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
            addPath(
                fill = if (spec.stroke) SolidColor(Color.Transparent) else SolidColor(Color.Black),
                stroke = if (spec.stroke) SolidColor(Color.Black) else null,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fillRule = PathFillType.NonZero
            ) {
                buildMaterialPath(spec.pathData)
            }
        }
    }.build()
}

/** Трансляция SVG/Material path-данных в команды [androidx.compose.ui.graphics.PathBuilder]. */
private fun androidx.compose.ui.graphics.PathBuilder.buildMaterialPath(pathData: String) {
    val tokens = parsePathTokens(pathData)
    var i = 0
    var currentCmd = 'M'
    var curX = 0f
    var curY = 0f   // текущая точка
    var lastCtrlX = 0f
    var lastCtrlY = 0f // последняя контрольная точка (для отражений S/T)
    var lastWasCurve = false
    var lastWasQuad = false
    var startX = 0f
    var startY = 0f // начало текущего подсегмента (для Z)

    fun next(): Float = tokens[i++].toFloat()

    while (i < tokens.size) {
        val t = tokens[i]
        if (t.length == 1 && t[0].isLetter()) {
            currentCmd = t[0]
            i++
            continue
        }
        val relative = currentCmd.isLowerCase()
        when (currentCmd.uppercaseChar()) {
            'A', 'a' -> {
                // Дуга SVG (rx ry rot largeArc sweep x y). В наших данных — только
                // окружности/эллипсы; аппроксимируем кубическими Безье по четвертям.
                val rx = next(); val ry = next()
                next() // rotation — не поддерживаем (в иконках не используется)
                val largeArc = next() != 0f
                val sweepPos = next() != 0f
                var x = next(); var y = next()
                if (relative) { x += curX; y += curY }
                arcToCubicApprox(x, y, rx, ry, largeArc, sweepPos)
                lastWasCurve = false; lastWasQuad = false
            }
            'M' -> {
                var x = next(); var y = next()
                if (relative) { x += curX; y += curY }
                moveTo(x, y)
                curX = x; curY = y; startX = x; startY = y
                lastWasCurve = false; lastWasQuad = false
                currentCmd = if (relative) 'l' else 'L' // повторы после M трактуем как линии
            }
            'L' -> {
                var x = next(); var y = next()
                if (relative) { x += curX; y += curY }
                lineTo(x, y)
                curX = x; curY = y
                lastWasCurve = false; lastWasQuad = false
            }
            'H' -> {
                var x = next()
                if (relative) x += curX
                lineTo(x, curY)
                curX = x
                lastWasCurve = false; lastWasQuad = false
            }
            'V' -> {
                var y = next()
                if (relative) y += curY
                lineTo(curX, y)
                curY = y
                lastWasCurve = false; lastWasQuad = false
            }
            'C' -> {
                var x1 = next(); var y1 = next()
                var x2 = next(); var y2 = next()
                var x = next();  var y = next()
                if (relative) {
                    x1 += curX; y1 += curY; x2 += curX; y2 += curY; x += curX; y += curY
                }
                cubicTo(x1, y1, x2, y2, x, y)
                lastCtrlX = x2; lastCtrlY = y2
                curX = x; curY = y
                lastWasCurve = true; lastWasQuad = false
            }
            'S' -> {
                // Первая контрольная точка — отражение последней контрольной точки предыдущей C/S.
                var x1 = if (lastWasCurve) 2 * curX - lastCtrlX else curX
                var y1 = if (lastWasCurve) 2 * curY - lastCtrlY else curY
                var x2 = next(); var y2 = next()
                var x = next();  var y = next()
                if (relative) { x2 += curX; y2 += curY; x += curX; y += curY }
                cubicTo(x1, y1, x2, y2, x, y)
                lastCtrlX = x2; lastCtrlY = y2
                curX = x; curY = y
                lastWasCurve = true; lastWasQuad = false
            }
            'Q' -> {
                var x1 = next(); var y1 = next()
                var x = next();  var y = next()
                if (relative) { x1 += curX; y1 += curY; x += curX; y += curY }
                quadraticBezierTo(x1, y1, x, y)
                lastCtrlX = x1; lastCtrlY = y1
                curX = x; curY = y
                lastWasQuad = true; lastWasCurve = false
            }
            'T' -> {
                // Отражённая контрольная точка предыдущего Q/T.
                val x1 = if (lastWasQuad) 2 * curX - lastCtrlX else curX
                val y1 = if (lastWasQuad) 2 * curY - lastCtrlY else curY
                var x = next(); var y = next()
                if (relative) { x += curX; y += curY }
                quadraticBezierTo(x1, y1, x, y)
                lastCtrlX = x1; lastCtrlY = y1
                curX = x; curY = y
                lastWasQuad = true; lastWasCurve = false
            }
            'Z' -> {
                close()
                curX = startX; curY = startY
                lastWasCurve = false; lastWasQuad = false
            }
            else -> { /* неизвестную команду пропускаем */ }
        }
    }
}

/** Лексемизация path-строк: буквы команд и числа (включая «-», «+», десятичные точки). */
private fun parsePathTokens(pathData: String): List<String> {
    val out = ArrayList<String>()
    val sb = StringBuilder()
    for (ch in pathData) {
        when {
            ch.isLetter() -> {
                if (sb.isNotEmpty()) { out += sb.toString(); sb.setLength(0) }
                // Буквы A/E внутри чисел не встречаются в наших данных; команды выносим отдельно.
                out += ch.toString()
            }
            ch == ',' || ch.isWhitespace() -> {
                if (sb.isNotEmpty()) { out += sb.toString(); sb.setLength(0) }
            }
            ch == '-' || ch == '+' -> {
                // Знак начинает новое число, если уже накоплено число.
                if (sb.isNotEmpty()) { out += sb.toString(); sb.setLength(0) }
                sb.append(ch)
            }
            ch == '.' -> {
                // Вторая точка («5.5.5» в SVG = два числа) — начинаем новое число.
                if (sb.contains('.')) { out += sb.toString(); sb.setLength(0) }
                sb.append(ch)
            }
            else -> sb.append(ch) // цифры
        }
    }
    if (sb.isNotEmpty()) out += sb.toString()
    return out
}

/**
 * Аппроксимация дуги SVG эллипса (`A`/`a`) кубическими кривыми Безье
 * (стандартный метод 4 контрольных точек на четверть окружности).
 * Обновляет текущую точку PathBuilder'а (`curX/curY`).
 */
private fun androidx.compose.ui.graphics.PathBuilder.arcToCubicApprox(
    endX: Float,
    endY: Float,
    rxIn: Float,
    ryIn: Float,
    largeArc: Boolean,
    sweepPos: Boolean
) {
    if (curX == endX && curY == endY) return
    var rx = rxIn
    var ry = ryIn
    if (rx == 0f || ry == 0f) {
        lineTo(endX, endY)
        curX = endX; curY = endY
        return
    }
    rx = kotlin.math.abs(rx); ry = kotlin.math.abs(ry)

    val dx = (curX - endX) / 2f
    val dy = (curY - endY) / 2f
    var lambda = (dx * dx) / (rx * rx) + (dy * dy) / (ry * ry)
    if (lambda > 1f) {
        val s = kotlin.math.sqrt(lambda)
        rx *= s; ry *= s
    }

    val sign = if (largeArc != sweepPos) 1f else -1f
    val num = rx * rx * ry * ry - rx * rx * dy * dy - ry * ry * dx * dx
    val den = rx * rx * dy * dy + ry * ry * dx * dx
    val co = sign * kotlin.math.sqrt((if (num < 0f) 0f else num) / den)
    val cxp = co * (rx * dy / ry)
    val cyp = co * (-(ry * dx / rx))
    val cx = (curX + endX) / 2f + cxp
    val cy = (curY + endY) / 2f + cyp

    fun angle(ux: Float, uy: Float, vx: Float, vy: Float): Float {
        val dot = ux * vx + uy * vy
        val len = kotlin.math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy))
        var a = kotlin.math.acos((dot / len).coerceIn(-1f, 1f))
        if (ux * vy - uy * vx < 0f) a = -a
        return a
    }

    val theta1 = angle(1f, 0f, (dx - cxp) / rx, (dy - cyp) / ry)
    var deltaTheta = angle(
        (dx - cxp) / rx, (dy - cyp) / ry,
        (-dx - cxp) / rx, (-dy - cyp) / ry
    )
    if (!sweepPos && deltaTheta > 0f) deltaTheta -= 2f * Math.PI.toFloat()
    if (sweepPos && deltaTheta < 0f) deltaTheta += 2f * Math.PI.toFloat()

    val segments = (kotlin.math.ceil(kotlin.math.abs(deltaTheta) / (Math.PI.toFloat() / 2f)))
        .toInt().coerceAtLeast(1)
    val segAngle = deltaTheta / segments
    // k = 4/3*tan(Δ/4) — вес контрольных точек для кубической аппроксимации дуги
    val halfTan = kotlin.math.tan(segAngle / 2f)
    val alpha = (4f / 3f) * halfTan
    var t = theta1
    repeat(segments) {
        val t2 = t + segAngle
        val cosT = kotlin.math.cos(t); val sinT = kotlin.math.sin(t)
        val cosT2 = kotlin.math.cos(t2); val sinT2 = kotlin.math.sin(t2)
        // концы сегмента на эллипсе
        val x1 = cx + rx * cosT; val y1 = cy + ry * sinT
        val x2 = cx + rx * cosT2; val y2 = cy + ry * sinT2
        // производные (касательные)
        val d1x = -rx * sinT; val d1y = ry * cosT
        val d2x = -rx * sinT2; val d2y = ry * cosT2
        cubicTo(x1 + alpha * d1x, y1 + alpha * d1y, x2 - alpha * d2x, y2 - alpha * d2y, x2, y2)
        t = t2
    }
    curX = endX; curY = endY
}
