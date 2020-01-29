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

import com.moduleforge.libraries.geometry._3d.Line
import com.moduleforge.libraries.geometry._3d.Line.linePassingBy
import com.moduleforge.libraries.geometry._3d.Vector
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Bundle.Side.TOP
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import com.whitebeluga.origami.loosen.chunkloosening.Chunk
import com.whitebeluga.origami.loosen.chunkloosening.ChunkBoundary
import com.whitebeluga.origami.loosen.chunkloosening.openingup.RelativePosition.ABOVE
import com.whitebeluga.origami.loosen.chunkloosening.openingup.RelativePosition.BELOW

/**
 * Internal chunks (as opposed to interbundle chunks) are the ones that originate new rotations.
 *
 * Consider that interbundle chunks do not need to be "opened". They already are, by definition.
 *
 * What this class produces are pre-rotation information: the boundary around which the chunk rotates and
 *
 */
internal class InternalChunkPreRotationCalculator(
        private val chunk: Chunk,
        parentChunk: Chunk,
        private val chunkToRotatedBundle: Map<Chunk, Bundle>,
        private val vertexToRotatedVertex: Map<Vertex, Vertex>) {
   private val bundle = chunk.bundle
   /**
    * Side towards which the chunk must be opened with respect to its parent bundle.
    */
   private val openingSide: Bundle.Side
   private val unrotatedBoundaryWithParent: ChunkBoundary
   private val rotatedParentBundle = chunkToRotatedBundle[parentChunk]!!
   init {
      val internalBoundaryWithParent = parentChunk.internallyConnectedChunks[chunk]!!
      openingSide = internalBoundaryWithParent.first
      unrotatedBoundaryWithParent = internalBoundaryWithParent.second
   }
   fun calculateRotation(): PreRotation {
      val rotationLine = calculateLineOfRotatedInternalBoundary(unrotatedBoundaryWithParent)
      return calculateRotation(rotationLine, rotatedParentBundle)
   }
   private fun calculateLineOfRotatedInternalBoundary(unrotatedInternalBoundary: ChunkBoundary): Line {
      val randomEdge = unrotatedInternalBoundary.edges.first()
      val verticesOfEdge = randomEdge.vertices
      /*
      there might have been no rotations thus far, in which case, we can simply use the values of the unrotated
      boundary
       */
      val rotatedEdgeEnd1 = vertexToRotatedVertex[verticesOfEdge[0]] ?: verticesOfEdge[0]
      val rotatedEdgeEnd2 = vertexToRotatedVertex[verticesOfEdge[1]] ?: verticesOfEdge[1]
      return linePassingBy(rotatedEdgeEnd1, rotatedEdgeEnd2)
   }
   private fun calculateRotation(rotationLine: Line, rotatedParentBundle: Bundle): PreRotation {
      val rotationDirection = calculateRotationDirection(rotatedParentBundle)
      val facesAboveAndBelow = calculateFacesThatShouldBeAboveAndBelow()
      return PreRotation(rotationLine, rotationDirection,
              openedUpFacesThatShouldBeAbove = facesAboveAndBelow.first,
              openedUpFacesThatShouldBeBelow = facesAboveAndBelow.second)
   }
   private fun calculateRotationDirection(rotatedParentBundle: Bundle): Vector =
      if(openingSide == TOP)
         rotatedParentBundle.normal
      else
         rotatedParentBundle.normal.negate()
   /**
    * These are the rotated faces whose relative position to the chunk that we are attempting to rotate must
    * be respected
    */
   private fun calculateFacesThatShouldBeAboveAndBelow(): Pair<Set<Face>, Set<Face>> {
      val facesOfAlreadyRotatedChunksThatShouldBeAbove = facesOfAlreadyRotatedChunks(ABOVE)
      val facesOfAlreadyRotatedChunksThatShouldBeBelow = facesOfAlreadyRotatedChunks(BELOW)
      return Pair(facesOfAlreadyRotatedChunksThatShouldBeAbove, facesOfAlreadyRotatedChunksThatShouldBeBelow)
   }
   private fun facesOfAlreadyRotatedChunks(position: RelativePosition): Set<Face> {
      val map = if(openingSide == TOP) {
         if(position == ABOVE)
            bundle.facesToFacesAbove
         else
            bundle.facesToFacesBelow
      } else {
         if(position == ABOVE)
            bundle.facesToFacesBelow
         else
            bundle.facesToFacesAbove
      }
      val facesOfChunk = chunk.faces
      val facesOfBundleWhoseRelativePositionMustBeKept =
              facesOfChunk.flatMap { map[it] ?: emptySet() }.toSet() - facesOfChunk
      return findAlreadyRotatedChunkFacesWhoseRelativePositionMustBeKept(facesOfBundleWhoseRelativePositionMustBeKept)
   }
   private fun findAlreadyRotatedChunkFacesWhoseRelativePositionMustBeKept(
           facesWithWhichPositionHasToBeRespected: Set<Face>): Set<Face> {
      if(facesWithWhichPositionHasToBeRespected.isEmpty())
         return setOf()
      val originalChunksThatHaveBeenRotated = chunkToRotatedBundle.keys
      val originalChunksThatHaveBeenRotated_AndWithWhichPositionHasToBeRespected =
              originalChunksThatHaveBeenRotated
                 .filter { it.faces.intersect(facesWithWhichPositionHasToBeRespected).isNotEmpty() }
                 .toSet()
      return originalChunksThatHaveBeenRotated_AndWithWhichPositionHasToBeRespected
                 .flatMap { chunkToRotatedBundle[it]!!.faces }
                 .toSet()
   }
}