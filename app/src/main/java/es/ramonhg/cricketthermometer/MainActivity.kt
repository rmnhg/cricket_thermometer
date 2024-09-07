package es.ramonhg.cricketthermometer

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.ramonhg.cricketthermometer.ui.theme.CricketThermometerTheme
import kotlin.time.TimeSource

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CricketThermometerTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                colors = topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.primary,
                                ),
                                title = {
                                    Text("Cricket Thermometer")
                                }
                            )
                        }
                ) { innerPadding ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CricketTemp(innerPadding)
                    }
                }
            }
        }
    }
}

fun convertCelsiusToFahrenheit(celsius: Double): Double {
    return (celsius * 9/5) + 32
}

@SuppressLint("DefaultLocale")
fun getTempFromCricketSounds(soundsPerMinute: Double, toFahrenheit: Boolean): String {
    val resultInCelsius = ((soundsPerMinute - 40)/4 + 18)/1.8
    val suffix = if (toFahrenheit) " ºF" else " ºC"
    return String.format("%.2f", (if (toFahrenheit) convertCelsiusToFahrenheit(resultInCelsius) else resultInCelsius)) + suffix
}

fun updateTaps(taps: MutableList<TimeSource.Monotonic.ValueTimeMark>) {
    taps.add(TimeSource.Monotonic.markNow())
}

fun calculateSoundsPerMinute(taps: MutableList<TimeSource.Monotonic.ValueTimeMark>): String {
    if (taps.size < 4)
        return ""
    else {
        var originalTaps = taps.toMutableList()
        var removeOldTaps = false
        var durationsInMilliseconds = mutableListOf<Long>()
        for (i in 1 until originalTaps.size - 1) {
            var currentDuration = (originalTaps[i] - originalTaps[i - 1]).inWholeMilliseconds
            if (currentDuration < 1000)
                durationsInMilliseconds.add(currentDuration)
            else {
                removeOldTaps = true
                durationsInMilliseconds.clear()
                taps.clear()
            }
            if (removeOldTaps)
                taps.add(originalTaps[i])
        }
        return if (durationsInMilliseconds.isEmpty())
            ""
        else
            (60000 / durationsInMilliseconds.average()).toInt().toString()

    }
}

@Composable
fun CricketTemp(innerPadding: PaddingValues) {
    var numberStr by remember { mutableStateOf("") }
    var toFahrenheit by remember { mutableStateOf(false) }
    var taps by remember { mutableStateOf(mutableListOf(TimeSource.Monotonic.markNow())) }
    Column(
        modifier = Modifier
            .padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Count and enter the number of cricket sounds in a minute or tap the button to" +
                " calculate it:")
        Button(onClick = {
            updateTaps(taps)
            numberStr = calculateSoundsPerMinute(taps)
        }) {
            Text("Tap to count")
        }
        OutlinedTextField(
            value = numberStr,
            onValueChange = { numberStr = it },
            label = { Text("Number of sounds per minute") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("To Fahrenheit:")
            Switch(
                checked = toFahrenheit,
                onCheckedChange = {
                    toFahrenheit = it
                }
            )
        }
        Text("Calculated temp: ${if (numberStr.isNotEmpty()) getTempFromCricketSounds(numberStr.toDouble(), toFahrenheit) else ""}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CricketThermometerTheme {
        Scaffold(modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text("Cricket Thermometer")
                    }
                )
            }
        ) { innerPadding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CricketTemp(innerPadding)
            }
        }
    }
}