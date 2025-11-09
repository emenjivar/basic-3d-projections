package com.emenjivar.threedimensionalprojections

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.emenjivar.threedimensionalprojections.math.scale
import com.emenjivar.threedimensionalprojections.math.xRotationMatrix
import com.emenjivar.threedimensionalprojections.math.yRotationMatrix
import com.emenjivar.threedimensionalprojections.math.zRotationMatrix
import com.emenjivar.threedimensionalprojections.ui.theme.ThreeDimensionalProjectionsTheme

private val edgeWidth = 1.dp

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

                var scale by remember { mutableFloatStateOf(1f) }

                Box {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, gestureZoom, gestureRotate ->
                                    scale = scale * gestureZoom
                                    _xRotation += pan.y
                                    _yRotation += pan.x
                                }
                            }
                    ) {
                        val projectedCoordinates = cubeVertexes.map { vertex ->
                            project3DCoordinate(
                                x = vertex.x,
                                y = vertex.y,
                                z = vertex.z,
                                alpha = _xRotation, // y-rotation
                                beta = _yRotation, // x-rotation
                                gamma = 0f, // z-rotation,
                                zDistance = zDistance,
                                scalar = scale
                            )
                        }

                        val zRotatedCoordinates = cubeVertexes.map { vertex ->
                            val alphaRad = Math.toRadians(_xRotation.toDouble()).toFloat()
                            val betaRad = Math.toRadians(_yRotation.toDouble()).toFloat()

                            val original = arrayOf(
                                floatArrayOf(vertex.x),
                                floatArrayOf(vertex.y),
                                floatArrayOf(vertex.z)
                            )

                            val xRotation = xRotationMatrix(alphaRad) multiply original
                            val yRotation = yRotationMatrix(betaRad) multiply xRotation
                            yRotation[2][0] // z-coordinate
                        }

                        edges.forEach { edge ->
                            val startCoordinates = projectedCoordinates[edge.first]
                            val endCoordinates = projectedCoordinates[edge.second]

                            val startOffset = Offset(
                                x = normalizeWidth(startCoordinates.x),
                                y = normalizeHeight(startCoordinates.y)
                            )
                            val endOffset = Offset(
                                x = normalizeWidth(endCoordinates.x),
                                y = normalizeHeight(endCoordinates.y)
                            )

                            // Draw vertex
                            drawLine(
                                color = Color.Black,
                                start = startOffset,
                                end = endOffset,
                                strokeWidth = edgeWidth.toPx() * scale
                            )
                        }

                        // Draw faces (z-index is not considered for now)
                        faces.map { face ->
                            val averageZ = listOf(
                                zRotatedCoordinates[face.a],
                                zRotatedCoordinates[face.b],
                                zRotatedCoordinates[face.c],
                                zRotatedCoordinates[face.d]
                            ).average()

                            averageZ to face
                        }.sortedBy { it.first }
                            .forEach { (_, face) ->
                                val a = projectedCoordinates[face.a]
                                val b = projectedCoordinates[face.b]
                                val c = projectedCoordinates[face.c]
                                val d = projectedCoordinates[face.d]

                                drawPath(
                                    color = face.color,
                                    alpha = 1f,
                                    path = Path().apply {
                                        moveTo(normalizeWidth(a.x), normalizeHeight(a.y))
                                        lineTo(normalizeWidth(b.x), normalizeHeight(b.y))
                                        lineTo(normalizeWidth(c.x), normalizeHeight(c.y))
                                        lineTo(normalizeWidth(d.x), normalizeHeight(d.y))
                                        close() // Connect back to start and fills
                                    }
                                )
                            }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(20.dp)
                            .navigationBarsPadding(),
                    ) {
                        Text("scale: $scale")
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
    zDistance: Float,
    scalar: Float
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

    val scaled = original scale scalar
    val xRotated = xRotationMatrix(alphaRad) multiply scaled
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