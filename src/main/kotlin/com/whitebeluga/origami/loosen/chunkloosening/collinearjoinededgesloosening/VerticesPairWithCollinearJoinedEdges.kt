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

package com.whitebeluga.origami.loosen.chunkloosening.collinearjoinededgesloosening

import com.moduleforge.libraries.geometry._3d.Vector
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.component.Edge
import com.whitebeluga.origami.figure.component.Vertex

/**
 * When there are two vertices in a figure and those are shared by at least a pair of collinear edges
 * and the faces that contain the respective vertices have no edges in common, then we ought to separate the vertices
 * in a direction perpendicular to the edges, and coplanar to the bundle.
 *
 * This is because it looks very unnatural for two collinear edges to remain together after the faces have been opened
 * alongside the depth of the bundle.
 *
 * This also true if there were only *one* instance of an edge that articulates two faces of a bundle, think we opens
 * those two faces to give depth, one of the ends would move outwards a little.
 *
 * However, simplifying, we can forget about that case, because if the are no collinear conjoined edges, but instead
 * a single flap, even we it's not opened outwards it doesn't look unnatural by itself.
 *
 * While the edges are a discerning criterion, we cannot uniquely identify objects of this class by them (as a vertex
 * can be shared by any number of collinear edges) and we have to use the vertices instead.
 *
 * An example of this are the inner edges in the waterbomb base or the two
 * legs of the bird base.
 *
 * A last VERY IMPORTANT thing to note is that this algorithm will return false positives, pairs of vertices that
 * ought to don't open outwards, but instead along the depth.
 *
 * This is not a problem because the user of this class will filter out those that, after depth-stretching,
 * are not together anymore. Which obviously doesn't represent a problem as far as realistic 3D rendering is concerned
 * since they will end up separated one way or another.
 */
internal class VerticesPairWithCollinearJoinedEdges
   private constructor(val vertex1: Vertex, val vertex2: Vertex,
                       /**
                        * The only reason we need these two edges is to calculate how much do the vertices need to be
                        * shifted, although we have two edges as parameters, there is only assurance that the number
                        * of vertices of the situation contemplated by this algorithm is exactly two, there is probably
                        * only two conjoined edges as well, even if there is not, taking any such pair for their length
                        * is a good approximation
                        */
                       val conjoinedEdge1: Edge, val conjoinedEdge2: Edge,
                       val vertex1ShiftDirection: Vector) {
   val vertex2ShiftDirection = vertex1ShiftDirection.negate()

   companion object {
      /**
       * Receives a pair of edges of a bundle.
       *
       * Checks the rest of conditions as explained in the class' javadoc and calculates the rest of information
       * of this object, such as the opening up direction.
       */
      fun tryToMakeVerticesPairFromEdges(edges: Pair<Edge, Edge>, bundle: Bundle): VerticesPairWithCollinearJoinedEdges? {
         val edge1 = edges.first
         val edge2 = edges.second
         val collinear = edge1.isCollinearWith(edge2)
         if (!collinear)
            return null
         if (!edge1.isJoinedTo(edge2))
            return null
         if (!edge1.overlaps(edge2))
            return null
         val joint = edge1.vertices.intersect(edge2.vertices).first()

         val vertex1 = (edge1.vertices - joint).first()
         val facesThatShareFirstVertex = bundle.faces.filter {it.vertices.contains(vertex1)}.toSet()
         val vertex2 = (edge2.vertices - joint).first()
         val facesThatShareSecondVertex = bundle.faces.filter {it.vertices.contains(vertex2)}.toSet()
         val edges1 = facesThatShareFirstVertex.flatMap { it.edges }.toSet()
         val edges2 = facesThatShareSecondVertex.flatMap { it.edges }.toSet()
         //it's better to check that the faces that share each vertex don't share any edge than to check
         // if the sets of faces overlap (some of them could overlap and we still want the vertices separated).
         // besides it's better and cleaner not to involve overlapping calculations and rely on the geometry of the figure
         if(edges1.intersect(edges2).isNotEmpty())
            return null

         val shiftVector = calculateShiftVector(edge1, bundle)
         return VerticesPairWithCollinearJoinedEdges(vertex1, vertex2, edge1, edge2, shiftVector)
      }
      private fun calculateShiftVector(edge: Edge, bundle: Bundle): Vector {
         // The face is used only to calculate the directions in which
         // the ends ought to be moved to separate them.
         // This is done by picking a vertex that is not on the collinear edge and that tells us
         // towards which side the vertex ought to move.
         val arbitraryFaceConnectedByEdge = bundle.facesConnectedBy(edge).first()
         val arbitraryVertex = arbitraryFaceConnectedByEdge.vertices.minus(edge.vertices).first()
         val closestToP = edge.line.closestPoint(arbitraryVertex)!!
         return closestToP.vectorTo(arbitraryVertex).normalize()
      }
   }
   override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false
      other as VerticesPairWithCollinearJoinedEdges
      return setOf(vertex1, vertex2) == setOf(other.vertex1, other.vertex2)
   }
   override fun hashCode(): Int = setOf(vertex1, vertex2).hashCode()
}
