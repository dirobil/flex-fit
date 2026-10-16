package ru.chedaro.flexfit.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.util.UUID

/**
 * Репозиторий данных приложения.
 * Хранение — JSON-файл во внутренней памяти app (filesDir/flexfit_data.json).
 * Используется встроенный org.json (часть Android SDK) — сторонние библиотеки не нужны.
 */
object FlexRepository {

    private const val FILE_NAME = "flexfit_data.json"

    private lateinit var file: File

    private val _data = MutableStateFlow(FlexData())
    val data: StateFlow<FlexData> = _data.asStateFlow()

    fun init(context: Context) {
        if (::file.isInitialized) return
        file = File(context.filesDir, FILE_NAME)
        _data.value = read()
    }

    // ---------- Публичные операции ----------

    fun addExercise(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        mutate { d ->
            d.copy(exercises = d.exercises + Exercise(UUID.randomUUID().toString(), trimmed))
        }
    }

    fun renameExercise(id: String, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        mutate { d ->
            d.copy(exercises = d.exercises.map { if (it.id == id) it.copy(name = trimmed) else it })
        }
    }

    fun setStepPerTap(id: String, step: Int) {
        mutate { d ->
            d.copy(
                exercises = d.exercises.map {
                    if (it.id == id) it.copy(stepPerTap = step.coerceAtLeast(1)) else it
                }
            )
        }
    }

    fun deleteExercise(id: String) {
        mutate { d ->
            d.copy(
                exercises = d.exercises.filter { it.id != id },
                logs = d.logs.filter { it.exerciseId != id }
            )
        }
    }

    /** Сохраняет остановленный интервал таймера. */
    fun saveTimerSeconds(seconds: Int) {
        if (seconds <= 0) return
        mutate { d ->
            d.copy(
                savedTimerEntries = d.savedTimerEntries +
                        TimerEntry(UUID.randomUUID().toString(), EntryType.TIMER, seconds = seconds)
            )
        }
    }

    /**
     * Сохраняет значение нажатий (таппер/ручной ввод) как запись для выбора упражнения.
     * Возвращает id новой записи или null, если значение <= 0.
     */
    fun saveTaps(taps: Int): String? {
        if (taps <= 0) return null
        val entry = TimerEntry(UUID.randomUUID().toString(), EntryType.TAPS, taps = taps)
        mutate { d ->
            d.copy(savedTimerEntries = d.savedTimerEntries + entry)
        }
        return entry.id
    }

    fun removeSavedEntry(id: String) {
        mutate { d ->
            d.copy(savedTimerEntries = d.savedTimerEntries.filter { it.id != id })
        }
    }

    fun clearSavedEntries() {
        mutate { d -> d.copy(savedTimerEntries = emptyList()) }
    }

    /** Очистка только сохранённых интервалов таймера (кнопка «Сброс/Очистить» в окне Timer). */
    fun clearTimerEntries() {
        mutate { d ->
            d.copy(savedTimerEntries = d.savedTimerEntries.filter { it.type != EntryType.TIMER })
        }
    }

    /** Очистка только сохранённых значений нажатий (кнопка «Сброс/Очистить» в окне Tapper). */
    fun clearTapsEntries() {
        mutate { d ->
            d.copy(savedTimerEntries = d.savedTimerEntries.filter { it.type != EntryType.TAPS })
        }
    }

    /** Записывает выполненное действие упражнения и удаляет использованную сохранённую запись. */
    fun logAction(entry: TimerEntry, exerciseId: String) {
        val value = if (entry.type == EntryType.TIMER) entry.seconds else entry.taps
        mutate { d ->
            d.copy(
                logs = d.logs + LogEntry(
                    id = UUID.randomUUID().toString(),
                    exerciseId = exerciseId,
                    type = entry.type,
                    value = value
                ),
                savedTimerEntries = d.savedTimerEntries.filter { it.id != entry.id }
            )
        }
    }

    fun setShowAsCards(cards: Boolean) {
        mutate { d -> d.copy(settings = d.settings.copy(showAsCards = cards)) }
    }

