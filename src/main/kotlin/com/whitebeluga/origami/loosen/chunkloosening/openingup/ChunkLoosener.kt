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

import com.google.common.annotations.VisibleForTesting
import com.moduleforge.libraries.geometry.Geometry.almostZero
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.component.Vertex
import com.whitebeluga.origami.figure.folding.rotating.bundlerotation.BundleRotator.Companion.makeRotator
import com.whitebeluga.origami.loosen.Constants.DEFAULT_OPEN_UP_ANGLE
import com.whitebeluga.origami.loosen.chunkloosening.Chunk
import com.whitebeluga.origami.loosen.chunkloosening.chunktreemaking.ChunkTreeMaker
import com.whitebeluga.origami.loosen.chunkloosening.openingup.RelativePosition.ABOVE
import java.lang.Math.PI

/**
 * Opens up the chunks of the figure
 */
internal class ChunkLoosener(val figure: Figure) {
   /**
    * As we rotate the different bundles, keep track of the original vertices and their rotations with this map.
    *
    * This is to avoid duplicating vertices at boundaries. That is, when rotating two bundles that are joined
    * by a boundary the vertices at said boundary ought to be the same objects.
    */
   private val mapOfVertexToRotatedVertex: MutableMap<Vertex, Vertex> = mutableMapOf()
   private val mapOfChunkToRotatedBundle: MutableMap<Chunk, Bundle> = mutableMapOf()
   val loosenedFigure: Figure

   init {
      val chunkTreeMaker = ChunkTreeMaker(figure)
      val root = chunkTreeMaker.makeChunkTree()
      val isJustOneChunk = root.isLeaf()
      loosenedFigure =
         if(isJustOneChunk) {
            figure
         } else {
            val loosened = makeLoosenedBundles(root)
            Figure(loosened)
         }
   }
   private fun makeLoosenedBundles(root: Chunk): Set<Bundle> {
      initMapsFromRootChunk(root)
      makeBundlesOfChildren(root)
      return mapOfChunkToRotatedBundle.values.toSet()
   }
   private fun initMapsFromRootChunk(root: Chunk) {
      val rootAsBundle = root.makeBundle()
      mapOfChunkToRotatedBundle[root] = rootAsBundle
      val initialVertexToRotated = rootAsBundle.vertices.map { it to it }
      mapOfVertexToRotatedVertex.putAll(initialVertexToRotated)
   }
   private fun makeBundlesOfChildren(chunk: Chunk, rotations: List<Rotation> = listOf()) {
      if(chunk.isLeaf())
         return
      makeBundlesOfInternalChildren(chunk, rotations)
      makeBundlesOfInterBundleChildren(chunk, rotations)
   }
   private fun makeBundlesOfInternalChildren(chunk: Chunk, rotations: List<Rotation>) =
           chunk.internallyConnectedChunks.forEach { (child, _ )->
                      makeBundleOfInternalChunk(child, chunk, rotations)
                   }
   private fun makeBundleOfInternalChunk(chunk: Chunk, parentChunk: Chunk, rotationsOfAncestors: List<Rotation>) {
      val rotationCalculator = InternalChunkPreRotationCalculator(chunk, parentChunk,
              mapOfChunkToRotatedBundle, mapOfVertexToRotatedVertex)
      val chunkPreRotation = rotationCalculator.calculateRotation()
      val chunkRotation = calculateRotation(chunk, rotationsOfAncestors, chunkPreRotation)
      val rotations = if(chunkRotation == null)
            rotationsOfAncestors
         else
            rotationsOfAncestors + chunkRotation
      makeBundleFromSubtree(chunk, rotations)
   }
   /**
    * If there are conflicts so that the chunk cannot be opened at any angle, then null is returned
    */
   private fun calculateRotation(chunk: Chunk, rotationsOfAncestors: List<Rotation>, preRotation: PreRotation): Rotation? {
      val canBeFreelyOpened = preRotation.openedUpFacesThatShouldBeAbove.isEmpty() &&
                      preRotation.openedUpFacesThatShouldBeBelow.isEmpty()
      if(canBeFreelyOpened)
         return Rotation(preRotation.line, preRotation.rotationDirection, DEFAULT_OPEN_UP_ANGLE)
      val angle = calculateRotationAngleWhenPossibleConflicts(chunk, rotationsOfAncestors, preRotation)
      return if(almostZero(angle))
            null
         else Rotation(preRotation.line, preRotation.rotationDirection, angle)
   }

