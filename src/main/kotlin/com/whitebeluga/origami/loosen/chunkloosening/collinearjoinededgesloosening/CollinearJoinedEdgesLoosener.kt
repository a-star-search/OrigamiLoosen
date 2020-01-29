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

import com.google.common.annotations.VisibleForTesting
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.component.Edge
import com.whitebeluga.origami.figure.component.Vertex
import com.whitebeluga.origami.loosen.Constants.DEFAULT_OPEN_UP_SHIFT_RATIO
import com.whitebeluga.origami.loosen.chunkloosening.collinearjoinededgesloosening.VerticesPairWithCollinearJoinedEdges.Companion.tryToMakeVerticesPairFromEdges

/**
 * An example of collinear joined edges are the two inner edges of a waterbomb base.
 *
 * If we stretch the bundle to give it depth those segments are still going to be together when they shouldn't, because
 * they exist side to side and the faces may not overlap, even if they overlap, the vertices must be opened towards the
 * sides.
 */
internal object CollinearJoinedEdgesLoosener {
   const val COLLINEAR_EDGES_OPEN_UP_RATIO = DEFAULT_OPEN_UP_SHIFT_RATIO / 3.0

   /**
    * This function uses the original bundle to find the collinear segments but then applies the
    * transformation to open them up to the mapping of vertex that are the output of different stretching algorithms
    *
    * It returns an updated map of vertices.
    */
   fun openUpEdges(bundle: Bundle, vertexToStretched: Map<Vertex, Vertex>): Map<Vertex, Vertex> {
      val collinearJoinedEdges = findVerticesPairWithCollinearJoinedEdges(bundle)
      val filtered = filterThoseThatRemainCollinearAfterStretching(collinearJoinedEdges, vertexToStretched)
      return openUpCollinearEdges(filtered, vertexToStretched)
   }
   @VisibleForTesting
   internal fun findVerticesPairWithCollinearJoinedEdges(bundle: Bundle): Set<VerticesPairWithCollinearJoinedEdges> {
      val edgePairs = calculateAllEdgesCombinations(bundle)
      return edgePairs.mapNotNull { edges -> tryToMakeVerticesPairFromEdges(edges, bundle) }.toSet()
   }
   /**
    * Needs extra memory by using this method but makes code more legible.
    */
   private fun calculateAllEdgesCombinations(bundle: Bundle): Set<Pair<Edge, Edge>> {
      val result = mutableSetOf<Pair<Edge, Edge>>()
      val edges = bundle.edges.toList()
      for ((index, edge) in edges.dropLast(1).withIndex()) {
         val restOfEdges = edges.drop(index + 1)
         for (edge2 in restOfEdges)
            result.add(Pair(edge, edge2))
      }
      return result
   }
   @VisibleForTesting
   internal fun filterThoseThatRemainCollinearAfterStretching(pairs: Set<VerticesPairWithCollinearJoinedEdges>,
                                                              vertexToStretched: Map<Vertex, Vertex>):
           Set<VerticesPairWithCollinearJoinedEdges> =
           pairs.filter { stretchedRemainCollinear(it, vertexToStretched) }.toSet()
   private fun stretchedRemainCollinear(pair: VerticesPairWithCollinearJoinedEdges,
                                        originalToStretched: Map<Vertex, Vertex>): Boolean {
      val stretchedEdgeVertices1 = pair.conjoinedEdge1.vertices.map { originalToStretched[it]!!}
      val stretchedEdge1 = Edge(stretchedEdgeVertices1[0], stretchedEdgeVertices1[1])
      val stretchedEdgeVertices2 = pair.conjoinedEdge2.vertices.map { originalToStretched[it]!!}
      val stretchedEdge2 = Edge(stretchedEdgeVertices2[0], stretchedEdgeVertices2[1])
      return stretchedEdge1.isCollinearWith(stretchedEdge2)
   }
   private fun openUpCollinearEdges(collinear: Set<VerticesPairWithCollinearJoinedEdges>, stretched: Map<Vertex, Vertex>):
           Map<Vertex, Vertex>{
      val result = mutableMapOf<Vertex, Vertex>()
      collinear.forEach {
         val pair = openUpCollinearEdge(it, stretched)
         result[it.vertex1] = pair.first
         result[it.vertex2] = pair.second
      }
      return result
   }
   /**
    * returns the new positions of the free ends of the collinear joined edges
    */
   private fun openUpCollinearEdge(joined: VerticesPairWithCollinearJoinedEdges, stretched: Map<Vertex, Vertex>): Pair<Vertex, Vertex>{
      //for small values: angle ≈ sin(angle)
      val freeEnd1Separation = COLLINEAR_EDGES_OPEN_UP_RATIO * joined.conjoinedEdge1.length()
      val vector1 = joined.vertex1ShiftDirection.withLength(freeEnd1Separation)
      val freeEnd1 = stretched[joined.vertex1]!!
      val newFreeEnd1 = Vertex(freeEnd1.translate(vector1))

      val freeEnd2Separation = COLLINEAR_EDGES_OPEN_UP_RATIO * joined.conjoinedEdge2.length()
      val vector2 = joined.vertex2ShiftDirection.withLength(freeEnd2Separation)
      val freeEnd2 = stretched[joined.vertex2]!!
      val newFreeEnd2 = Vertex(freeEnd2.translate(vector2))

      return Pair(newFreeEnd1, newFreeEnd2)
   }
}