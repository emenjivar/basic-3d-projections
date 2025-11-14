package com.emenjivar.threedimensionalprojections.shapes

import androidx.compose.ui.graphics.Color
import kotlin.math.sqrt

class Tetrahedron: Shape() {

    override val name: String
        get() = "Tetrahedron"

    // For a regular tetrahedron centered at origin
    // Using coordinates that form an equilateral triangle base
    private val a = 1f / sqrt(3f)  // Distance from center to vertex
    private val h = sqrt(2f / 3f)   // Height from base to apex

    override val vertexes: List<Coordinate3D>
        get() {
            return listOf(
                // Base vertices forming an equilateral triangle in XY plane
                Coordinate3D(x = a, y = 0f, z = -h / 3f),      // Front
                Coordinate3D(x = -a / 2f, y = a * sqrt(3f) / 2f, z = -h / 3f),  // Back-left
                Coordinate3D(x = -a / 2f, y = -a * sqrt(3f) / 2f, z = -h / 3f), // Back-right
                // Apex vertex
                Coordinate3D(x = 0f, y = 0f, z = 2f * h / 3f)  // Top
            )
        }

    override val faces: List<Face> = listOf(
        // Base (bottom face)
        Face(indexes = listOf(0, 2, 1), color = Color(0xFFE91E63)), // Pink/Magenta
        // Three side faces
        Face(indexes = listOf(0, 1, 3), color = Color(0xFF2196F3)), // Blue
        Face(indexes = listOf(1, 2, 3), color = Color(0xFF4CAF50)), // Green
        Face(indexes = listOf(2, 0, 3), color = Color(0xFFFFA726))  // Orange (better contrast than yellow)
    )
}

val TetrahedronInstance = Tetrahedron()