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

package com.whitebeluga.origami.loosen.chunkloosening.chunktreemaking

import com.moduleforge.libraries.geometry._3d.Polygon.Companion.calculateUnionArea
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Bundle.Side.BOTTOM
import com.whitebeluga.origami.figure.Bundle.Side.TOP
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.loosen.chunkloosening.Chunk
import com.whitebeluga.origami.loosen.chunkloosening.Chunk.Companion.chunkOfMonoChunkFigure
import com.whitebeluga.origami.loosen.chunkloosening.Chunk.Companion.makeChunkWithNoBoundariesToOtherBundles
import com.whitebeluga.origami.loosen.chunkloosening.Chunk.Companion.makeChunkWithNoBoundariesToSameBundle
import com.whitebeluga.origami.loosen.chunkloosening.Chunk.Companion.makeLeafChunk
import com.whitebeluga.origami.loosen.chunkloosening.ChunkBoundary

/**
 * Takes a figure as input and breaks it into a tree structure of chunks.
 *
 * See the definition of Chunk.
 *
 * The root chunk is the chunk with maximum area of the bundle with maximum area.
 */
internal class ChunkTreeMaker(private val figure: Figure) {
   private val bundles = figure.bundles

   fun makeChunkTree(): Chunk =
           when (figure.faces.size) {
              1 -> chunkOfMonoChunkFigure(figure)
              2 -> makeChunkTreeFromBiFaceFigure()
              else -> makeChunkTreeWhenMoreThanTwoFaces()
           }
   /** special case */
   private fun makeChunkTreeFromBiFaceFigure(): Chunk {
      val faces = figure.faces.toList()
      val connectingEdge = faces[0].edges.intersect(faces[1].edges)
      val boundary = ChunkBoundary(connectingEdge)
      val bigFace = faces.maxBy { it.area() }!!
      val smallFace = faces.minBy { it.area() }!!
      return if (figure.isMonoBundle())
         makeChunkTreeFromBiFaceFigure_SingleBundleFigure(boundary, smallFace, bigFace)
      else
         makeChunkTreeFromBiFaceFigure_TwoBundleFigure(boundary, smallFace, bigFace)
   }
   /** special case */
   private fun makeChunkTreeFromBiFaceFigure_TwoBundleFigure(boundary: ChunkBoundary, smallFace: Face, bigFace: Face): Chunk {
      val bigBundle = figure.bundleOf(bigFace)
      val smallBundle = figure.bundleOf(smallFace)
      val childChunk = makeLeafChunk(smallBundle, smallFace)
      val rootChunk = makeChunkWithNoBoundariesToSameBundle(bigBundle, setOf(bigFace), mapOf(childChunk to boundary))
      return rootChunk
   }
   /** special case */
   private fun makeChunkTreeFromBiFaceFigure_SingleBundleFigure(boundary: ChunkBoundary, smallFace: Face, bigFace: Face): Chunk {
      val bundle = bundles.first()
      val childChunk = makeLeafChunk(bundle, smallFace)
      val sideOfSmallFace = if (bundle.topFaces().contains(smallFace)) TOP else BOTTOM
      val rootChunk = makeChunkWithNoBoundariesToOtherBundles(bundle, setOf(bigFace),
              mapOf(childChunk to Pair(sideOfSmallFace, boundary)))
      return rootChunk
   }
   private fun makeChunkTreeWhenMoreThanTwoFaces(): Chunk {
      val chunkMaker = UnconnectedChunkMaker(figure)
      val allChunks = chunkMaker.makeUnconnectedChunks()
      return makeChunkTree(allChunks)
   }
   private fun makeChunkTree(unconnectedChunks: Set<UnconnectedChunk>): Chunk {
      val boundaryToChunks = makeMapOfBoundaryToChunks(unconnectedChunks)
      val mainChunk = findMainChunk(unconnectedChunks)
      return makeChunkTreeRecursive(mainChunk, null, boundaryToChunks)
   }
   /**
    * From the set of unconnected chunks we make a map of boundaries to unconnected chunks, so that
    * the tree can be constructed easily, finding the children of a given node for which we know what the boundaries are
    */
   private fun makeMapOfBoundaryToChunks(chunks: Set<UnconnectedChunk>): Map<ChunkBoundary, Set<UnconnectedChunk>> {
      val boundaryToChunks = mutableMapOf<ChunkBoundary, MutableSet<UnconnectedChunk>>()
      for (chunk in chunks) {
         val boundariesWithOtherBundles = chunk.interBundleBoundaries
         for (boundary in boundariesWithOtherBundles) {
            boundaryToChunks[boundary]?.add(chunk)
            boundaryToChunks.putIfAbsent(boundary, mutableSetOf(chunk))
         }
         val boundariesWithSameBundleChunks = chunk.internalBoundaries
                 .map { it.second }
         for (boundary in boundariesWithSameBundleChunks) {
            boundaryToChunks[boundary]?.add(chunk)
            boundaryToChunks.putIfAbsent(boundary, mutableSetOf(chunk))
         }
      }
      return boundaryToChunks
   }
   private fun findMainChunk(allChunks: Set<UnconnectedChunk>): UnconnectedChunk = findChunkWithLargestArea(allChunks)
   private fun findChunkWithLargestArea(chunks: Set<UnconnectedChunk>) = chunks.maxBy {  calculateUnionArea(it.faces) }!!
   private fun makeChunkTreeRecursive(node: UnconnectedChunk,
                                      boundaryOfParent: ChunkBoundary?,
                                      boundaryToChunks: Map<ChunkBoundary, Set<UnconnectedChunk>>): Chunk {
      val chunkToBoundaryOfSameBundleChunks =
              makeInternallyConnectedChunkChildren(node, boundaryOfParent, boundaryToChunks)
      val chunkToBoundaryOfOtherBundles: Map<Chunk, ChunkBoundary> =
              makeInterBundleConnectedChunkChildren(node, boundaryOfParent, boundaryToChunks)
      return Chunk(node.bundle, node.faces, chunkToBoundaryOfSameBundleChunks, chunkToBoundaryOfOtherBundles)
   }
   private fun makeInternallyConnectedChunkChildren(unconnectedChunk: UnconnectedChunk,
                                                    boundaryOfParent: ChunkBoundary?,
                                                    mapOfBoundaryToChunks: Map<ChunkBoundary, Set<UnconnectedChunk>>):
           Map<Chunk, Pair<Bundle.Side, ChunkBoundary>> {
      val internalBoundaries = unconnectedChunk.internalBoundaries
      val internalBoundariesExceptParent = internalBoundaries
                       .filter { it.second != boundaryOfParent }
      if(internalBoundariesExceptParent.isEmpty())
         return emptyMap()
      val justBoundaries = internalBoundariesExceptParent.map {it.second}
      val internallyConnectedChunks = boundariesToChunks(justBoundaries, unconnectedChunk, mapOfBoundaryToChunks)
      /*
      the side used to create a child Chunk is the side of that the child is (not the side occupied by the parent -the parent is the UnconnectedChunk received as parameter -)
      so we need to replace it for its opposite
      */
      val internalBoundariesExceptParent_WithCorrectSide =
              internalBoundariesExceptParent.map { (side, boundary) -> Pair(side.opposite(), boundary) }
      return internallyConnectedChunks.zip(internalBoundariesExceptParent_WithCorrectSide).toMap()
   }
   private fun makeInterBundleConnectedChunkChildren(unconnectedChunk: UnconnectedChunk,
                                                     boundaryOfParent: ChunkBoundary?,
                                                     mapOfBoundaryToChunks: Map<ChunkBoundary, Set<UnconnectedChunk>>):
           Map<Chunk, ChunkBoundary> {
      val interBundleBoundaries = unconnectedChunk.interBundleBoundaries
      val interBundleBoundariesExceptParent = interBundleBoundaries.filter { it != boundaryOfParent }
      if(interBundleBoundariesExceptParent.isEmpty())
         return emptyMap()
      val interBundleConnectedChunks =
              boundariesToChunks(interBundleBoundariesExceptParent, unconnectedChunk, mapOfBoundaryToChunks)
      return interBundleConnectedChunks.zip(interBundleBoundariesExceptParent).toMap()
   }
   private fun boundariesToChunks(boundaries: List<ChunkBoundary>,
                                  unconnectedChunk: UnconnectedChunk,
                                  mapOfBoundaryToChunks: Map<ChunkBoundary, Set<UnconnectedChunk>>): List<Chunk> =
           boundaries.map {
              val otherUnconnectedChunk = (mapOfBoundaryToChunks[it]!! - unconnectedChunk).first()
              makeChunkTreeRecursive(otherUnconnectedChunk, it, mapOfBoundaryToChunks)
           }
}