    /** Шаг прироста счётчика за тап (окно «Таппер»), не меньше 1. */
    fun setTapperStep(step: Int) {
        mutate { d -> d.copy(settings = d.settings.copy(tapperStep = step.coerceAtLeast(1))) }
    }

    // ---------- Внутренний механизм ----------

    private inline fun mutate(block: (FlexData) -> FlexData) {
        _data.value = block(_data.value)
        write(_data.value)
    }

    private fun toJson(d: FlexData): String {
        val root = JSONObject()

        root.put("exercises", JSONArray().apply {
            d.exercises.forEach { e ->
                put(JSONObject().apply {
                    put("id", e.id)
                    put("name", e.name)
                    put("stepPerTap", e.stepPerTap)
                })
            }
        })

        root.put("savedEntries", JSONArray().apply {
            d.savedTimerEntries.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("type", t.type.name)
                    put("seconds", t.seconds)
                    put("taps", t.taps)
                    put("dateEpochDay", t.dateEpochDay)
                })
            }
        })

        root.put("logs", JSONArray().apply {
            d.logs.forEach { l ->
                put(JSONObject().apply {
                    put("id", l.id)
                    put("exerciseId", l.exerciseId)
                    put("type", l.type.name)
                    put("value", l.value)
                    put("dateEpochDay", l.dateEpochDay)
                })
            }
        })

        root.put("settings", JSONObject().apply {
            put("showAsCards", d.settings.showAsCards)
            put("tapperStep", d.settings.tapperStep)
        })

        return root.toString(2)
    }

    private fun fromJson(text: String): FlexData {
        return try {
            val root = JSONObject(text)

            val exercises = mutableListOf<Exercise>()
            val exArr = root.optJSONArray("exercises") ?: JSONArray()
            for (i in 0 until exArr.length()) {
                val o = exArr.getJSONObject(i)
                exercises.add(
                    Exercise(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        stepPerTap = o.optInt("stepPerTap", 1)
                    )
                )
            }

            val entries = mutableListOf<TimerEntry>()
            val enArr = root.optJSONArray("savedEntries") ?: JSONArray()
            for (i in 0 until enArr.length()) {
                val o = enArr.getJSONObject(i)
                entries.add(
                    TimerEntry(
                        id = o.getString("id"),
                        type = EntryType.valueOf(o.getString("type")),
                        seconds = o.optInt("seconds", 0),
                        taps = o.optInt("taps", 0),
                        dateEpochDay = o.optLong("dateEpochDay", LocalDate.now().toEpochDay())
                    )
                )
            }

            val logs = mutableListOf<LogEntry>()
            val loArr = root.optJSONArray("logs") ?: JSONArray()
            for (i in 0 until loArr.length()) {
                val o = loArr.getJSONObject(i)
                logs.add(
                    LogEntry(
                        id = o.getString("id"),
                        exerciseId = o.getString("exerciseId"),
                        type = EntryType.valueOf(o.getString("type")),
                        value = o.getInt("value"),
                        dateEpochDay = o.optLong("dateEpochDay", LocalDate.now().toEpochDay())
                    )
                )
            }

            val settingsObj = root.optJSONObject("settings")
            val settings = AppSettings(
                showAsCards = settingsObj?.optBoolean("showAsCards", true) ?: true,
                tapperStep = settingsObj?.optInt("tapperStep", 1)?.coerceAtLeast(1) ?: 1
            )

            FlexData(exercises, entries, logs, settings)
        } catch (e: Exception) {
            FlexData()
        }
    }

    private fun read(): FlexData {
        return if (file.exists()) {
            try {
                fromJson(file.readText())
            } catch (e: Exception) {
                FlexData()
            }
        } else FlexData()
    }

    private fun write(d: FlexData) {
        try {
            file.writeText(toJson(d))
        } catch (e: Exception) {
            // Игнорируем ошибки записи, данные останутся в памяти
        }
    }
}
