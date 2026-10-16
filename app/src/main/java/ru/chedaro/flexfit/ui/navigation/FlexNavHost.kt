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
                onOpenSelectEntries = { navController.navigate(Routes.selectEntry(EntryType.TAPS)) },
                // Сохранённое число нажатий — сразу открываем выбор упражнения
                onOpenSelectExercise = { entryId -> navController.navigate(Routes.selectExercise(entryId)) }
            )
        }

        composable(Routes.INPUT) {
            InputScreen(
                onBack = { navController.popBackStack() },
                // Сохраняем введённое число и сразу открываем окно выбора упражнения
                onNumberConfirmed = { value ->
                    val entryId = FlexRepository.saveTaps(value)
                    if (entryId != null) {
                        navController.navigate(Routes.selectExercise(entryId))
                    } else {
                        navController.navigate(Routes.selectEntry(EntryType.TAPS))
                    }
                }
            )
        }

        composable(Routes.GRAPHS) {
            GraphsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.EXERCISES) {
            ExercisesScreen(onBack = { navController.popBackStack() })
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
                        FlexRepository.logAction(entry, exerciseId)
                        // Возвращаемся на экран, откуда открыли выбор (2 уровня назад)
                        navController.previousBackStackEntry?.savedStateHandle["logged"] = true
                        repeat(2) { navController.popBackStack() }
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
