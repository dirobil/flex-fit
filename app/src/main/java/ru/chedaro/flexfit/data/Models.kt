package ru.chedaro.flexfit.data

import java.io.Serializable
import java.time.LocalDate

/**
 * Единая точка входа для сериализуемых сущностей приложения.
 * Все модели — простые data class, совместимые со стандартным JSON Android (org.json).
 */

/** Тип записи: сохранённый интервал таймера или количество нажатий. */
enum class EntryType { TIMER, TAPS }

/** Сохранённая запись таймера (секунды) или счётчика нажатий. */
data class TimerEntry(
    val id: String,
    val type: EntryType,
    val seconds: Int = 0,
    val taps: Int = 0,
    val dateEpochDay: Long = LocalDate.now().toEpochDay()
) : Serializable {

    /** Значение записи в удобном виде: "mm:ss" для таймера, число для нажатий. */
    val displayValue: String
        get() = if (type == EntryType.TIMER) {
            "%02d:%02d".format(seconds / 60, seconds % 60)
        } else {
            taps.toString()
        }

    val localDate: LocalDate get() = LocalDate.ofEpochDay(dateEpochDay)
}

/** Упражнение. stepPerTap — насколько увеличивать счётчик при одном нажатии в Tapper. */
data class Exercise(
    val id: String,
    val name: String,
    val stepPerTap: Int = 1
) : Serializable

/** Запись о выполненном подходе упражнения. */
data class LogEntry(
    val id: String,
    val exerciseId: String,
    val type: EntryType,
    val value: Int, // секунды для TIMER, количество повторений для TAPS
    val dateEpochDay: Long = LocalDate.now().toEpochDay()
) : Serializable {
    val localDate: LocalDate get() = LocalDate.ofEpochDay(dateEpochDay)
}

/** Настройки приложения. */
data class AppSettings(
    val showAsCards: Boolean = true
) : Serializable

/** Полное состояние данных, которое сохраняется в JSON-файл. */
data class FlexData(
    val exercises: List<Exercise> = emptyList(),
    val savedTimerEntries: List<TimerEntry> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val settings: AppSettings = AppSettings()
) : Serializable
