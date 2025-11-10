package com.emenjivar.threedimensionalprojections.shapes

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

class Tetrahedron: Shape() {

    private val l = 1f // Length of the sides of the triangle
    private val r = (l * sin(Math.toRadians(30.0)) / sin(Math.toRadians(120.0))).toFloat()

    private val a = polar(r = r, angle = 90f)
    private val b = polar(r = r, angle = 210f)
    private val c = polar(r = r, angle = -30f)

    private fun polar(r: Float, angle: Float): Coordinate2D {
        return Coordinate2D(
            x = r * cos(Math.toRadians(angle.toDouble())).toFloat(),
            y = r * sin(Math.toRadians(angle.toDouble())).toFloat()
        )
    }

    override val vertexes: List<Coordinate3D>
        get() {
            return listOf(
                Coordinate3D(x = a.x, y = a.y, z = 0f),
                Coordinate3D(x = b.x, y = b.y, z = 0f),
                Coordinate3D(x = c.x, y = c.y, z = 0f),
//                Coordinate3D(x = 0f, y = -delta, z = 0f), // Apex (top)
//                Coordinate3D(x = -delta, y = delta, z = delta), // base front-left
//                Coordinate3D(x = delta, y = delta, z = delta), // base front-right
//                Coordinate3D(x = 0f, y = delta, z = -delta) // base back
            )
        }


    override val faces: List<Face> = listOf(
        Face(indexes = listOf(0, 2, 1), color = Color.Yellow),
//        Face(indexes = listOf(1, 3, 2), color = Color.Green),
        Face(indexes = listOf(0, 2, 1), color = Color.Yellow),
    )

    override val edges: List<Edge> = listOf(
        Edge(start = 0, end = 1),
        Edge(start = 1, end = 2),
//        Edge(start = 2, end = 3),
//        Edge(start = 3, end = 1),
        Edge(start = 2, end = 0),
//        Edge(start = 3, end = 0)
    )
}