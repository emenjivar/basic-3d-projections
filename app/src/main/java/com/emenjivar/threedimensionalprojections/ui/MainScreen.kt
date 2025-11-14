package com.emenjivar.threedimensionalprojections.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.emenjivar.threedimensionalprojections.math.multiply
import com.emenjivar.threedimensionalprojections.math.project3DCoordinate
import com.emenjivar.threedimensionalprojections.math.xRotationMatrix
import com.emenjivar.threedimensionalprojections.math.yRotationMatrix
import com.emenjivar.threedimensionalprojections.shapes.Coordinate2D
import com.emenjivar.threedimensionalprojections.shapes.CubeInstance
import com.emenjivar.threedimensionalprojections.shapes.CustomShape
import com.emenjivar.threedimensionalprojections.shapes.RubikCubeInstance
import com.emenjivar.threedimensionalprojections.shapes.Shape
import com.emenjivar.threedimensionalprojections.shapes.ShrekInstance
import com.emenjivar.threedimensionalprojections.shapes.TeapotInstance
import com.emenjivar.threedimensionalprojections.shapes.TetrahedronInstance
import kotlinx.coroutines.launch

private val DeepStartColor = Color(0xffc20e0e)
private val DeepEndColor = Color(0xffffe96e)
private const val ROTATION_SPEED = 0.5f
private val BackgroundColor = Color(0xffe7e7e7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val coroutineScope = rememberCoroutineScope()
    var shape by remember { mutableStateOf<Shape>(CubeInstance) }
    val availableShapes = remember {
        listOf(CubeInstance, TetrahedronInstance, RubikCubeInstance, TeapotInstance, ShrekInstance)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var xRotation by remember { mutableFloatStateOf(0f) }
    var yRotation by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }
    var renderVertexes by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundColor)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, gestureRotate ->
                        scale = scale * gestureZoom
                        xRotation += pan.y * ROTATION_SPEED
                        yRotation += pan.x * ROTATION_SPEED
                    }
                }
        ) {
            val projectedCoordinates = shape.vertexes.map { vertex ->
                project3DCoordinate(
                    x = vertex.x,
                    y = vertex.y,
                    z = vertex.z,
                    alpha = xRotation, // y-rotation
                    beta = yRotation, // x-rotation
                    gamma = 0f, // z-rotation,
                    scalar = scale
                )
            }

            val zRotatedCoordinates = shape.vertexes.map { vertex ->
                val alphaRad = Math.toRadians(xRotation.toDouble()).toFloat()
                val betaRad = Math.toRadians(yRotation.toDouble()).toFloat()

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
            Text(text = "Shape name: ${shape.name}")
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
                    coroutineScope.launch {
                        sheetState.expand()
                    }
                }
            ) {
                Text(text = "Pick a shape")
            }
        }
    }

    ShapePickerBottomSheet(
        sheetState = sheetState,
        selectedShape = shape,
        availableLocalShapes = availableShapes,
        onPickLocalShape = { selectedShape ->
            shape = selectedShape
        }
    )
}

/**
 * Converts a normalized coordinate to screen space on the width axis.
 * @param coordinate A normalized value from -1f to 1f.
 * @return A pixel coordinate within the screen.
 */
private fun DrawScope.normalizeWidth(coordinate: Float): Float {
    return size.width / 2f * coordinate + size.width / 2f
}

/**
 * Converts a normalized coordinate to screen space on the height axis.
 * @param coordinate A normalized value from -1f to 1f.
 * @return A pixel coordinate within the screen.
 */
private fun DrawScope.normalizeHeight(coordinate: Float): Float {
    return size.width / 2f * coordinate + size.height / 2f
}
