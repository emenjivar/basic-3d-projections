package com.emenjivar.threedimensionalprojections.shapes

import androidx.compose.ui.graphics.Color

class Cube : Shape() {
    override val vertexes: List<Coordinate3D>
        get() = listOf(
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

    override val faces: List<Face>
        get() = listOf(
            // front
            Face(indexes = listOf(0, 2, 3, 1), color = Color.Blue),
            // top
            Face(indexes = listOf(0, 2, 6, 4), color = Color.Red),
            // bottom
            Face(indexes = listOf(1, 3, 7, 5), color = Color.Green),
            // back
            Face(indexes = listOf(4, 6, 7, 5), color = Color.White),
            // Left
            Face(indexes = listOf(0, 4, 5, 1), color = Color.Yellow),
            // right
            Face(indexes = listOf(2, 6, 7, 3), color = Color(0xFFFF9800))
        )

    override val edges: List<Edge>
        get() = listOf(
//            // Front edges
//            Edge(start = 0, end = 1), // top-left to bottom-left
//            Edge(start = 1, end = 3), // bottom-left to bottom-right
//            Edge(start = 3, end = 2), // bottom-right to top-right
//            Edge(start = 2, end = 0), // top-right to top-left
//            // Back edges
//            Edge(start = 4, end = 5), // top-left to bottom-left
//            Edge(start = 4, end = 6), // top-left to top-right
//            Edge(start = 5, end = 7), // bottom-left to bottom-right
//            Edge(start = 7, end = 6),
//            // Union edges
//            Edge(start = 0, end = 4),
//            Edge(start = 1, end = 5),
//            Edge(start = 2, end = 6),
//            Edge(start = 3, end = 7),
        )
}