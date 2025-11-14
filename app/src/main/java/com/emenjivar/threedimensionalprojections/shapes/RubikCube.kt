package com.emenjivar.threedimensionalprojections.shapes

import androidx.compose.ui.graphics.Color

class RubikCube : Shape() {

    override val name: String
        get() = "RubiksCube"

    // Size of each small cube
    private val cubeSize = 0.33f
    private val gap = 0.02f // Small gap between cubes
    private val offset = cubeSize + gap

    override val vertexes: List<Coordinate3D>
        get() {
            val vertices = mutableListOf<Coordinate3D>()

            // Create 27 small cubes (3x3x3)
            for (x in -1..1) {
                for (y in -1..1) {
                    for (z in -1..1) {
                        val baseX = x * offset
                        val baseY = y * offset
                        val baseZ = z * offset
                        val half = cubeSize / 2f

                        // 8 vertices for each small cube
                        vertices.addAll(listOf(
                            Coordinate3D(baseX - half, baseY - half, baseZ + half), // 0: front-top-left
                            Coordinate3D(baseX - half, baseY + half, baseZ + half), // 1: front-bottom-left
                            Coordinate3D(baseX + half, baseY - half, baseZ + half), // 2: front-top-right
                            Coordinate3D(baseX + half, baseY + half, baseZ + half), // 3: front-bottom-right
                            Coordinate3D(baseX - half, baseY - half, baseZ - half), // 4: back-top-left
                            Coordinate3D(baseX - half, baseY + half, baseZ - half), // 5: back-bottom-left
                            Coordinate3D(baseX + half, baseY - half, baseZ - half), // 6: back-top-right
                            Coordinate3D(baseX + half, baseY + half, baseZ - half)  // 7: back-bottom-right
                        ))
                    }
                }
            }

            return vertices
        }

    override val faces: List<Face>
        get() {
            val facesList = mutableListOf<Face>()

            // For each of the 27 cubes
            for (cubeIndex in 0 until 27) {
                val baseVertex = cubeIndex * 8
                val x = (cubeIndex / 9) - 1        // -1, 0, 1
                val y = ((cubeIndex % 9) / 3) - 1  // -1, 0, 1
                val z = (cubeIndex % 3) - 1        // -1, 0, 1

                // Front face (z = 1) - external or internal
                val frontColor = if (z == 1) getColorForFace("front") else Color.Black
                facesList.add(Face(
                    indexes = listOf(
                        baseVertex + 0,
                        baseVertex + 2,
                        baseVertex + 3,
                        baseVertex + 1
                    ),
                    color = frontColor
                ))

                // Back face (z = -1) - external or internal
                val backColor = if (z == -1) getColorForFace("back") else Color.Black
                facesList.add(Face(
                    indexes = listOf(
                        baseVertex + 4,
                        baseVertex + 6,
                        baseVertex + 7,
                        baseVertex + 5
                    ),
                    color = backColor
                ))

                // Top face (y = -1) - external or internal
                val topColor = if (y == -1) getColorForFace("top") else Color.Black
                facesList.add(Face(
                    indexes = listOf(
                        baseVertex + 0,
                        baseVertex + 2,
                        baseVertex + 6,
                        baseVertex + 4
                    ),
                    color = topColor
                ))

                // Bottom face (y = 1) - external or internal
                val bottomColor = if (y == 1) getColorForFace("bottom") else Color.Black
                facesList.add(Face(
                    indexes = listOf(
                        baseVertex + 1,
                        baseVertex + 3,
                        baseVertex + 7,
                        baseVertex + 5
                    ),
                    color = bottomColor
                ))

                // Left face (x = -1) - external or internal
                val leftColor = if (x == -1) getColorForFace("left") else Color.Black
                facesList.add(Face(
                    indexes = listOf(
                        baseVertex + 0,
                        baseVertex + 4,
                        baseVertex + 5,
                        baseVertex + 1
                    ),
                    color = leftColor
                ))

                // Right face (x = 1) - external or internal
                val rightColor = if (x == 1) getColorForFace("right") else Color.Black
                facesList.add(Face(
                    indexes = listOf(
                        baseVertex + 2,
                        baseVertex + 6,
                        baseVertex + 7,
                        baseVertex + 3
                    ),
                    color = rightColor
                ))
            }

            return facesList
        }

    private fun getColorForFace(face: String): Color {
        // Standard Rubik's cube colors:
        // White (top), Yellow (bottom), Red (front), Orange (back), Green (right), Blue (left)
        return when (face) {
            "front" -> Color.Red      // z = 1
            "back" -> Color(0xFFFFA500)  // z = -1 (Orange)
            "top" -> Color.White      // y = -1
            "bottom" -> Color.Yellow  // y = 1
            "left" -> Color.Blue      // x = -1
            "right" -> Color.Green    // x = 1
            else -> Color.Gray
        }
    }
}

val RubikCubeInstance = RubikCube()