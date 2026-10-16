package ru.chedaro.flexfit.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.ui.graphics.vector.ImageVector
import ru.chedaro.flexfit.data.EntryType

/**
 * Набор иконок приложения — стандартные Material Icons из библиотеки
 * `androidx.compose.material:material-icons-extended` (официальный артефакт
 * Compose, сторонних зависимостей нет).
 */
object FlexIcons {

    val ArrowBack: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val Add: ImageVector = Icons.Filled.Add
    val Delete: ImageVector = Icons.Filled.Delete
    val Settings: ImageVector = Icons.Filled.Settings

    /** Круглые часы — Таймер. */
    val Timer: ImageVector = Icons.Filled.Timer

    /** Рука с пальцем — Таппер. */
    val Tapper: ImageVector = Icons.Filled.TouchApp

    /** Линия с нулём (стандартной иконки нет) — ручной ввод числа. */
    val InputNumber: ImageVector = Icons.Filled.Edit

    /** График — окно достижений/аналитики. */
    val Chart: ImageVector = Icons.Filled.ShowChart

    /** Человек — Упражнения. */
    val Dumbbell: ImageVector = Icons.Filled.Person

    /** Дополнительно: гантель для деталей упражнения. */
    val Fitness: ImageVector = Icons.Filled.FitnessCenter

    /** Иконка по типу сохранённой записи (для окна выбора записей). */
    fun forEntryType(type: EntryType): ImageVector = when (type) {
        EntryType.TIMER -> Timer
        EntryType.TAPS -> Tapper
    }
}
