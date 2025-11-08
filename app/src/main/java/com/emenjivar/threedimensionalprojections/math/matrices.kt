package com.emenjivar.threedimensionalprojections.math

import kotlin.math.cos
import kotlin.math.sin

val projectionMatrix = arrayOf(
    floatArrayOf(1f, 0f, 0f),
    floatArrayOf(0f, 1f, 0f),
    floatArrayOf(0f, 0f, 0f),
)

fun xRotationMatrix(
    angle: Float
): Array<FloatArray> {
    return arrayOf(
        floatArrayOf(1f, 0f, 0f),
        floatArrayOf(0f, cos(angle), -sin(angle)),
        floatArrayOf(0f, sin(angle), cos(angle)),
    )
}

fun yRotationMatrix(
    angle: Float
): Array<FloatArray> {
    return arrayOf(
        floatArrayOf(cos(angle), 0f, sin(angle)),
        floatArrayOf(0f, 1f, 0f),
        floatArrayOf(-sin(angle), 0f, cos(angle)),
    )
}

fun zRotationMatrix(
    angle: Float
): Array<FloatArray> {
    return arrayOf(
        floatArrayOf(cos(angle), -sin(angle), 0f),
        floatArrayOf(sin(angle), cos(angle), 0f),
        floatArrayOf(0f, 0f, 1f),
    )
}

infix fun FloatArray.dotProduct(b: FloatArray): Float {
    var result = 0f
    for (i in 0..this.size - 1) {
        result += this[i] * b[i]
    }
    return result
}

infix fun Array<FloatArray>.multiply(
    b: Array<FloatArray>
): Array<FloatArray> {
    val rowsA = this.size
    val columnsA = this[0].size
    val rowsB = b.size
    val columnsB = b[0].size

    if (columnsA != rowsB) {
        throw IllegalArgumentException(
            "Matrix dimension incompatible: ($rowsA x $columnsB). Columns of A must equals rows of B."
        )
    }

    // Result matrix will be rowsA * columnsB
    val result = Array(rowsA) { FloatArray(columnsB) }

    // Fill the output matrix
    for (rowA in 0..rowsA - 1) {
        for (colB in 0..columnsB - 1) {
            val columnB = FloatArray(rowsB) { rowB -> b[rowB][colB] }
            result[rowA][colB] = this[rowA] dotProduct columnB
        }
    }

    return result
}