package com.emenjivar.threedimensionalprojections.math

object MatricesOperator {
    /**
     * Assuming [a] and [b] as vectors with the same length
     */
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
            throw Exception("The rows of a must equal the number of columns in b")
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
}