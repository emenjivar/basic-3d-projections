package com.emenjivar.threedimensionalprojections

import android.R
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.emenjivar.threedimensionalprojections.math.multiply
import com.emenjivar.threedimensionalprojections.math.projectionMatrix
import com.emenjivar.threedimensionalprojections.math.xRotationMatrix
import com.emenjivar.threedimensionalprojections.math.yRotationMatrix
import com.emenjivar.threedimensionalprojections.math.zRotationMatrix
import com.emenjivar.threedimensionalprojections.ui.theme.ThreeDimensionalProjectionsTheme

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

                val faces = remember {
                    listOf(
                        CubeFace(0, 2, 3, 1, Color.Blue), // front
                        CubeFace(0, 2, 6, 4, Color.Red), // top
                        CubeFace(1, 3, 7, 5, Color.Green), // bottom
                        CubeFace(4, 6, 7, 5, Color.White), // back
                        CubeFace(0, 4, 5, 1, Color.Yellow), // Left
                        CubeFace(2, 6, 7, 3, Color(0xFFFF9800)) // right
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

                var _xRotation by remember { mutableFloatStateOf(0f) }
                var _yRotation by remember { mutableFloatStateOf(0f) }
                var zDistanceSlider by remember { mutableFloatStateOf(0.5f) }
                val zDistance = lerp(
                    start = 1f,
                    stop = 5f,
                    fraction = zDistanceSlider
                )

                Box {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    Log.wtf("MainActivity", "drag: $dragAmount, scroll delta: ${change.scrollDelta}")
                                    val slowFactor = 0.5f
                                    // TODO: switch the values
                                    _yRotation += dragAmount.x * slowFactor % 360f
                                    _xRotation += dragAmount.y * slowFactor % 360f
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
                                gamma = 0f, // z-rotation,
                                zDistance = zDistance
                            )

                            val endCoordinates = project3DCoordinate(
                                x = end.x,
                                y = end.y,
                                z = end.z,
                                alpha = _xRotation, // y-rotation
                                beta = _yRotation, // x-rotation
                                gamma = 0f, // z-rotation
                                zDistance = zDistance
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

                        // Draw faces (z-index is not considered for now)
                        faces.map { face ->
                            var groupZ = 0f
                            val a = cubeVertexes[face.a].let {
                                project3DCoordinate(
                                    x = it.x,
                                    y = it.y,
                                    z = it.z,
                                    alpha = _xRotation,
                                    beta = _yRotation,
                                    gamma = 0f,
                                    zDistance = zDistance
                                )
                            }

                            val b = cubeVertexes[face.b].let {
                                project3DCoordinate(
                                    x = it.x,
                                    y = it.y,
                                    z = it.z,
                                    alpha = _xRotation,
                                    beta = _yRotation,
                                    gamma = 0f,
                                    zDistance = zDistance
                                )
                            }

                            val c = cubeVertexes[face.c].let {
                                project3DCoordinate(
                                    x = it.x,
                                    y = it.y,
                                    z = it.z,
                                    alpha = _xRotation,
                                    beta = _yRotation,
                                    gamma = 0f,
                                    zDistance = zDistance
                                )
                            }

                            val d = cubeVertexes[face.d].let {
                                project3DCoordinate(
                                    x = it.x,
                                    y = it.y,
                                    z = it.z,
                                    alpha = _xRotation,
                                    beta = _yRotation,
                                    gamma = 0f,
                                    zDistance = zDistance
                                )
                            }

                            // groupZ = a.z + b.z + c.z + d.z

                            drawPath(
                                color = face.color,
                                alpha = 0.3f,
                                path = Path().apply {
                                    moveTo(normalizeWidth(a.x), normalizeHeight(a.y))
                                    lineTo(normalizeWidth(b.x), normalizeHeight(b.y))
                                    lineTo(normalizeWidth(c.x), normalizeHeight(c.y))
                                    lineTo(normalizeWidth(d.x), normalizeHeight(d.y))
                                    close() // Connect back to start and fills
                                }
                            )
                            // groupZ to listOf(a, b, c, d)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(20.dp),
                    ) {
                        Text(text = "Camera distance ($zDistance):")
                        Slider(
                            value = zDistanceSlider,
                            onValueChange = { zDistanceSlider = it }
                        )
                    }
                }
            }
        }
    }
}

data class Coordinate(val x: Float, val y: Float)
data class Coordinate3D(val x: Float, val y: Float, val z: Float)
data class CubeFace(
    val a: Int,
    val b: Int,
    val c: Int,
    val d: Int,
    val color: Color
)

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
    gamma: Float,
    zDistance: Float
): Coordinate {
    val alphaRad = Math.toRadians(alpha.toDouble()).toFloat()
    val betaRad = Math.toRadians(beta.toDouble()).toFloat()
    val gammaRad = Math.toRadians(gamma.toDouble()).toFloat()

    // 3D coordinate
    val original = arrayOf(
        floatArrayOf(x),
        floatArrayOf(y),
        floatArrayOf(z),
    )

    val xRotated = xRotationMatrix(alphaRad) multiply original
    val yRotated = yRotationMatrix(betaRad) multiply xRotated
    val zRotated = zRotationMatrix(gammaRad) multiply yRotated

    // Apply perspective projection
    val w = 1f / (zDistance - zRotated[2][0]) // perpective divide factor

    val projected = projectionMatrix multiply zRotated

    // Assuming zRotated as 1x2 matrix
    return Coordinate(
        x = projected[0][0] * w,
        y = projected[1][0] * w
    )
//    return Coordinate(
//        x = x * cos(betaRad) * cos(gammaRad) - y * cos(betaRad) * sin(gammaRad) + z * sin(betaRad),
//        y = x * (sin(alphaRad) * sin(betaRad) * cos(gammaRad) + cos(alphaRad) * sin(gammaRad)) + y * (cos(alphaRad) * cos(gammaRad) - sin(alphaRad) * sin(gammaRad)) - z * sin(alphaRad) * cos(betaRad)
//    )
}

/**
 * @param coordinate A value from 0 to 1f.
 * @return A coordinate within the screen.
 */
fun DrawScope.normalizeWidth(coordinate: Float): Float {
    return size.width / 2f * coordinate + size.width / 2f
}

fun DrawScope.normalizeHeight(coordinate: Float): Float {
    return size.width / 2f * coordinate + size.height / 2f
}