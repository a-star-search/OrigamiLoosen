/*
 *    This file is part of "Origami".
 *
 *     Origami is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Origami is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Origami.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Copyright (c) 2018.  This file is subject to the terms and conditions defined in file 'LICENSE.txt', which is part of this source code package.
 */

package com.whitebeluga.origami.loosen

import com.moduleforge.libraries.geometry._3d.ColorCombination
import com.moduleforge.libraries.geometry._3d.Plane
import com.moduleforge.libraries.geometry._3d.Point
import com.moduleforge.libraries.geometry._3d.Polygon
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Face.Companion.faceFromPoints
import com.whitebeluga.origami.figure.component.Vertex
import java.awt.Color

/**
 * An opened up figure doesn't necessarily have proper polygons as faces, as faces of more than three vertices may not
 * share the same plane, being somewhat deformed.
 *
 * Admittedly this is a leakage from both the library used in the front end (babylon js, in which it's
 * trivial to draw deformed non planar polygons) and the stretching algorithm that may easily create deformed faces.
 *
 * But it's not like the actual faces of an origami figure aren't always slightly deformed too.
 *
 * It is NOT created with the intent of giving the face a more realistic curved shape.
 *
 * The application is not so sophisticated to render such deformed surfaces.
 *
 * The deformation of the faces may only be small (this is guaranteed because opening up a figure only shifts the faces
 * slightly enough to be perceived with volume by the eye)
 *
 * The reason is that it is necessary to determine the plane of the face and its orientation (given by the order of the
 * points). The orientation is needed in order to differentiate the colors at each side.
 *
 * The plane and its direction may be calculated using any reasonable method such as taking the first three points
 * of the face (keep in mind that the polygon is convex, it is always an actual face of the figure, not a visible
 * part of it such as the faces in the flat rendering of the figure, so we could take any three points
 * for an approximation of the direction of the normal established by the point order)
 *
 */
class DeformableFace(val vertices: List<Point>, val colors: ColorCombination) {
   val frontColor: Color
      get() = colors.front
   val backColor: Color
      get() = colors.back

//   val plane: Plane
//      get() {
//         //taking three vertices is enough and, of course, always guarantees they belong in the same plane.
//         val pol = Polygon(vertices.take(3))
//         return pol.polygonPlane
//      }

   /**
    * An approximation of this face with straight faces.
    *
    * If all the vertices of the faces are on the same plane, then a set of a single polygon is returned.
    *
    * Otherwise the face could be triangulated. There is no promise as to whether it breaks this object into
    * the minimum possible number of flat faces, or the specific way of breaking it up into faces,
    * but no new points are created outside of the vertices positions.
    *
    * That is, the maximum possible number of faces is given by its triangulation, which is (number of vertices - 2)
    *
    * In the faces no duplicated points at a given position are created either. Two faces that share an edge, share
    * the same vertex objects.
    *
    */
   fun asStraightFaces(): Set<Face> {
      if(vertices.size == 3)
         return setOf(faceFromPoints(vertices, colors))
      val plane = planeOfFirstThreeVertices()
      val allPointsInSamePlane = vertices.all { plane.contains(it) }
      return if(allPointsInSamePlane)
            setOf(faceFromPoints(vertices, colors))
         else triangulateVerticesOfDeformedFace(vertices, colors)
   }
   private fun planeOfFirstThreeVertices(): Plane {
      val pol = Polygon(vertices.take(3))
      return pol.polygonPlane
   }
   companion object {
      fun deformableFaceFrom(face: Face): DeformableFace = DeformableFace(face.vertices, face.colors)
      /**
       * A triangle is always flat, in order to have flat faces after stretching the vertices, we break the
       * faces of more than three vertices into triangles.
       */
      fun triangulateVerticesOfDeformedFace(points: List<Point>, colors: ColorCombination): Set<Face> {
         val facesNoColor = triangulateVerticesOfDeformedFace(points)
         return facesNoColor.map { Face(it.vertices, colors)}.toSet()
      }
      private fun triangulateVerticesOfDeformedFace(points: List<Point>): Set<Face> {
         val vertices = points.map { Vertex(it)}
         return triangulateVerticesOfDeformedFace2(vertices)
      }
      private fun triangulateVerticesOfDeformedFace2(vertices: List<Vertex>): Set<Face> =
              if(vertices.size == 3)
                 setOf(Face(vertices))
              else
                 triangulateWhenMoreThanThreeSides(vertices)
      private fun triangulateWhenMoreThanThreeSides(vertices: List<Vertex>): Set<Face> =
              triangulateRecursive(vertices, emptySet(), 1)
      private fun triangulateRecursive(vertices: List<Vertex>, facesSoFar: Set<Face>, index: Int): Set<Face> {
         val noMoreTriangles = index == vertices.lastIndex
         if(noMoreTriangles)
            return facesSoFar
         val newVertices = listOf(vertices.first(), vertices[index], vertices[index + 1])
         return triangulateRecursive(vertices, facesSoFar + Face(newVertices), index + 1)
      }
   }
}