package com.emenjivar.threedimensionalprojections.shapes

import androidx.compose.ui.graphics.Color

abstract class Shape {
    abstract val name: String
    abstract val vertexes: List<Coordinate3D>
    abstract val faces: List<Face>
}

data class Coordinate2D(val x: Float, val y: Float)
data class Coordinate3D(val x: Float, val y: Float, val z: Float)
data class Face(var indexes: List<Int>, val color: Color)
