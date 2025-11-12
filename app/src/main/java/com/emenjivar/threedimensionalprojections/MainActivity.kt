package com.emenjivar.threedimensionalprojections

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.emenjivar.threedimensionalprojections.math.multiply
import com.emenjivar.threedimensionalprojections.math.projectionMatrix
import com.emenjivar.threedimensionalprojections.math.scale
import com.emenjivar.threedimensionalprojections.math.xRotationMatrix
import com.emenjivar.threedimensionalprojections.math.yRotationMatrix
import com.emenjivar.threedimensionalprojections.math.zRotationMatrix
import com.emenjivar.threedimensionalprojections.parser.convertToShape
import com.emenjivar.threedimensionalprojections.shapes.Coordinate2D
import com.emenjivar.threedimensionalprojections.shapes.Cube
import com.emenjivar.threedimensionalprojections.shapes.CustomShape
import com.emenjivar.threedimensionalprojections.shapes.Shape
import com.emenjivar.threedimensionalprojections.ui.theme.ThreeDimensionalProjectionsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val edgeWidth = 1.dp
private val DeepStartColor = Color(0xffc20e0e)
private val DeepEndColor = Color(0xfffa9c50)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ThreeDimensionalProjectionsTheme {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()

                var shape by remember { mutableStateOf<Shape>(Cube()) }

                // File picker launcher
                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        coroutineScope.launch(Dispatchers.IO) {
                            val result = context.contentResolver.openInputStream(it)
                                ?.bufferedReader()
                                ?.readText()

                            shape = convertToShape(result.orEmpty())

                        }
                    }
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
                var renderVertexes by remember { mutableStateOf(false) }

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
                        val projectedCoordinates = shape.vertexes.map { vertex ->
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

                        val zRotatedCoordinates = shape.vertexes.map { vertex ->
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

                        if (renderVertexes) {
                            projectedCoordinates.forEach { coordinate ->
                                val normalizedCoordinate = Offset(
                                    x = normalizeWidth(coordinate.x),
                                    y = normalizeHeight(coordinate.y)
                                )

                                drawCircle(
                                    color = Color.Red,
                                    center = normalizedCoordinate,
                                    radius = 5.dp.toPx() * scale
                                )
                            }
                        }

                        // Draw faces
                        shape.faces.map { face ->
                            val averageZ = face.indexes.map { index ->
                                zRotatedCoordinates[index]
                            }.average()

                            averageZ to face
                        }.sortedBy { it.first }
                            .forEach { (averageZ, face) ->
                                // Assuming normalized coordinates for averageZ
                                val color = if (shape is CustomShape) {
                                    androidx.compose.ui.graphics.lerp(
                                        start = DeepStartColor,
                                        stop = DeepEndColor,
                                        fraction = averageZ.toFloat()
                                    )
                                } else {
                                    face.color
                                }

                                val points = face.indexes.map { index ->
                                    val v = projectedCoordinates[index]
                                    Coordinate2D(x = normalizeWidth(v.x), y = normalizeHeight(v.y))
                                }

                                drawPath(
                                    color = color,
                                    alpha = 1f,
                                    path = Path().apply {
                                        moveTo(points[0].x, points[0].y)
                                        points.drop(1).forEach { lineTo(it.x, it.y) }
                                        close()
                                    }
                                )
                            }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                            .navigationBarsPadding(),
                    ) {
                        Text("Scale: $scale")
                        Text("Faces: ${shape.faces.size}")
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show vertexes (${shape.vertexes.size}): ")
                            Checkbox(
                                checked = renderVertexes,
                                onCheckedChange = { renderVertexes = it }
                            )
                        }
                        Button(
                            onClick = {
                                filePickerLauncher.launch("*/*")
                            }
                        ) {
                            Text("Select OBJ file")
                        }
                    }
                }
            }
        }
    }
}

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
): Coordinate2D {
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
    return Coordinate2D(
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