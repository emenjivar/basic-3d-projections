package com.emenjivar.threedimensionalprojections.shapes

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Teapot : Shape() {

    private val segments = 16 // Number of segments around the teapot
    private val rings = 8 // Number of vertical rings

    override val name: String
        get() = "Teapot"

    override val vertexes: List<Coordinate3D>
        get() {
            val vertices = mutableListOf<Coordinate3D>()

            // Body of teapot (sphere-like)
            val bodyRadius = 0.4f
            val bodyHeight = 0.3f
            for (ring in 0..rings) {
                val v = ring.toFloat() / rings
                val theta = v * PI.toFloat()
                val y = cos(theta) * bodyHeight
                val r = sin(theta) * bodyRadius

                for (seg in 0..segments) {
                    val u = seg.toFloat() / segments
                    val phi = u * 2f * PI.toFloat()
                    val x = cos(phi) * r
                    val z = sin(phi) * r
                    vertices.add(Coordinate3D(x, y, z))
                }
            }

            // Spout (cone-like)
            val spoutBaseRadius = 0.05f
            val spoutTipRadius = 0.02f
            val spoutLength = 0.4f
            val spoutSegments = 8
            val spoutRings = 4

            for (ring in 0..spoutRings) {
                val t = ring.toFloat() / spoutRings
                val x = bodyRadius + t * spoutLength
                val radius = spoutBaseRadius + t * (spoutTipRadius - spoutBaseRadius)
                val y = 0.1f - t * 0.15f // Slight downward curve

                for (seg in 0..spoutSegments) {
                    val angle = (seg.toFloat() / spoutSegments) * 2f * PI.toFloat()
                    val dy = cos(angle) * radius
                    val dz = sin(angle) * radius
                    vertices.add(Coordinate3D(x, y + dy, dz))
                }
            }

            // Handle (torus arc)
            val handleSegments = 8
            val handleRings = 8
            val handleMajorRadius = 0.25f
            val handleMinorRadius = 0.03f

            for (ring in 0..handleRings) {
                val u = ring.toFloat() / handleRings
                val theta = u * PI.toFloat() // Half circle
                val x = -bodyRadius - cos(theta) * handleMajorRadius
                val y = sin(theta) * handleMajorRadius

                for (seg in 0..handleSegments) {
                    val v = seg.toFloat() / handleSegments
                    val phi = v * 2f * PI.toFloat()
                    val dx = cos(phi) * handleMinorRadius
                    val dy = sin(phi) * handleMinorRadius
                    vertices.add(Coordinate3D(x + dx, y + dy, 0f))
                }
            }

            // Lid (flat disc with knob)
            val lidRadius = 0.35f
            val lidRings = 3
            for (ring in 0..lidRings) {
                val r = (ring.toFloat() / lidRings) * lidRadius
                val y = bodyHeight + 0.05f

                for (seg in 0..segments) {
                    val angle = (seg.toFloat() / segments) * 2f * PI.toFloat()
                    val x = cos(angle) * r
                    val z = sin(angle) * r
                    vertices.add(Coordinate3D(x, y, z))
                }
            }

            // Knob on lid (small sphere)
            val knobRadius = 0.08f
            val knobRings = 4
            for (ring in 0..knobRings) {
                val v = ring.toFloat() / knobRings
                val theta = v * PI.toFloat()
                val y = bodyHeight + 0.05f + cos(theta) * knobRadius
                val r = sin(theta) * knobRadius

                for (seg in 0..segments) {
                    val u = seg.toFloat() / segments
                    val phi = u * 2f * PI.toFloat()
                    val x = cos(phi) * r
                    val z = sin(phi) * r
                    vertices.add(Coordinate3D(x, y, z))
                }
            }

            return vertices
        }

    override val faces: List<Face>
        get() {
            val facesList = mutableListOf<Face>()
            var vertexOffset = 0

            // Body faces
            val bodyVertexCount = (segments + 1) * (rings + 1)
            for (ring in 0 until rings) {
                for (seg in 0 until segments) {
                    val i0 = vertexOffset + ring * (segments + 1) + seg
                    val i1 = i0 + segments + 1
                    val i2 = i1 + 1
                    val i3 = i0 + 1

                    facesList.add(Face(
                        indexes = listOf(i0, i1, i2, i3),
                        color = Color(0xFF8B4513) // Brown
                    ))
                }
            }
            vertexOffset += bodyVertexCount

            // Spout faces
            val spoutSegments = 8
            val spoutRings = 4
            val spoutVertexCount = (spoutSegments + 1) * (spoutRings + 1)
            for (ring in 0 until spoutRings) {
                for (seg in 0 until spoutSegments) {
                    val i0 = vertexOffset + ring * (spoutSegments + 1) + seg
                    val i1 = i0 + spoutSegments + 1
                    val i2 = i1 + 1
                    val i3 = i0 + 1

                    facesList.add(Face(
                        indexes = listOf(i0, i1, i2, i3),
                        color = Color(0xFF8B4513)
                    ))
                }
            }
            vertexOffset += spoutVertexCount

            // Handle faces
            val handleSegments = 8
            val handleRings = 8
            val handleVertexCount = (handleSegments + 1) * (handleRings + 1)
            for (ring in 0 until handleRings) {
                for (seg in 0 until handleSegments) {
                    val i0 = vertexOffset + ring * (handleSegments + 1) + seg
                    val i1 = i0 + handleSegments + 1
                    val i2 = i1 + 1
                    val i3 = i0 + 1

                    facesList.add(Face(
                        indexes = listOf(i0, i1, i2, i3),
                        color = Color(0xFF8B4513)
                    ))
                }
            }
            vertexOffset += handleVertexCount

            // Lid faces
            val lidRings = 3
            val lidVertexCount = (segments + 1) * (lidRings + 1)
            for (ring in 0 until lidRings) {
                for (seg in 0 until segments) {
                    val i0 = vertexOffset + ring * (segments + 1) + seg
                    val i1 = i0 + segments + 1
                    val i2 = i1 + 1
                    val i3 = i0 + 1

                    facesList.add(Face(
                        indexes = listOf(i0, i1, i2, i3),
                        color = Color(0xFF8B4513)
                    ))
                }
            }
            vertexOffset += lidVertexCount

            // Knob faces
            val knobRings = 4
            for (ring in 0 until knobRings) {
                for (seg in 0 until segments) {
                    val i0 = vertexOffset + ring * (segments + 1) + seg
                    val i1 = i0 + segments + 1
                    val i2 = i1 + 1
                    val i3 = i0 + 1

                    facesList.add(Face(
                        indexes = listOf(i0, i1, i2, i3),
                        color = Color(0xFF8B4513)
                    ))
                }
            }

            return facesList
        }
}

val TeapotInstance = Teapot()