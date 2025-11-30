package com.example.studybuddyplannerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studybuddyplannerapp.data.Task
import com.example.studybuddyplannerapp.data.TaskRepository
import com.example.studybuddyplannerapp.ui.theme.StudyBuddyPlannerAppTheme
import com.example.studybuddyplannerapp.ui.viewmodel.TaskViewModel
import com.example.studybuddyplannerapp.ui.viewmodel.TaskViewModelFactory
import com.example.studybuddyplannerapp.ui.viewmodel.calculateTaskStats
import java.time.LocalDate
import java.time.format.DateTimeParseException

class MainActivity : ComponentActivity() {

    // ViewModel with DataStore-backed repository
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(TaskRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyBuddyPlannerAppTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudyBuddyNavHost(
                        navController = navController,
                        viewModel = taskViewModel
                    )
                }
            }
        }
    }
}

/* ---------- Navigation host ---------- */

@Composable
fun StudyBuddyNavHost(
    navController: NavHostController,
    viewModel: TaskViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onGoToAdd = { navController.navigate("addEdit") },
                onGoToStats = { navController.navigate("stats") }
            )
        }

        composable("addEdit") {
            AddEditTaskScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("stats") {
            StatsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

/* ---------- Home screen ---------- */

@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    onGoToAdd: () -> Unit,
    onGoToStats: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "StudyBuddy Planner",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Today's tasks",
            style = MaterialTheme.typography.titleMedium
        )

        if (tasks.isEmpty()) {
            Text(
                text = "No tasks yet. Tap \"Add task\" to start.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onToggleDone = { id -> viewModel.toggleDone(id) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onGoToAdd,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Add task")
            }

            Button(
                onClick = onGoToStats,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "View stats")
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onToggleDone: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onToggleDone(task.id) }
            )

            Column {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = task.subject,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Due: ${task.date}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/* ---------- Add / Edit task screen (add only for now) ---------- */

@Composable
fun AddEditTaskScreen(
    viewModel: TaskViewModel,
    onBackClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") } // yyyy-MM-dd
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add / edit task",
            style = MaterialTheme.typography.headlineSmall
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = dateText,
            onValueChange = { dateText = it },
            label = { Text("Due date (YYYY-MM-DD, optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (title.isBlank() || subject.isBlank()) {
                    errorMessage = "Title and subject cannot be empty."
                    return@Button
                }

                val parsedDate = if (dateText.isBlank()) {
                    LocalDate.now()
                } else {
                    try {
                        LocalDate.parse(dateText)
                    } catch (_: DateTimeParseException) {
                        errorMessage = "Invalid date. Use format YYYY-MM-DD."
                        return@Button
                    }
                }

                viewModel.addTask(title, subject, parsedDate)
                onBackClick()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save task")
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Cancel")
        }
    }
}

/* ---------- Stats screen (live stats + clearAll) ---------- */

@Composable
fun StatsScreen(
    viewModel: TaskViewModel,
    onBackClick: () -> Unit
) {
    // Observe tasks so UI updates when clearAll() is pressed
    val tasks by viewModel.tasks.collectAsState()
    val (total, done, pending) = calculateTaskStats(tasks)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Study stats",
            style = MaterialTheme.typography.headlineSmall
        )

        HorizontalDivider()

        Text("Total tasks: $total")
        Text("Completed: $done")
        Text("Pending: $pending")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.clearAll() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear all tasks")
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Back")
        }
    }
}

/* ---------- Simple preview (no ViewModel) ---------- */

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    StudyBuddyPlannerAppTheme {
        val sampleTasks = listOf(
            Task(
                id = 1,
                title = "Sample task",
                subject = "Preview",
                date = LocalDate.now()
            )
        )
        Column(modifier = Modifier.padding(24.dp)) {
            sampleTasks.forEach {
                TaskCard(task = it, onToggleDone = {})
            }
        }
    }
}
