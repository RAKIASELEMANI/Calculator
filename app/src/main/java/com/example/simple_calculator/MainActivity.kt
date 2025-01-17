package com.example.simple_calculator

import android.os.Bundle
import android.content.Context
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.simple_calculator.ui.theme.Simple_CalculatorTheme
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Simple_CalculatorTheme {
                CalculatorApp()
            }
        }
    }
}

@Composable
fun CalculatorApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "calculator") {
        composable("calculator") { CalculatorScreen(navController) }
        composable("history") { HistoryScreen(navController) }
    }
}

@Composable
fun CalculatorScreen(navController: NavController) {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf<String?>(null) }
    var firstOperand by remember { mutableStateOf<String?>(null) }
    var isResultDisplayed by remember { mutableStateOf(false) }
    var isScientificExpanded by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf(mutableListOf<String>()) }
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    val expandedHeight by animateDpAsState(targetValue = if (isScientificExpanded) 200.dp else 80.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "RAD",
                color = Color.Black,
                fontSize = 20.sp
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White)
                    .clickable { navController.navigate("history") },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ":",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(color = Color.White),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = when {
                    isResultDisplayed -> result
                    input.isEmpty() && firstOperand.isNullOrEmpty() && operator.isNullOrEmpty() -> ""
                    else -> {
                        val displayFirstOperand = firstOperand ?: ""
                        val displayOperator = operator ?: ""
                        val displayInput = input
                        "$displayFirstOperand $displayOperator $displayInput"
                    }
                },
                color = Color.Black,
                fontSize = 32.sp,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.End
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                val buttons = listOf(
                    listOf("7", "8", "9"),
                    listOf("4", "5", "6"),
                    listOf("1", "2", "3"),
                    listOf(".", "0", "Clear")
                )
                for (row in buttons) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (label in row) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                                    .background(color = Color.DarkGray)
                                    .padding(2.dp)
                                    .clickable {
                                        vibrator.vibrate(50)

                                        if (isResultDisplayed) {
                                            input = ""
                                            isResultDisplayed = false
                                        }

                                        when (label) {
                                            "DEL" -> {
                                                input = ""
                                                result = ""
                                                operator = null
                                                firstOperand = null
                                                isResultDisplayed = false
                                            }
                                            else -> {
                                                input += label
                                            }
                                        }
                                    }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 24.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val operators = listOf("÷", "x", "-", "+", "=")
                for (op in operators) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .background(color = Color.LightGray)
                            .padding(2.dp)
                            .clickable {
                                vibrator.vibrate(50)

                                if (isResultDisplayed) {
                                    isResultDisplayed = false
                                    firstOperand = result
                                    input = ""
                                }

                                when (op) {
                                    "=" -> {
                                        result = calculate(firstOperand ?: "", input, operator ?: "")
                                        history.add("$firstOperand $operator $input = $result")
                                        isResultDisplayed = true
                                    }
                                    else -> {
                                        operator = op
                                        firstOperand = input
                                        input = ""
                                    }
                                }
                            }
                    ) {
                        Text(
                            text = op,
                            fontSize = 24.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(expandedHeight)
                    .background(color = Color(0xFF009688))
                    .clickable { isScientificExpanded = !isScientificExpanded }
            ) {
                Text(
                    text = "Scientific",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (isScientificExpanded) {
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color(0xFF009688))
                ) {
                    val scientificButtons = listOf(
                        listOf("sin", "cos", "tan"),
                        listOf("ln", "log", "√"),
                        listOf("π", "e", "^"),
                        listOf("log2", "tan⁻¹")
                    )

                    for (row in scientificButtons) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (label in row) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(80.dp)
                                        .background(color = Color(0xFF009688))
                                        .padding(2.dp)
                                        .clickable {
                                            vibrator.vibrate(50)

                                            if (isResultDisplayed) {
                                                input = ""
                                                isResultDisplayed = false
                                            }

                                            when (label) {
                                                "sin" -> input = sin(Math.toRadians(input.toDoubleOrNull() ?: 0.0)).toString()
                                                "cos" -> input = cos(Math.toRadians(input.toDoubleOrNull() ?: 0.0)).toString()
                                                "tan" -> input = tan(Math.toRadians(input.toDoubleOrNull() ?: 0.0)).toString()
                                                "ln" -> input = ln(input.toDoubleOrNull() ?: 1.0).toString()
                                                "log" -> input = log10(input.toDoubleOrNull() ?: 1.0).toString()
                                                "√" -> input = sqrt(input.toDoubleOrNull() ?: 0.0).toString()
                                                "π" -> input = Math.PI.toString()
                                                "e" -> input = Math.E.toString()
                                                "^" -> input = input.toDoubleOrNull()?.let { base -> base.pow(2) }?.toString() ?: "0"
                                                "log2" -> input = log2(input.toDoubleOrNull() ?: 1.0).toString()
                                                "tan⁻¹" -> input = Math.toDegrees(atan(input.toDoubleOrNull() ?: 0.0)).toString()
                                            }
                                        }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 24.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun calculate(firstOperand: String, secondOperand: String, operator: String): String {
    return try {
        val num1 = firstOperand.toDouble()
        val num2 = secondOperand.toDouble()
        when (operator) {
            "+" -> (num1 + num2).toString()
            "-" -> (num1 - num2).toString()
            "x" -> (num1 * num2).toString()
            "÷" -> {
                if (num2 != 0.0) {
                    (num1 / num2).toString()
                } else {
                    "Error: Division by zero"
                }
            }
            else -> "Error"
        }
    } catch (e: Exception) {
        "Error: Invalid input"
    }
}

@Composable
fun HistoryScreen(navController: NavController) {
    Text(
        text = "History screen coming soon!",
        modifier = Modifier.fillMaxSize(),
        fontSize = 24.sp,
        textAlign = TextAlign.Center
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Simple_CalculatorTheme {
        CalculatorApp()
    }
}
