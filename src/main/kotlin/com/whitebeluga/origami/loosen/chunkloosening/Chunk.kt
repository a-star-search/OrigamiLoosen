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

package com.whitebeluga.origami.loosen.chunkloosening

import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.loosen.datastructures.TreeNode

/**
 * A chunk is a set of faces bounded by one or more line segments where one or more pairs of faces are articulated.
 *
 * Chunks are articulated at usually one or sometimes more sides. If a chunk is articulated on just one segment,
 * which is the usual case, this is what we may call a "flap". Think, for example of the triangle flaps of the
 * blintz base or the two flaps of the kite base. The name "chunk", while not so apt for these more usual "flaps"
 * is more generic and includes these chained chunks that are not proper flaps.
 *
 * A bundle may have one or more chunks.
 *
 * The set of internally connected chunks and inter bundle connected chunks are not (necessarily) all the chunks
 * connected to this one! This is a tree, all the connected chunks are children to the object and the object is also
 * connected to a parent although that information is not contained in a child (ie there are no cyclical references).
 *
 */
internal open class Chunk(
        /** The bundle this chunk belongs to. */
        val bundle: Bundle,
        val faces: Set<Face>,
        /**
         * The keys of the map are the children.
         *
         * The values are a pair, the boundary itself and the side of the bundle in which the child is.
         *
         */
        val internallyConnectedChunks: Map<Chunk, Pair<Bundle.Side, ChunkBoundary>> = mapOf(),
        val interBundleConnectedChunks: Map<Chunk, ChunkBoundary> = mapOf() ) : TreeNode<Chunk> {
   override val children: Set<Chunk>
   init {
      children = internallyConnectedChunks.keys.toSet() + interBundleConnectedChunks.keys.toSet()
      assert( internallyConnectedChunks.keys.all { it.bundle == bundle})
      assert( interBundleConnectedChunks.keys.all { it.bundle != bundle })
   }
   fun makeBundle(): Bundle {
      val plane = bundle.plane
      val facesToFacesAbove = makeMapOfFaceToFacesAbove()
      return Bundle(plane, faces, facesToFacesAbove)
   }
   private fun makeMapOfFaceToFacesAbove(): Map<Face, Set<Face>> {
      val faceToFacesAbove_Bundle = bundle.facesToFacesAbove
      val keysFiltered = faceToFacesAbove_Bundle.filterKeys { faces.contains(it) }
      return keysFiltered
              .map { (face, facesAbove) ->
                  face to facesAbove.filter { faces.contains(it) }.toSet() }.toMap()
              .filterValues { it.isNotEmpty() }
   }
   companion object {
      /**
       * When a figure is a single chunk itself (better understood if we say have "no chunks").
       */
      fun chunkOfMonoChunkFigure(figure: Figure) =
              MonoChunkFigureChunk(figure.bundles.first())
      /** no children */
      fun makeLeafChunk(bundle: Bundle, faces: Set<Face>) = Chunk(bundle, faces)
      fun makeLeafChunk(bundle: Bundle, face: Face) = makeLeafChunk(bundle, setOf(face))

      fun makeChunkWithNoBoundariesToOtherBundles(bundle: Bundle, faces: Set<Face>,
                  boundariesWithSameBundleChunks: Map<Chunk, Pair<Bundle.Side, ChunkBoundary>>) =
              Chunk(bundle, faces, boundariesWithSameBundleChunks, mapOf())
      fun makeChunkWithNoBoundariesToSameBundle(bundle: Bundle, faces: Set<Face>,
                                                boundariesWithOtherBundles: Map<Chunk, ChunkBoundary>)=
              Chunk(bundle, faces, mapOf(), boundariesWithOtherBundles)
   }
   class MonoChunkFigureChunk(bundle: Bundle): Chunk(bundle, bundle.faces){
      //
   }
}