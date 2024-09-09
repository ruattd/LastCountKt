import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import dev.darkokoa.datetimewheelpicker.WheelDateTimePicker
import kotlinx.coroutines.*
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.PrintWriter
import kotlin.io.path.*
import java.nio.file.Path

// config type
@Serializable
data class Config(
    val hintA: String = "某考试",
    val timeA: LocalDateTime = LocalDateTime(
        year = 2024,
        monthNumber = 10,
        dayOfMonth = 22,
        hour = 7,
        minute = 30,
    ),
    val hintB: String = "最终考试",
    val timeB: LocalDateTime = LocalDateTime(
        year = 2025,
        monthNumber = 5,
        dayOfMonth = 10,
        hour = 7,
        minute = 30,
    ),
    val positionX: Int = 540,
    val positionY: Int = 230,
)

// json serializer
val json = Json {
    encodeDefaults = true
    this.prettyPrint = true
}

//time
private var timeA: Instant? = null
private var timeB: Instant? = null

// files
private val directory = Path.of(Config::class.java.protectionDomain.codeSource.location.toURI()).parent
private val configPath = directory.resolve("LastCount.json")
private val icon = BitmapPainter(loadImageBitmap(Config::class.java.getResourceAsStream("assets/icon.png")!!))

private fun writeConfig(config: Config) {
    configPath.writeText(json.encodeToString(config), Charsets.UTF_8)
}