   private fun calculateRotationAngleWhenPossibleConflicts(chunk: Chunk, rotationsOfAncestors: List<Rotation>,
                                                      preRotation: PreRotation): Double {
      var angle = DEFAULT_OPEN_UP_ANGLE
      val increment = DEFAULT_OPEN_UP_ANGLE / 10.0
      val maxAngle = PI / 5.0
      while(angle > 0 && angle < maxAngle) {
         val conflictResolver = ConflictResolver(preRotation, chunk, rotationsOfAncestors, angle)
         val conflicts = conflictResolver.conflicts()
         val noConflicts = conflicts.isEmpty()
         if(noConflicts)
            return angle
         if(conflicts.size == 2)
            /* there is conflict that I don't know how to handle, this can happen because our algorithm is
             an imperfect heuristic, in this case we'll just return the current rotation and be done with it.
             */
            return angle
         val conflict = conflicts.first()
         if(conflict == ABOVE)
            angle -= increment
         else
            angle += increment
      }
      return angle //conflicts still exist, return either the minimum or the maximum
   }
   private fun makeBundlesOfInterBundleChildren(chunk: Chunk, rotations: List<Rotation>) =
           chunk.interBundleConnectedChunks.forEach { (child, _)->
              makeBundleFromSubtree(child, rotations)
           }
   /**
    * 1. Makes a new bundle
    * 2. Updates the bundle map and the vertex map.
    * 3. Recursively make bundle of children
    */
   private fun makeBundleFromSubtree(chunk: Chunk, rotations: List<Rotation>) {
      makeBundleFromChunk(chunk, rotations)
      makeBundlesOfChildren(chunk, rotations)
   }
   private fun makeBundleFromChunk(chunk: Chunk, rotations: List<Rotation>) {
      if (rotations.isEmpty()) {
         mapOfChunkToRotatedBundle[chunk] = chunk.makeBundle()
      } else {
         val rotatedBundle = rotateChunk(chunk, rotations)
         mapOfChunkToRotatedBundle[chunk] = rotatedBundle.first
         updateMapOfVertexToRotated(rotatedBundle.second)
      }
   }
   /**
    * Rotates a chunk and makes a bundle where existing vertices have been replaced.
    * Returns the bundle and the map of vertex to rotated vertex
    */
   private fun rotateChunk(chunk: Chunk, rotations: List<Rotation>): Pair<Bundle, Map<Vertex, Vertex>> {
      val rotated = rotate(chunk, rotations)
      val rotatedBundle = rotated.first
      val originalVerticesToRotated = rotated.second
      val vertexReplacer = RotatedBundleVertexReplacer(rotatedBundle, originalVerticesToRotated, mapOfVertexToRotatedVertex)
      val rotatedAndBoundaryVerticesReplaced = vertexReplacer.replaceVertices()
      val verticesOfNewBundle = rotatedAndBoundaryVerticesReplaced.vertices
      val originalVerticesToRotatedMinusReplaced =
              originalVerticesToRotated.filterValues { verticesOfNewBundle.contains(it) }
      return Pair(rotatedAndBoundaryVerticesReplaced, originalVerticesToRotatedMinusReplaced)
   }
   private fun updateMapOfVertexToRotated(newVertexToRotated: Map<Vertex, Vertex>) {
      newVertexToRotated.forEach { v, r -> mapOfVertexToRotatedVertex.putIfAbsent(v, r) }
   }
   companion object {
      internal fun rotate(chunk: Chunk, rotations: List<Rotation>): Pair<Bundle, Map<Vertex, Vertex>> {
         val bundle = chunk.makeBundle()
         return doRotations(rotations, bundle)
      }
      /**
       * Rotates the bundle doing one rotation at a time, starting with the first of the list.
       *
       * Returns both the rotated bundle and the mapping of original vertices to the vertices of the rotated bundle.
       */
      @VisibleForTesting
      internal fun doRotations(rotations: List<Rotation>, bundle: Bundle,
                               originalToVertex: Map<Vertex, Vertex>? = null ): Pair<Bundle, Map<Vertex, Vertex>> {
         if(rotations.isEmpty())
            return Pair(bundle, originalToVertex!!)
         val nextRotation = rotations.first()
         val rotator = makeRotator(angle = nextRotation.angle, bundles = setOf(bundle),
                 line = nextRotation.line, rotationDirection = nextRotation.rotationDirection)
         val rotatedBundle = rotator.rotateBundles().first()
         val vertexToRotated = rotator.mapOfVertexToRotatedVertex
         val originalToRotated = originalToVertex?.map {
            (original, vertex) ->
            original to vertexToRotated[vertex]!! }?.toMap() ?: vertexToRotated
         return doRotations(rotations.drop(1), rotatedBundle, originalToRotated)
      }
   }
}