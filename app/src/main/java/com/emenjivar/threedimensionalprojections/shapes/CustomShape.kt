package com.emenjivar.threedimensionalprojections.shapes

class CustomShape(
    override val vertexes: List<Coordinate3D>,
    override val faces: List<Face>,
    override val edges: List<Edge>
) : Shape()