package com.emenjivar.threedimensionalprojections.shapes

class CustomShape(
    override val name: String,
    override val vertexes: List<Coordinate3D>,
    override val faces: List<Face>
) : Shape()