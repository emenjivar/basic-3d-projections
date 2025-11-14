package com.emenjivar.threedimensionalprojections.shapes

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

private val ShrekGreen = Color(0xFF5B8A0D) // Verde Oliva Profundo
private val EarGreen = Color(0xFF82B1FF)   // Un verde ligeramente diferente

class ShrekAbstract : Shape() {

    override val name: String
        get() = "Shrek"

    // --- Componente 1: Cabeza (Cápsula Cónica, 24 vértices) ---
    private val segments = 8 // Usamos 8 segmentos por círculo
    private val radiusTop = 0.4f // Parte superior más estrecha
    private val radiusMid = 0.6f // Parte media más ancha
    private val radiusBottom = 0.5f // Mandíbula ligeramente más estrecha
    private val height = 0.8f // Altura total (Eje Y)

    // Vértices 0-7: Círculo Superior (Z=0)
    private val topCircle = createCircleVertices(radiusTop, z = height/2, segments, yOffset = 0f)
    // Vértices 8-15: Círculo Medio (Z=0.4)
    private val midCircle = createCircleVertices(radiusMid, z = 0f, segments, yOffset = 0f)
    // Vértices 16-23: Círculo Inferior (Z=0.8)
    private val bottomCircle = createCircleVertices(radiusBottom, z = -height/2, segments, yOffset = 0f)

    // --- Componente 2 & 3: Orejas (Prismas Cónicos, 8 vértices) ---
    private val earPivotZ = 0.35f // Altura en el lado de la cabeza
    private val earPivotX = radiusMid * 0.9f // Posición lateral (en el punto más ancho)

    private fun createEarVertices(isRight: Boolean): List<Coordinate3D> {
        val sign = if (isRight) 1f else -1f
        val xBase = sign * earPivotX
        val zBase = earPivotZ // Eje Z es la altura
        val ySpread = 0.15f // Separación en profundidad
        val tipZ = 0.95f // Elevación de la punta (más alta que la cabeza)

        // La forma de la oreja es una base plana con un punto cónico superior, con una pequeña rotación
        val tipX = xBase * 1.1f // La punta se extiende hacia afuera

        return listOf(
            // Base Triangular (0, 1) y un vértice de la cabeza (2) para anclar
            Coordinate3D(x = xBase, y = -ySpread/2, z = zBase),   // 0: Base-Left (cerca de la cabeza)
            Coordinate3D(x = xBase, y = ySpread/2, z = zBase),    // 1: Base-Right
            Coordinate3D(x = xBase * 0.9f, y = 0f, z = zBase * 0.9f), // 2: Vértice extra para la cara
            // Punta del cono (3)
            Coordinate3D(x = tipX, y = 0f, z = tipZ),             // 3: Punta
        )
    }

    private val leftEarVertices = createEarVertices(isRight = false) // Índices 24-27
    private val rightEarVertices = createEarVertices(isRight = true)  // Índices 28-31
    private val earVertexCount = leftEarVertices.size // 4


    // UTILITY: Auxiliar para crear círculos
    private fun createCircleVertices(
        radius: Float,
        z: Float,
        segments: Int,
        yOffset: Float = 0f
    ): List<Coordinate3D> {
        return (0 until segments).map { i ->
            val angle = Math.toRadians((i * 360 / segments).toDouble()).toFloat()
            Coordinate3D(
                x = radius * cos(angle),
                y = radius * sin(angle) + yOffset, // El Y es ahora la profundidad/espalda
                z = z
            )
        }
    }


    // ** 1. VERTEXES (Combinación total: 8+8+8+4+4 = 32 vértices) **

    override val vertexes: List<Coordinate3D>
        get() = topCircle + midCircle + bottomCircle + leftEarVertices + rightEarVertices

    // ** 2. FACES (Ensamblaje por Componente) **

    override val faces: List<Face>
        get() {
            val faces = mutableListOf<Face>()

            // A. CARAS CÓNICAS SUPERIORES (Une topCircle (0-7) con midCircle (8-15))
            for (i in 0 until segments) {
                val next = (i + 1) % segments
                // Cuadrilátero que conecta los dos círculos.
                faces.add(
                    Face(
                        indexes = listOf(i, next, next + segments, i + segments),
                        color = ShrekGreen
                    )
                )
            }

            // B. CARAS CÓNICAS INFERIORES (Une midCircle (8-15) con bottomCircle (16-23))
            val midOffset = segments // 8
            val botOffset = segments * 2 // 16
            for (i in 0 until segments) {
                val next = (i + 1) % segments
                // Cuadrilátero que conecta los dos círculos inferiores.
                faces.add(
                    Face(
                        indexes = listOf(i + midOffset, next + midOffset, next + botOffset, i + botOffset),
                        color = ShrekGreen
                    )
                )
            }

            // C. TAPAS (Top y Bottom) - Opcional, pero las agregamos para cerrar el modelo
            // faces.add(Face(indexes = topCircle.indices.toList(), color = ShrekGreen)) // Tapa superior
            // faces.add(Face(indexes = bottomCircle.indices.map { it + botOffset }.toList(), color = ShrekGreen)) // Tapa inferior


            // D. CARAS DE LA OREJA IZQUIERDA (Índices 24-27)
            val leftEarOffset = topCircle.size + midCircle.size + bottomCircle.size // 24

            // El cono se forma con 3 triángulos que van de la base (0, 1, 2) a la punta (3)
            val leftEarFaces = listOf(
                Face(indexes = listOf(0, 1, 3), color = EarGreen),
                Face(indexes = listOf(1, 2, 3), color = EarGreen),
                Face(indexes = listOf(2, 0, 3), color = EarGreen),
            )
            faces.addAll(leftEarFaces.map { face ->
                Face(indexes = face.indexes.map { it + leftEarOffset }, color = face.color)
            })

            // E. CARAS DE LA OREJA DERECHA (Índices 28-31)
            val rightEarOffset = leftEarOffset + earVertexCount // 24 + 4 = 28

            val rightEarFaces = listOf(
                Face(indexes = listOf(0, 1, 3), color = EarGreen),
                Face(indexes = listOf(1, 2, 3), color = EarGreen),
                Face(indexes = listOf(2, 0, 3), color = EarGreen),
            )
            faces.addAll(rightEarFaces.map { face ->
                Face(indexes = face.indexes.map { it + rightEarOffset }, color = face.color)
            })

            return faces
        }
}

val ShrekInstance = ShrekAbstract()
