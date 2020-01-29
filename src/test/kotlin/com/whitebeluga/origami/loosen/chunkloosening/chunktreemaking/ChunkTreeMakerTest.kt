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

import com.moduleforge.libraries.geometry._3d.Plane
import com.moduleforge.libraries.geometry._3d.Point
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ChunkTreeMakerTest {
   private lateinit var xyPlane_NormalTowardsZPositive: Plane

   @Before
   fun setUp(){
      val antiClockwisePointsAsSeenFromZPositive =
              listOf(Point(0, 0, 0), Point(1, 0, 0), Point(1, 1, 0))
      xyPlane_NormalTowardsZPositive = Plane.planeFromOrderedPoints(antiClockwisePointsAsSeenFromZPositive)
   }

   /**
    * This is a rectangle, with two edge-to-edge parallel folds, making an accordion
    * of three faces.
    *
    * Each fold is, obviously, a boundary.
    *
    * Top face is the largest by area.
    *
    * Unfolded:
    *
    * v1--------v2----v3----v4
    * |          |     |     |
    * |          |     |     |
    * v5--------v6----v7----v8
    *
    */
   @Test
   fun testMakeChunkTree_WhenAccordionOfThreeFaces_InOneBundle_AndTopFaceHasLargestArea_ShouldMakeChunkTreeWithTopFaceAsRoot() {
      val v1 = Vertex(0.0, 1.0, 0.0)
      val v2 = Vertex(2.0, 1.0, 0.0)
      val v3 = Vertex(1.0, 1.0, 0.0)
      val v4 = Vertex(2.0, 1.0, 0.0)

      val v5 = Vertex(0.0, 0.0, 0.0)
      val v6 = Vertex(2.0, 0.0, 0.0)
      val v7 = Vertex(1.0, 0.0, 0.0)
      val v8 = Vertex(2.0, 0.0, 0.0)
      val topFace = Face(v1, v2, v6, v5)
      val innerFace = Face(v2, v3, v7, v6)
      val bottomFace = Face(v3, v4, v8, v7)
      val bundle = Bundle(xyPlane_NormalTowardsZPositive, setOf(topFace, innerFace, bottomFace),
              mapOf(bottomFace to setOf(innerFace, topFace), innerFace to setOf(topFace) ) )
      val accordion = Figure(bundle)
      val treeMaker = ChunkTreeMaker(accordion)
      //test faces
      val rootChunk = treeMaker.makeChunkTree()
      assertEquals(rootChunk.faces, setOf(topFace))
      assertThat(rootChunk.children.size, `is`(1))
      val childOfRoot = rootChunk.children.first()
      assertEquals(childOfRoot.faces, setOf(innerFace))
      val childrenOfChildOfRoot = childOfRoot.children
      assertThat(childrenOfChildOfRoot.size, `is`(1))
      val childOfChildOfRoot = childrenOfChildOfRoot.first()
      assertEquals(childOfChildOfRoot.faces, setOf(bottomFace))
      //test internal boundaries
      val internallyConnectedChunksOfRootChunk = rootChunk.internallyConnectedChunks
      assertThat(internallyConnectedChunksOfRootChunk.size, `is`(1))
      val internallyConnectedChunksOfChildOfRootChunk = childOfRoot.internallyConnectedChunks
      assertThat(internallyConnectedChunksOfChildOfRootChunk.size, `is`(1))
      val internallyConnectedChunksOfChildOfChildOfRootChunk =
              childOfChildOfRoot.internallyConnectedChunks
      assertTrue(internallyConnectedChunksOfChildOfChildOfRootChunk.isEmpty())

      val sideOfChildInBoundaryWithRootChunk = internallyConnectedChunksOfRootChunk.entries.first().value.first
      //naturally the side of the child of the root chunk is the bottom,since the root chunk is the top face
      assertThat(sideOfChildInBoundaryWithRootChunk, `is`(Bundle.Side.BOTTOM))
   }
   /**
    * This is a rectangle, with two edge-to-edge parallel folds, making an accordion
    * of three faces.
    *
    * Each fold is, obviously, a boundary.
    *
    * Top face is the largest by area.
    *
    * Unfolded:
    *
    * v1----v2--------v3----v4
    * |     |         |     |
    * |     |         |     |
    * v5----v6--------v7----v8
    *
    */
   @Test
   fun testMakeChunkTree_WhenAccordionOfThreeFaces_InOneBundle_AndInnerFaceHasLargestArea_ShouldMakeChunkTreeWithInnerFaceAsRoot() {
      val v1 = Vertex(0.0, 1.0, 0.0)
      val v2 = Vertex(1.0, 1.0, 0.0)
      val v3 = Vertex(-1.0, 1.0, 0.0)
      val v4 = Vertex(0.0, 1.0, 0.0)

      val v5 = Vertex(0.0, 0.0, 0.0)
      val v6 = Vertex(1.0, 0.0, 0.0)
      val v7 = Vertex(-1.0, 0.0, 0.0)
      val v8 = Vertex(0.0, 0.0, 0.0)
      val topFace = Face(v1, v2, v6, v5)
      val innerFace = Face(v2, v3, v7, v6)
      val bottomFace = Face(v3, v4, v8, v7)
      val bundle = Bundle(xyPlane_NormalTowardsZPositive, setOf(topFace, innerFace, bottomFace),
              mapOf(bottomFace to setOf(innerFace, topFace), innerFace to setOf(topFace) ) )
      val accordion = Figure(bundle)
      val treeMaker = ChunkTreeMaker(accordion)
      //test faces
      val rootChunk = treeMaker.makeChunkTree()
      assertEquals(rootChunk.faces, setOf(innerFace))
      val childrenOfRoot = rootChunk.children
      assertThat(childrenOfRoot.size, `is`(2))
      //the children of the root have no children themselves
      assertTrue(childrenOfRoot.all {it.children.isEmpty()} )

      //test internal boundaries
      val internallyConnectedChunksOfRootChunk = rootChunk.internallyConnectedChunks
      assertThat(internallyConnectedChunksOfRootChunk.size, `is`(2))
      val chunkBelowRootChunk = internallyConnectedChunksOfRootChunk.filterValues { it.first == Bundle.Side.BOTTOM}.keys.first()
      assertThat(chunkBelowRootChunk.faces, `is`(setOf(bottomFace)))
      val chunkOverRootChunk = internallyConnectedChunksOfRootChunk.filterValues { it.first == Bundle.Side.TOP}.keys.first()
      assertThat(chunkOverRootChunk.faces, `is`(setOf(topFace)))
   }
}