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

import com.google.common.base.Preconditions
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Bundle.Side.BOTTOM
import com.whitebeluga.origami.figure.Bundle.Side.TOP
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.component.Edge
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.loosen.chunkloosening.ChunkBoundary
import com.whitebeluga.origami.loosen.util.ConnectedFinder

/**
 * This class represents an intermediate step in the process of making the chunk tree.
 * It creates the nodes with the faces and the boundaries but it stops there and does not find the
 * connections between nodes to make the tree.
 */
internal class UnconnectedChunkMaker(val figure: Figure) {
   private val bundles: Set<Bundle> = figure.bundles
   private val mapOfBundleToInternalBoundaries: Map<Bundle, Set<ChunkBoundary>>
   private val mapOfBundlePairToInterBundleBoundary: Map<Set<Bundle>, ChunkBoundary>

   init {
      mapOfBundleToInternalBoundaries = findInternalBoundaries()
      mapOfBundlePairToInterBundleBoundary = findInterBundleBoundaries()
   }
   /**
    * Finds boundaries within bundles. Bundle connections are also boundaries, but are calculated with this method.
    */
   private fun findInternalBoundaries(): Map<Bundle, Set<ChunkBoundary>> =
           bundles.map {
              it to InternalChunkBoundaryFinder(figure, it).findInternalBoundaries()
           }.toMap()
   /**
    * Returns pairs of connected bundles (as sets) mapped to their boundaries
    */
   private fun findInterBundleBoundaries(): Map<Set<Bundle>, ChunkBoundary> =
           when(bundles.size) {
              1 -> mapOf()
              2 -> findInterBundleBoundariesWhenTwoBundles()
              else -> findInterBundleBoundariesWhenMoreThanTwoBundles()
           }
   private fun findInterBundleBoundariesWhenTwoBundles(): Map<Set<Bundle>, ChunkBoundary> {
      val bundleList = bundles.toList()
      return mapOf(bundles to findBoundary(bundleList[0], bundleList[1]))
   }
   private fun findInterBundleBoundariesWhenMoreThanTwoBundles(): Map<Set<Bundle>, ChunkBoundary> {
      val result = mutableMapOf<Set<Bundle>, ChunkBoundary>()
      for(bundle in bundles) {
         val connectedTo = figure.bundlesConnectedTo(bundle)
         for(connectedBundle in connectedTo) {
            val key = setOf(bundle, connectedBundle)
            if(!result.containsKey(key)) {
               val boundary = findBoundary(bundle, connectedBundle)
               result[key] = boundary
            }
         }
      }
      return result
   }
   private fun findBoundary(bundle1: Bundle, bundle2: Bundle): ChunkBoundary =
           ChunkBoundary(bundle1.edges.intersect(bundle2.edges))
   fun makeUnconnectedChunks(): Set<UnconnectedChunk> {
      return bundles.flatMap {bundle -> makeUnconnectedChunks(bundle) }.toSet()
   }
   private fun findInternalBoundariesOf(bundle: Bundle): Set<ChunkBoundary> =
           mapOfBundleToInternalBoundaries[bundle] ?: emptySet()
   private fun interBundleBoundariesOfBundle(bundle: Bundle) : Map<Bundle, ChunkBoundary> =
           mapOfBundlePairToInterBundleBoundary
                   .filterKeys { it.contains(bundle) }
                   .map { (bundlePair, boundary) ->
                      val otherBundle = (bundlePair - bundle).first()
                      otherBundle to boundary }
                   .toMap()
   private fun makeUnconnectedChunks(bundle: Bundle): Set<UnconnectedChunk> {
      val mapOfFacesOfChunkToInternalBoundaries =
              makeMapOfFacesOfChunkToInternalBoundaries(bundle)
      val interBundleBoundaries = interBundleBoundariesOfBundle(bundle).values.toSet()
      return mapOfFacesOfChunkToInternalBoundaries
              .map { (facesOfChunk, internalBoundaries) ->
                 val interBundleBoundariesOfChunk = findInterBundleBoundariesOfChunk(facesOfChunk, interBundleBoundaries)
                 UnconnectedChunk(bundle, facesOfChunk, internalBoundaries, interBundleBoundariesOfChunk)
              }
              .toSet()
   }
   /**
    * If there are no internal boundaries then the map will have a single entry where the key is a set of all the
    * faces in the bundle and the value is an empty set
    */
   private fun makeMapOfFacesOfChunkToInternalBoundaries(bundle: Bundle): Map<Set<Face>, Set<Pair<Bundle.Side, ChunkBoundary>>> {
      val internalBoundarySideToFaces =
              makeMapOfInternalBoundarySideToFaces(bundle)
      val thereAreNoInternalBoundaries = internalBoundarySideToFaces.isEmpty()
      return if(thereAreNoInternalBoundaries)
            mapOf(bundle.faces to emptySet())
         else
            mergeFaceSets(internalBoundarySideToFaces)
   }
   /**
    * Maps each side of an internal boundary of a bundle to the faces that are connected to it and that are bound
    * by the other internal boundaries and the boundaries of the bundle
    */
   private fun makeMapOfInternalBoundarySideToFaces(bundle: Bundle): Map<Pair<Bundle.Side, ChunkBoundary>, Set<Face>> {
      val internalBoundaries = findInternalBoundariesOf(bundle)
      val thereAreNoInternalBoundaries = internalBoundaries.isEmpty()
      if(thereAreNoInternalBoundaries)
         return emptyMap()
      val internalBoundarySideToFaces: MutableMap<Pair<Bundle.Side, ChunkBoundary>, Set<Face>> = mutableMapOf()
      for(internalBoundary in internalBoundaries) {
         val restOfInternalBoundaries = internalBoundaries - internalBoundary
         val faceFinder = ChunkBoundarySide_FaceFinder(bundle, internalBoundary, restOfInternalBoundaries)
         val sideToFaces = faceFinder.findFaces()
         listOf(TOP, BOTTOM).forEach {
            internalBoundarySideToFaces[Pair(it, internalBoundary)] = sideToFaces[it]!!
         }
      }
      return internalBoundarySideToFaces
   }
   private fun findInterBundleBoundariesOfChunk(facesOfChunk: Set<Face>, interBundleBoundaries: Set<ChunkBoundary>): Set<ChunkBoundary> {
      return interBundleBoundaries.filter {boundary ->
         val edges = boundary.edges
         val areEdgesContainedInTheChunkFaces = edges.all { edge -> facesOfChunk.any { face -> face.edges.contains(edge) } }
         val someEdgesContainedInTheChunkFaces = edges.any { edge -> facesOfChunk.any { face -> face.edges.contains(edge) } }
         assert(areEdgesContainedInTheChunkFaces == someEdgesContainedInTheChunkFaces) //either all the edges belong to the chunk, or none of them
         areEdgesContainedInTheChunkFaces
      }.toSet()
   }
   private fun mergeFaceSets(internalBoundarySideToFaces: Map<Pair<Bundle.Side, ChunkBoundary>, Set<Face>>):
           Map<Set<Face>, Set<Pair<Bundle.Side, ChunkBoundary>>> {
      val facesOfChunkToBoundaries =
              mutableMapOf<Set<Face>, Set<Pair<Bundle.Side, ChunkBoundary>>>()
      for((sideAndBoundary, faces) in internalBoundarySideToFaces) {
         val entriesToMerge = facesOfChunkToBoundaries
                 .filterKeys { it.intersect(faces).isNotEmpty() }
         if(entriesToMerge.isEmpty())
            facesOfChunkToBoundaries[faces] = setOf(sideAndBoundary)
         else {
            assert(entriesToMerge.size == 1) //can only be one other set of faces at most
            val entryToMerge = entriesToMerge.entries.first()
            facesOfChunkToBoundaries.remove(entryToMerge.key)
            val newFaces = faces + entryToMerge.key
            val newBoundaries = entryToMerge.value + sideAndBoundary
            facesOfChunkToBoundaries[newFaces] = newBoundaries
         }
      }
      return facesOfChunkToBoundaries
   }
   /**
    * Finds the internal boundaries of a bundle
    */
   class InternalChunkBoundaryFinder(private val figure: Figure, private val bundle: Bundle) {
      private val connectingEdges = bundle.edges.filter { bundle.isAConnectingEdge(it) }.toSet()
      /**
       * Algorithm:
       *
       * 1) Find all groups of intersecting edges that have faces folded in the same direction
       * 2) The faces articulated by the edges must be "wrapped" with each other
       * 3) The articulated faces at each side cannot be connected through (other than by the edges identified in the
       * previous step)
       */
      fun findInternalBoundaries(): Set<ChunkBoundary> =
              when(bundle.faces.size) {
                 1 -> emptySet()
                 else -> findInternalBoundariesWhenTwoOrMoreFaces()
              }
      private fun findInternalBoundariesWhenTwoOrMoreFaces(): Set<ChunkBoundary> {
         val setsOfOverlappingEdges = groupOverlappingEdges(connectingEdges).toMutableSet()
         breakSetsOfOverlappingEdgesIntoSetsOfWrappedEdges(setsOfOverlappingEdges)
         val filteredNotConnectedFaces = setsOfOverlappingEdges
                 .filter { facesArticulatedByEdgesAreNotConnected(it) }
                 .toSet()
         return filteredNotConnectedFaces.map { ChunkBoundary(it) }.toSet()
      }
      private fun groupOverlappingEdges(edges: Set<Edge>): Set<Set<Edge>> {
         val overlappingEdgeSets: MutableSet<MutableSet<Edge>> = mutableSetOf()
         edges.forEach {
            updateSetsOfOverlappingEdges(it, overlappingEdgeSets)
         }
         return overlappingEdgeSets
      }
      private fun updateSetsOfOverlappingEdges(newEdge: Edge, sets: MutableSet<MutableSet<Edge>>) {
         for(edgeSet in sets) {
            val belongs = edgeSet.any { newEdge.overlaps(it) }
            if(belongs){
               edgeSet.add(newEdge)
               return
            }
         }
         sets.add(mutableSetOf(newEdge))
      }
      private fun breakSetsOfOverlappingEdgesIntoSetsOfWrappedEdges(edgeSets: MutableSet<Set<Edge>>) {
         edgeSets
                 .filter { it.size > 1 } //if the set is comprised of just one edge, there's nothing to do
                 .forEach { edgeSet ->
                    val broken = breakEdgeSetIntoWrappedEdgesSet(edgeSet)
                    val shouldEdgeSetBeBroken = broken.size > 1
                    if(shouldEdgeSetBeBroken) {
                       edgeSets.remove(edgeSet)
                       edgeSets.addAll(broken)
                    }
                 }
      }
      /**
       * Parameter: a set of overlapping edges
       *
       * A set of edges forming a true boundary must be wrapped together.
       *
       * For example two edges that articulate respective pairs of faces folded in different directions and, thus,
       * not overlapping is not a true boundary.
       *
       * Two edges that articulate respective pairs of faces that are not wrapped around each other but rather
       * one pair on top of the other are not a true boundary either.
       *
       */
      private fun breakEdgeSetIntoWrappedEdgesSet(edgeSet: Set<Edge>): Set<Set<Edge>> {
         val isJustOneEdge = edgeSet.size == 1
         if(isJustOneEdge)
            return setOf(edgeSet)
         val edgesNotClassified: MutableSet<Edge> = edgeSet.toMutableSet()
         val groupedEdges: MutableSet<Set<Edge>> = mutableSetOf()
         while(edgesNotClassified.isNotEmpty()) {
            val justOneLeft = edgesNotClassified.size == 1
            if(justOneLeft) {
               groupedEdges.add(edgesNotClassified)
               return groupedEdges
            }
            val edge = edgesNotClassified.first()
            val wrappedOrWrappingEdges= fun(e: Edge): Set<Edge> {
               val otherEdges = edgesNotClassified - e
               return wrappedOrWrapping(e, otherEdges)
            }
            val group = ConnectedFinder.findConnectedThrough(edge, wrappedOrWrappingEdges)
            edgesNotClassified.removeAll(group)
            groupedEdges.add(group)
         }
         return groupedEdges
      }
      private fun wrappedOrWrapping(edge: Edge, otherEdges: Set<Edge>): Set<Edge> =
              otherEdges.filter { wrappedOrWrapping(edge, it) }.toSet()
      private fun wrappedOrWrapping(edge1: Edge, edge2: Edge): Boolean=
              facesOfFirstEdgeWrappingFacesOfSecondEdge(edge1, edge2) ||
                      facesOfFirstEdgeWrappingFacesOfSecondEdge(edge2, edge1)
      private fun facesOfFirstEdgeWrappingFacesOfSecondEdge(wrappingEdgeCandidate: Edge, wrappedEdgeCandidate: Edge): Boolean {
         val wrappingFacesCandidate = bundle.facesConnectedBy(wrappingEdgeCandidate).toList()
         val wrappedFacesCandidate = bundle.facesConnectedBy(wrappedEdgeCandidate).toList()
         val facesAboveWrapped = wrappedFacesCandidate.flatMap { bundle.facesAbove(it) }.toSet()
         val facesBelowWrapped = wrappedFacesCandidate.flatMap { bundle.facesBelow(it) }.toSet()
         val thereIsOneWrappingFaceAboveWrapped = facesAboveWrapped.intersect(wrappingFacesCandidate).size == 1
         val thereIsOneWrappingFaceBelowWrapped = facesBelowWrapped.intersect(wrappingFacesCandidate).size == 1
         return thereIsOneWrappingFaceAboveWrapped && thereIsOneWrappingFaceBelowWrapped
      }
      /**
       * Ensures that the group of faces at both sides (top and bottom) articulated by the edges of a boundary are
       * connected only by those edges, and not through other faces, which wouldn't allow the opening of the "chunk"
       * or "flap".
       */
      private fun facesArticulatedByEdgesAreNotConnected(edges: Set<Edge>): Boolean {
         val facesOnTopSide = edges.map { bundle.topFaceConnectedByEdge(it) }.toSet()
         val facesOnBottomSide = edges.map { bundle.bottomFaceConnectedByEdge(it) }.toSet()
         //connected faces, excluding the edges of the boundary
         val newConnections = fun(face: Face) =
                 face.edges
                         .filter {
                            figure.isAConnectingEdge(it) && !edges.contains(it) }
                         .map {
                            figure.connectedToByEdge(face, it) }
                         .toSet()
         facesOnTopSide.forEach {
            val facesConnectedThrough = ConnectedFinder.findConnectedThrough(it, newConnections)
            val isConnectedToAnyBottomSideFace = facesConnectedThrough.intersect(facesOnBottomSide).isNotEmpty()
            if(isConnectedToAnyBottomSideFace)
               return false
         }
         return true
      }
      companion object {
         fun fromMonoBundleFigure(figure: Figure): InternalChunkBoundaryFinder {
            Preconditions.checkArgument(figure.isMonoBundle())
            val bundle = figure.bundles.first()
            return InternalChunkBoundaryFinder(figure, bundle)
         }
      }
   }
   /**
    * This class finds all faces connected through from a side of an internal boundary of a bundle.
    */
   class ChunkBoundarySide_FaceFinder(private val bundle: Bundle,
                                      boundary: ChunkBoundary,
                                      /**
                                       * The rest of internal boundaries in this bundle
                                       */
                                      allOtherBoundariesInBundle: Set<ChunkBoundary>) {
      private val edgesOfBoundary = boundary.edges
      private val allOtherEdgesOfInternalBoundaries: Set<Edge>
      private val allEdgesOfInternalBoundaries: Set<Edge>
      init {
         assert(!allOtherBoundariesInBundle.contains(boundary))
         allOtherEdgesOfInternalBoundaries = allOtherBoundariesInBundle.flatMap { it.edges }.toSet()
         allEdgesOfInternalBoundaries = edgesOfBoundary + allOtherEdgesOfInternalBoundaries
      }
      fun findFaces(): Map<Bundle.Side, Set<Face>> = mapOf( TOP to findFaces(TOP), BOTTOM to findFaces(BOTTOM) )
      private fun findFaces(side: Bundle.Side): Set<Face> {
         val facesOnSideOfBoundary = edgesOfBoundary.map { bundle.faceConnectedByEdge(it, side) }.toSet()
         return facesOnSideOfBoundary.flatMap { ConnectedFinder.findConnectedThrough(it, ::connectedFaces) }.toSet()
      }
      fun connectedFaces(face: Face): Set<Face> {
         val connectedFaces = bundle.mapOfFacesConnectedTo(face)
         val connectedFacesThroughNonBoundaryEdges = connectedFaces
                 .filterKeys { !allEdgesOfInternalBoundaries.contains(it) }
         return connectedFacesThroughNonBoundaryEdges.values.toSet()
      }
   }
}