package ru.chedaro.flexfit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.chedaro.flexfit.data.EntryType
import ru.chedaro.flexfit.data.FlexRepository
import ru.chedaro.flexfit.ui.screens.ExerciseDetailScreen
import ru.chedaro.flexfit.ui.screens.ExercisesScreen
import ru.chedaro.flexfit.ui.screens.GraphsScreen
import ru.chedaro.flexfit.ui.screens.InputScreen
import ru.chedaro.flexfit.ui.screens.SelectEntryScreen
import ru.chedaro.flexfit.ui.screens.SelectExerciseScreen
import ru.chedaro.flexfit.ui.screens.StarterScreen
import ru.chedaro.flexfit.ui.screens.TimerScreen
import ru.chedaro.flexfit.ui.screens.TapperScreen

object Routes {
    const val STARTER = "starter"
    const val TIMER = "timer"
    const val TAPPER = "tapper"
    const val INPUT = "input"
    const val GRAPHS = "graphs"
    const val EXERCISES = "exercises"

    // type — EntryType.name (TIMER/TAPS), используется окном выбора записей
    const val SELECT_ENTRY_PATTERN = "select_entry/{type}"
    const val SELECT_EXERCISE_PATTERN = "select_exercise/{entryId}"
    const val EXERCISE_DETAIL_PATTERN = "exercise_detail/{exerciseId}"

    fun selectEntry(type: EntryType) = "select_entry/${type.name}"
    fun selectExercise(entryId: String) = "select_exercise/$entryId"
    fun exerciseDetail(exerciseId: String) = "exercise_detail/$exerciseId"
}

@Composable
fun FlexNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.STARTER,
        modifier = modifier
    ) {

        composable(Routes.STARTER) {
            StarterScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Routes.TIMER) {
            TimerScreen(
                onBack = { navController.popBackStack() },
                onOpenSelect = { navController.navigate(Routes.selectEntry(EntryType.TIMER)) }
            )
        }

        composable(Routes.TAPPER) {
            TapperScreen(
                onBack = { navController.popBackStack() },
                // Всегда через окно списка записей: там видно историю чисел,
                // и она обновляется мгновенно после записи.
                onOpenSelectEntries = { navController.navigate(Routes.selectEntry(EntryType.TAPS)) },
                onOpenSelectExercise = { navController.navigate(Routes.selectEntry(EntryType.TAPS)) }
            )
        }

        composable(Routes.INPUT) {
            InputScreen(
                onBack = { navController.popBackStack() },
                // Единый путь как у Таппера: сохраняем число и открываем список записей.
                onNumberConfirmed = { value ->
                    FlexRepository.saveTaps(value)
                    navController.navigate(Routes.selectEntry(EntryType.TAPS))
                }
            )
        }

        composable(Routes.GRAPHS) {
            GraphsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.EXERCISES) {
            ExercisesScreen(
                onBack = { navController.popBackStack() },
                onOpenDetail = { exerciseId ->
                    navController.navigate(Routes.exerciseDetail(exerciseId))
                }
            )
        }

        composable(
            route = Routes.SELECT_ENTRY_PATTERN,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) {
            val type = EntryType.valueOf(it.arguments?.getString("type") ?: EntryType.TIMER.name)
            SelectEntryScreen(
                entryType = type,
                onBack = { navController.popBackStack() },
                onSelect = { entry ->
                    navController.navigate(Routes.selectExercise(entry.id))
                }
            )
        }

        composable(
            route = Routes.SELECT_EXERCISE_PATTERN,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) {
            val entryId = it.arguments?.getString("entryId").orEmpty()
            val entry = FlexRepository.data.value.savedTimerEntries.firstOrNull { e -> e.id == entryId }
            if (entry != null) {
                SelectExerciseScreen(
                    entry = entry,
                    onBack = { navController.popBackStack() },
                    onPicked = { exerciseId ->
                        // Синхронно записываем значение в историю упражнения...
                        FlexRepository.logAction(entry, exerciseId)
                        // ...затем ОДИН popBackStack(): возвращаемся ровно на один
                        // экран назад (список записей). Двойной pop здесь вызывал
                        // гонку навигации и «белый экран» на Android 9.
                        val popped = navController.popBackStack()
                        if (!popped) {
                            // Страховка: если back stack пуст (прямое открытие deep link),
                            // возвращаемся на стартовый экран без удаления стартовой точки.
                            navController.navigate(Routes.STARTER) {
                                popUpTo(Routes.STARTER) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            } else {
                // Запись уже использована/удалена — просто выходим
                navController.popBackStack()
            }
        }

        composable(
            route = Routes.EXERCISE_DETAIL_PATTERN,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) {
            val exerciseId = it.arguments?.getString("exerciseId").orEmpty()
            ExerciseDetailScreen(
                exerciseId = exerciseId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
