package com.emenjivar.threedimensionalprojections.parser

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.emenjivar.threedimensionalprojections.shapes.Coordinate3D
import com.emenjivar.threedimensionalprojections.shapes.CustomShape
import com.emenjivar.threedimensionalprojections.shapes.Face
import kotlin.math.min

/**
 * Extract vertexes and faces from an `.obj` object.
 *
 * @param objContent The content of the obj file/
 */
fun convertToShape(objContent: String): CustomShape {
    val vertexes = mapVertexes(objContent)

    return CustomShape(
        vertexes = vertexes,
        faces = mapFaces(objContent, vertexes),
        edges = emptyList()
    )
}

// Vertex lines contains the xyx information for placing
// the point within the 3D space
// e.g. `v 11.305434 38.234230 0.000006`
private fun mapVertexes(objContent: String): List<Coordinate3D> {
    var minX = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE

    var minY = Float.MAX_VALUE
    var maxY = Float.MIN_VALUE

    var minZ = Float.MAX_VALUE
    var maxZ = Float.MIN_VALUE

    val vertexes = objContent.lines()
        .filter { it.startsWith("v ") }
        .map { vertexLine ->
            val split = vertexLine.split(" ").drop(1)
            val x = split[0].trim().toFloat()
            val y = split[1].trim().toFloat()
            val z = split[2].trim().toFloat()

            minX = min(x, minX)
            maxX = maxOf(x, maxX)

            minY = minOf(y, minY)
            maxY = maxOf(y, maxY)

            minZ = minOf(z, minZ)
            maxZ = maxOf(z, maxZ)

            arrayOf(x,y,z)
        }

    // User for normalize the coordinates
    val rangeX = maxX - minX
    val rangeY = maxY - minY
    val rangeZ = maxZ - minZ
    val maxRange = maxOf(rangeX, maxOf(rangeY, rangeZ))
    val scale = if (maxRange != 0f) maxRange else 1f // Prevent division by zero

    // Center the shape at the origin of the 3D space
    val centerX = (minX + maxX) / 2f
    val centerY = (minY + maxY) / 2f
    val centerZ = (minZ + maxZ) / 2f

    return vertexes.map { points ->
        Coordinate3D(
            x = (points[0] - centerX) / scale,
            y = (points[1] - centerY) / scale,
            z = (points[2] - centerZ) / scale
        )
    }
}

// Faces lines contains the points that borders the face.
// we only care about the first value (vertex index), the other twe values (separated by /)
// are not used on this example.
// e.g. `f 175/111/30 190/104/30 209/115/30`
private fun mapFaces(
    objContent: String,
    vertexes: List<Coordinate3D> // Assuming a list of normalized coordinates
): List<Face> {

    val faces = objContent.lines()
        .filter { it.startsWith("f ") }
        .map { faceLine ->
            val indices = faceLine.trim().split(" ").drop(1)
                .map { vertex ->
                    // Object uses index-1
                    // but my shape definitions keeps index-0 for simplicity
                    vertex.split("/")[0].toInt() - 1
                }
            // Add depth to the faces
            val averageZ = indices.map { index -> vertexes[index].z }.average().toFloat()

            Face(
                indexes = indices,
                color = lerp(start = Color.Blue, stop = Color.Red, fraction = averageZ)
            )
        }

    return faces
}