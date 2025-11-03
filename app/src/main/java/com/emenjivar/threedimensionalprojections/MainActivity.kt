package com.emenjivar.threedimensionalprojections

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.emenjivar.threedimensionalprojections.ui.theme.ThreeDimensionalProjectionsTheme
import kotlin.math.cos
import kotlin.math.sin

private const val ANIMATION_TIME = 5000

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            ThreeDimensionalProjectionsTheme {
                val cubeVertexes = remember {
                    listOf(
                        // Front edges
                        Coordinate3D(x = -0.5f, y = -0.5f, z = 0.5f), // top-left
                        Coordinate3D(x = -0.5f, y = 0.5f, z = 0.5f), // bottom-left
                        Coordinate3D(x = 0.5f, y = -0.5f, z = 0.5f), // top-right
                        Coordinate3D(x = 0.5f, y = 0.5f, z = 0.5f), // bottom-right
                        // Back edges
                        Coordinate3D(x = -0.5f, y = -0.5f, z = -0.5f), // top-left
                        Coordinate3D(x = -0.5f, y = 0.5f, z = -0.5f), // bottom-left
                        Coordinate3D(x = 0.5f, y = -0.5f, z = -0.5f), // top-right
                        Coordinate3D(x = 0.5f, y = 0.5f, z = -0.5f), // bottom-right
                    )
                }

                val edges = remember {
                    listOf(
                        // Front edges
                        Pair(0, 1), // top-left to bottom-left
                        Pair(1, 3), // bottom-left to bottom-right
                        Pair(3, 2), // bottom-right to top-right
                        Pair(2, 0), // top-right to top-left
                        // Back edges
                        Pair(4, 5), // top-left to bottom-left
                        Pair(4, 6), // top-left to top-right
                        Pair(5, 7), // bottom-left to bottom-right
                        Pair(7, 6),
                        // Union edges
                        Pair(0, 4),
                        Pair(1, 5),
                        Pair(2, 6),
                        Pair(3, 7),
                    )
                }

                val rotationTransition = rememberInfiniteTransition(label = "rotation")
                val xRotation = rotationTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = ANIMATION_TIME),
                        repeatMode = RepeatMode.Restart
                    )
                )

                val yRotation = rotationTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = ANIMATION_TIME),
                        repeatMode = RepeatMode.Restart
                    )
                )

                val zRotation = rotationTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = ANIMATION_TIME),
                        repeatMode = RepeatMode.Restart
                    )
                )

                var _xRotation by remember { mutableFloatStateOf(0f) }
                var _yRotation by remember { mutableFloatStateOf(0f) }

                Canvas(
                    modifier = Modifier.fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val slowFactor = 0.5f
                                // TODO: switch the values
                                _yRotation += -(dragAmount.x * slowFactor % 360f)
                                _xRotation += dragAmount.y * slowFactor % 360f

                                Log.wtf("MainActivity", "drag (x: ${dragAmount.x}, y: ${dragAmount.y}), rotation: ($_yRotation,$_xRotation) ")
                            }
                        }
                ) {
                    edges.forEach { edge ->
                        val start = cubeVertexes[edge.first]
                        val end = cubeVertexes[edge.second]

                        val startCoordinates = project3DCoordinate(
                            x = start.x,
                            y = start.y,
                            z = start.z,
                            alpha = _xRotation, // y-rotation
                            beta = _yRotation, // x-rotation
                            gamma = zRotation.value * 0f // z-rotation
                        )

                        val endCoordinates = project3DCoordinate(
                            x = end.x,
                            y = end.y,
                            z = end.z,
                            alpha = _xRotation, // y-rotation
                            beta = _yRotation, // x-rotation
                            gamma = zRotation.value * 0f // z-rotation
                        )

                        val startOffset = Offset(
                            x = normalizeWidth(startCoordinates.x),
                            y = normalizeHeight(startCoordinates.y)
                        )
                        val endOffset = Offset(
                            x = normalizeWidth(endCoordinates.x),
                            y = normalizeHeight(endCoordinates.y)
                        )

                        // Draw vertex in pairs (most efficient)
                        drawCircle(
                            color = Color.Red,
                            radius = 10f,
                            center = startOffset
                        )
                        drawCircle(
                            color = Color.Red,
                            radius = 10f,
                            center = endOffset
                        )

                        // Draw vertex
                        drawLine(color = Color.Black, start = startOffset, end = endOffset)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

data class Coordinate(val x: Float, val y: Float)
data class Coordinate3D(val x: Float, val y: Float, val z: Float)

/**
 * @param alpha Angle in degrees.
 * @param beta Angle in degrees.
 * @param gamma Angle in degrees
 */
fun project3DCoordinate(
    x: Float,
    y: Float,
    z: Float,
    alpha: Float,
    beta: Float,
    gamma: Float
): Coordinate {
    val alphaRad = Math.toRadians(alpha.toDouble()).toFloat()
    val betaRad = Math.toRadians(beta.toDouble()).toFloat()
    val gammaRad = Math.toRadians(gamma.toDouble()).toFloat()
    return Coordinate(
        x = x * cos(betaRad) * cos(gammaRad) - y * cos(betaRad) * sin(gammaRad) + z * sin(betaRad),
        y = x * (sin(alphaRad) * sin(betaRad) * cos(gammaRad) + cos(alphaRad) * sin(gammaRad)) + y * (cos(alphaRad) * cos(gammaRad) - sin(alphaRad) * sin(gammaRad)) - z * sin(alphaRad) * cos(betaRad)
    )
}

/**
 * @param coordinate A value from 0 to 1f.
 * @return A coordinate within the screen.
 */
fun DrawScope.normalizeWidth(coordinate: Float): Float {
    return size.width / 2f * coordinate + size.width / 2f
}

fun DrawScope.normalizeHeight(coordinate: Float): Float {
    return size.width  / 2f * coordinate + size.height / 2f
}