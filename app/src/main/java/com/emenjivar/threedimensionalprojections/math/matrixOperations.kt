package com.emenjivar.threedimensionalprojections.math

import com.emenjivar.threedimensionalprojections.shapes.Coordinate2D
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

infix fun Array<FloatArray>.scale(scalar: Float): Array<FloatArray> {
    val scaledMatrix = this.copyOf()
    for (row in 0 until scaledMatrix.size) {
        for (col in 0 until scaledMatrix[row].size) {
            scaledMatrix[row][col] *= scalar
        }
    }
    return scaledMatrix
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
    val w = 1f / (zDistance - zRotated[2][0]) // perspective divide factor

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