@OptIn(ExperimentalTextApi::class)
fun app() = application {
    var hintA by remember { mutableStateOf("") }
    var hintB by remember { mutableStateOf("") }

    var positionX by remember { mutableStateOf(0) }
    var positionY by remember { mutableStateOf(0) }

    var initialized by remember { mutableStateOf(false) }

    // read config
    if (!initialized) {
        rememberCoroutineScope().launch {
            withContext(Dispatchers.IO) {
                if (configPath.notExists()) {
                    configPath.createFile()
                    writeConfig(Config())
                }
                val config = json.decodeFromString<Config>(configPath.readText(Charsets.UTF_8))
                timeA = config.timeA.toInstant(TimeZone.currentSystemDefault())
                timeB = config.timeB.toInstant(TimeZone.currentSystemDefault())
                hintA = config.hintA
                hintB = config.hintB
                positionX = config.positionX
                positionY = config.positionY
                initialized = true
            }
        }
    }

    val font = FontFamily("HarmonyOS Sans SC")

    // settings window logic
    var settingsVisible by remember { mutableStateOf(false) }
    Window(
        onCloseRequest = { settingsVisible = false },
        visible = settingsVisible,
        title = "倒计时设置",
        icon = icon,
        state = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(400.dp, 600.dp)
        ),
        resizable = false,
    ) {
        MaterialTheme {
            @Composable
            fun Input(
                value: String,
                hint: String,
                onValueChange: (String) -> Unit,
                modifier: Modifier = Modifier
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(hint, fontFamily = font) },
                    textStyle = TextStyle(
                        fontFamily = font
                    ),
                    modifier = modifier
                        .width(80.dp)
                        .padding(2.dp)
                )
            }

            @Composable
            fun Hint(
                text: String,
            ) {
                Text(
                    text = text,
                    fontFamily = font,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
//                    .scrollable(rememberScrollState(), Orientation.Vertical)
                    .padding(5.dp)
            ) {
                Hint("提示文本")
                val modifier = Modifier.fillMaxWidth()
                Input(
                    value = hintA,
                    hint = "提示A",
                    onValueChange = { hintA = it },
                    modifier = modifier
                )
                Input(
                    value = hintB,
                    hint = "提示B",
                    onValueChange = { hintB = it },
                    modifier = modifier
                )

                val timezone = TimeZone.currentSystemDefault()
                val modifierTimePicker = modifier.height(135.dp)
                if (initialized) {
                    Hint("截止时间A")
                    WheelDateTimePicker(
                        startDateTime = timeA!!.toLocalDateTime(TimeZone.currentSystemDefault()),
                        onSnappedDateTime = { timeA = it.toInstant(timezone) },
                        modifier = modifierTimePicker
                    )
                    Hint("截止时间B")
                    WheelDateTimePicker(
                        startDateTime = timeB!!.toLocalDateTime(TimeZone.currentSystemDefault()),
                        onSnappedDateTime = { timeB = it.toInstant(timezone) },
                        modifier = modifierTimePicker
                    )
                }

                var buttonEnabled by remember { mutableStateOf(true) }
                Button(
                    onClick = {
                        buttonEnabled = false
                        CoroutineScope(Dispatchers.IO).launch {
                            writeConfig(Config(
                                hintA = hintA,
                                hintB = hintB,
                                timeA = timeA!!.toLocalDateTime(timezone),
                                timeB = timeB!!.toLocalDateTime(timezone),
                                positionX = positionX,
                                positionY = positionY,
                            ))
                            buttonEnabled = true
                        }
                        settingsVisible = false
                    },
                    modifier = modifier
                ) {
                    Text("保存设置", fontFamily = font)
                }
            }
        }
    }
    // main window logic
    var mainVisible by remember { mutableStateOf(true) }
    DialogWindow(
        onCloseRequest = ::exitApplication,
        visible = mainVisible,
        title = "倒计时",
        icon = icon,
        state = DialogState(
            size = DpSize(width = 500.dp, height = 300.dp),
            position = WindowPosition(540.dp, 230.dp)
        ),
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = false,
    ) {
        MaterialTheme {
            var dayLeft1 by remember { mutableStateOf("DayLeft1") }
            var dayLeft2 by remember { mutableStateOf("DayLeft2") }
            var timeLeft by remember { mutableStateOf("TimeLeft") }

            // countdown coroutine
            if (initialized) {
                rememberCoroutineScope().launch {
                    withContext(Dispatchers.IO) {
                        // start time
                        val clock = Clock.System
                        while (true) {
                            val now = clock.now()
                            val durationA = timeA!! - now
                            val durationB = timeB!! - now
                            dayLeft1 = durationA.inWholeDays.toString()
                            dayLeft2 = durationB.inWholeDays.toString()
                            timeLeft = durationA.toComponents { _, hours, minutes, seconds, _ ->
                                String.format("%02d:%02d:%02d", hours, minutes, seconds)
                            }
                            delay(200)
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0))
            ) {
                @Composable fun HintText(text: String, modifier: Modifier = Modifier) {
                    Text(
                        text = text,
                        fontFamily = font,
                        fontSize = 36.sp,
                        modifier = modifier
                    )
                }

                HintText("距${hintA}还有")
                Row(
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = dayLeft1,
                        fontFamily = font,
                        fontSize = 64.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                    )
                    HintText(
                        text = "天",
                        modifier = Modifier
                            .padding(bottom = 8.dp, end = 10.dp)
                    )
                    Text(
                        text = timeLeft,
                        fontFamily = font,
                        fontSize = 64.sp,
                        color = Color.Red.copy(alpha = .9f)
                    )
                }

                HintText("距${hintB}还有")

                Row(
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = dayLeft2,
                        fontFamily = font,
                        fontSize = 72.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                    HintText(
                        text = "天",
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                    )
                }
            }
        }
    }

    // system tray
    var trayShown by remember { mutableStateOf(true) }
    if (initialized && trayShown) {
        val state = rememberTrayState()
        val onOpenSettings = {
            settingsVisible = false
            settingsVisible = true
        }
        Tray(
            icon = icon,
            state = state,
            onAction = onOpenSettings
        ) {
            Item(
                text = if (mainVisible) "Hide" else "Show",
                onClick = { mainVisible = !mainVisible },
            )
            Item(
                text = "Settings",
                onClick = onOpenSettings
            )
            Separator()
            Item(
                text = "Exit",
                onClick = {
                    trayShown = false
                    exitApplication()
                },
            )
        }
    }
}

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        PrintWriter(directory.resolve("LastCount-Exception.txt").writer(Charsets.UTF_8)).let {
            throwable.printStackTrace(it)
            it.close()
        }
    }
    app()
}
