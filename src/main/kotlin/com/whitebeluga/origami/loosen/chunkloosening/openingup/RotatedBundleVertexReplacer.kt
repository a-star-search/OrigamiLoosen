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

package com.whitebeluga.origami.loosen.chunkloosening.openingup

import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex

/**
 * Those faces of the bundle that had vertices in common (before rotation) with the parent bundle,
 * are included in the map returned with the vertices replaced.
 *
 * If the bundle was indeed rotated, or any ancestor was rotated then for sure there are vertices that need
 * to be replaced: all those at the boundary. And, incidentally, only those, since the figure is opened up
 * traversing the tree and if a chunk is connected to an already rotated chunk that can only be the parent.
 *
 * Otherwise the face's vertices are not included in the map. In other words, the map only contains those faces
 * with vertices for which one or more vertex replacement were done.
 */
internal class RotatedBundleVertexReplacer(
        private val rotated: Bundle,
        private val vertexToRotatedOfRotatedBundle: Map<Vertex, Vertex>,
        private val vertexToRotatedOfAlreadyRotatedBundles: Map<Vertex, Vertex>) {
   val faces = rotated.faces
   fun replaceVertices(): Bundle {
      val faceToNewFace = replaceVerticesInFaces()
      val newFaces = faces.map { faceToNewFace[it] ?: it }.toSet()
      val faceToFacesAbove = rotated.facesToFacesAbove
      val newFaceToFacesAbove = faceToFacesAbove.map { (face, facesAbove) ->
         (faceToNewFace[face] ?: face) to facesAbove.map {  faceToNewFace[it] ?: it }.toSet()
      }.toMap()
      return Bundle(rotated.plane, newFaces, newFaceToFacesAbove)
   }
   private fun replaceVerticesInFaces(): Map<Face, Face> {
      val rotatedToOriginal = vertexToRotatedOfRotatedBundle.entries.associateBy({ it.value }) { it.key }
      return replaceVerticesInFaces(rotatedToOriginal)
   }
   private fun replaceVerticesInFaces(rotatedToOriginal: Map<Vertex, Vertex>): Map<Face, Face> {
      val replaceableVertices = vertexToRotatedOfAlreadyRotatedBundles.keys
      val faceToNewFace = mutableMapOf<Face, Face>()
      for(face in faces) {
         val vertices = face.vertices
         val originalVertices = vertices.map {rotatedToOriginal[it]!!}
         val thereAreNoVerticesToReplace = replaceableVertices.intersect(originalVertices).isEmpty()
         if(thereAreNoVerticesToReplace)
            continue
         val newVertices = replaceVerticesOfFace(vertices, originalVertices)
         faceToNewFace[face] = Face(newVertices, face.colors)
      }
      return faceToNewFace
   }
   private fun replaceVerticesOfFace(rotatedVertices: List<Vertex>, originalVertices: List<Vertex>): List<Vertex> =
           rotatedVertices.withIndex().map {
              (index, rotatedVertex) ->
              val originalVertex = originalVertices[index]
              vertexToRotatedOfAlreadyRotatedBundles[originalVertex] ?: rotatedVertex
           }
}