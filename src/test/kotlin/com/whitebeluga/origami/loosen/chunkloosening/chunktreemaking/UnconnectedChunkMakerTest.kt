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

package com.whitebeluga.origami.loosen.chunkloosening.chunktreemaking

import com.moduleforge.libraries.geometry._3d.Plane
import com.moduleforge.libraries.geometry._3d.Plane.planeFromOrderedPoints
import com.moduleforge.libraries.geometry._3d.Point
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Bundle.Side.BOTTOM
import com.whitebeluga.origami.figure.Bundle.Side.TOP
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.OrigamiBase.*
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UnconnectedChunkMakerTest {
   private lateinit var xyPlane_NormalTowardsZPositive: Plane
   private lateinit var xyPlane_NormalTowardsZPositive_ZCoordIs1: Plane
   //this y-z plane is at x = 1
   private lateinit var yzPlaneTowardsXPos: Plane

   @Before
   fun setUp(){
      val antiClockwisePointsAsSeenFromZPositive =
              listOf(Point(0, 0, 0), Point(1, 0, 0), Point(1, 1, 0))
      xyPlane_NormalTowardsZPositive = planeFromOrderedPoints(antiClockwisePointsAsSeenFromZPositive)

      val antiClockwisePointsAsSeenFromZPositive_ZCoordIs1 =
              listOf(Point(0, 0, 1), Point(1, 0, 1), Point(1, 1, 1))
      xyPlane_NormalTowardsZPositive_ZCoordIs1 = planeFromOrderedPoints(antiClockwisePointsAsSeenFromZPositive_ZCoordIs1)

      val pointsInYZ =
              listOf(Point(1, 0, 0), Point(1, 1, 0), Point(1, 1, 1))
      yzPlaneTowardsXPos = planeFromOrderedPoints(pointsInYZ)
   }
   @Test
   fun testMakeUnconnectedChunks_WhenSingleFaceFigure_Calculated() {
      val square = SQUARE.make(1)
      testsItIsASingleChunkFigure(square)
   }
   @Test
   fun testMakeUnconnectedChunks_WhenBlintzBase_Calculated() {
      val blintz = BLINTZ.make(1)
      val chunkMaker = UnconnectedChunkMaker(blintz)
      val unconnectedChunks = chunkMaker.makeUnconnectedChunks()
      assertThat(unconnectedChunks.size, `is`(5)) //the base and the four flaps
      //no interbundle boundaries for any chunk
      assertTrue(unconnectedChunks.all { it.interBundleBoundaries.isEmpty() })
      val squareBaseChunk = unconnectedChunks.first { it.faces.first().isQuadrilateral() }
      assertThat(squareBaseChunk.internalBoundaries.size, `is`(4))
      val flapChunks = unconnectedChunks - squareBaseChunk
      flapChunks.forEach {
         assertThat(it.internalBoundaries.size, `is`(1))
      }
   }
   /**
    *  The bird base is a moderately complicated figure with no "chunks" (or rather, just one).
    *  The interest of this test is that non-trivial figures with a single chunks are correctly calculated too.
    */
   @Test
   fun testMakeUnconnectedChunks_WhenBirdBase_ShouldReturnNoBoundaries() {
      val bird = BIRD.make(1)
      testsItIsASingleChunkFigure(bird)
   }
   /**
    * Same as the bird base, this is complicated figure but made of a single "chunk"
    */
   @Test
   fun testMakeUnconnectedChunks_WhenBirdBlintzBase_ShouldReturnNoBoundaries() {
      val birdBlintz = BIRD_BLINTZ.make(1)
      testsItIsASingleChunkFigure(birdBlintz)
   }
   /**
    * In this case, each chunk is made of two faces:
    * The paper is first folded by the middle, then folded in a three part accordion as the other
    * figure.
    *
    * Should find two boundaries where each has two edges
    */
   @Test
   fun testMakeUnconnectedChunks_WhenThreeChunkAccordion_Calculated() {
      val v1 = Vertex(0.0, 1.0, 0.0)
      val v2 = Vertex(1.0, 1.0, 0.0)
      val v3 = Vertex(0.0, 1.0, 0.0)
      val v4 = Vertex(1.0, 1.0, 0.0)

      val v5 = Vertex(0.0, 0.0, 0.0)
      val v6 = Vertex(1.0, 0.0, 0.0)
      val v7 = Vertex(0.0, 0.0, 0.0)
      val v8 = Vertex(1.0, 0.0, 0.0)

      val v9 = Vertex(v5)
      val v10 = Vertex(v6)
      val v11 = Vertex(v7)
      val v12 = Vertex(v8)
      val topFaceOfTopChunk = Face(v1, v2, v6, v5)
      val bottomFaceOfTopChunk = Face(v1, v2, v10, v9)
      val topFaceOfInnerChunk = Face(v2, v10, v11, v3)
      val bottomFaceOfInnerChunk = Face(v2, v6, v7, v3)
      val topFaceOfBottomChunk = Face(v3, v4, v8, v7)
      val bottomFaceOfBottomChunk = Face(v3, v4, v12, v11)
      val faces = setOf(topFaceOfTopChunk, bottomFaceOfTopChunk,
              topFaceOfInnerChunk, bottomFaceOfInnerChunk,
              topFaceOfBottomChunk, bottomFaceOfBottomChunk)
      val bundle = Bundle(xyPlane_NormalTowardsZPositive, faces,
              mapOf(  bottomFaceOfBottomChunk to faces - bottomFaceOfBottomChunk,
                      topFaceOfBottomChunk to (faces - bottomFaceOfBottomChunk) - topFaceOfBottomChunk,
                      bottomFaceOfInnerChunk to setOf(topFaceOfInnerChunk, bottomFaceOfTopChunk, topFaceOfTopChunk),
                      topFaceOfInnerChunk to setOf(bottomFaceOfTopChunk, topFaceOfTopChunk),
                      bottomFaceOfTopChunk to setOf(topFaceOfTopChunk) ) )
      val accordion = Figure(bundle)
      val chunkMaker = UnconnectedChunkMaker(accordion)
      val unconnectedChunks = chunkMaker.makeUnconnectedChunks()
      assertThat(unconnectedChunks.size, `is`(3)) //there should be three chunks
      //no interbundle boundaries for any chunk
      assertTrue(unconnectedChunks.all { it.interBundleBoundaries.isEmpty() })
      val topChunk = unconnectedChunks
              .first { it.faces == setOf(topFaceOfTopChunk, bottomFaceOfTopChunk) }
      val innerChunk = unconnectedChunks
              .first { it.faces == setOf(topFaceOfInnerChunk, bottomFaceOfInnerChunk) }
      val bottomChunk = unconnectedChunks
              .first { it.faces == setOf(topFaceOfBottomChunk, bottomFaceOfBottomChunk) }
      //top chunk has just one boundary
      assertThat(topChunk.internalBoundaries.size, `is`(1))
      //inner chunk has two boundaries
      assertThat(innerChunk.internalBoundaries.size, `is`(2))
      //bottom chunk has one boundary
      assertThat(bottomChunk.internalBoundaries.size, `is`(1))
      val sideAndBoundaryFromTopToInnerChunk = topChunk.internalBoundaries.first()
      val sideAndBoundaryFromBottomToInnerChunk = bottomChunk.internalBoundaries.first()
      val boundariesWithInnerChunk = innerChunk.internalBoundaries.map { it.second }.toSet()
      val boundaryFromTopToInnerChunk = sideAndBoundaryFromTopToInnerChunk.second
      val boundaryFromBottomToInnerChunk = sideAndBoundaryFromBottomToInnerChunk.second

      //the boundaries of the inner chunk coincide with the set of a boundary of the bottom chunk and a boundary of the top chunk
      assertThat(boundariesWithInnerChunk, `is`(setOf(boundaryFromTopToInnerChunk, boundaryFromBottomToInnerChunk)))

      val sideOfBoundaryFromTopToInnerChunk = sideAndBoundaryFromTopToInnerChunk.first

      //the side of the boundary of the top chunk must be, of course, the top
      assertThat(sideOfBoundaryFromTopToInnerChunk, `is`(TOP))

      val sideOfBoundaryFromBottomToInnerChunk = sideAndBoundaryFromBottomToInnerChunk.first
      assertThat(sideOfBoundaryFromBottomToInnerChunk, `is`(BOTTOM))

      val sideOfBoundaryFromInnerChunkToTop = innerChunk.internalBoundaries
              .first { it.second == boundaryFromTopToInnerChunk }.first
      //the inner chunk is in the bottom with respect to the top one
      assertThat(sideOfBoundaryFromInnerChunkToTop, `is`(BOTTOM))

      val sideOfBoundaryFromInnerChunkToBottom = innerChunk.internalBoundaries
              .first { it.second == boundaryFromBottomToInnerChunk }.first
      //the inner chunk is in the top with respect to the bottom one
      assertThat(sideOfBoundaryFromInnerChunkToBottom, `is`(TOP))
   }
   /**
    * This is similar to the other double-face three chunk example.
    *
    * However in this example, the figure is not just one bundle,
    * rather the chunks belong in different planes.
    *
    * Draw this stuff for easier visualization
    */
   @Test
   fun testMakeUnconnectedChunks_WhenThreeChunkFigure_Calculated() {
      val v1 = Vertex(0.0, 1.0, 0.0)
      val v2 = Vertex(1.0, 1.0, 0.0)
      val v3 = Vertex(1.0, 1.0, 1.0)
      val v4 = Vertex(2.0, 1.0, 1.0)

      val v5 = Vertex(0.0, 0.0, 0.0)
      val v6 = Vertex(1.0, 0.0, 0.0)
      val v7 = Vertex(1.0, 0.0, 1.0)
      val v8 = Vertex(2.0, 0.0, 1.0)

      val v9 = Vertex(v5)
      val v10 = Vertex(v6)
      val v11 = Vertex(v7)
      val v12 = Vertex(v8)

      val topFaceOfFirstBundle = Face(v1, v2, v6, v5)
      val bottomFaceOfFirstBundle = Face(v1, v2, v10, v9)

      val topFaceOfSecondBundle = Face(v2, v10, v11, v3)
      val bottomFaceOfSecondBundle = Face(v2, v6, v7, v3)

      val topFaceOfThirdBundle = Face(v3, v4, v8, v7)
      val bottomFaceOfThirdBundle = Face(v3, v4, v12, v11)

      val firstBundle = Bundle(xyPlane_NormalTowardsZPositive,
              setOf(topFaceOfFirstBundle, bottomFaceOfFirstBundle),
              mapOf(bottomFaceOfFirstBundle to setOf(topFaceOfFirstBundle)))
      val secondBundle = Bundle(yzPlaneTowardsXPos,
              setOf(bottomFaceOfSecondBundle, topFaceOfSecondBundle),
              mapOf(bottomFaceOfSecondBundle to setOf(topFaceOfSecondBundle)))
      val thirdBundle = Bundle(xyPlane_NormalTowardsZPositive,
              setOf(bottomFaceOfThirdBundle, topFaceOfThirdBundle),
              mapOf(bottomFaceOfThirdBundle to setOf(topFaceOfThirdBundle)))

      val figure = Figure(setOf(firstBundle, secondBundle, thirdBundle))
      val chunkMaker = UnconnectedChunkMaker(figure)
      val unconnectedChunks = chunkMaker.makeUnconnectedChunks()
      assertThat(unconnectedChunks.size, `is`(3)) //there should be three chunks
      assertTrue(unconnectedChunks.all { it.internalBoundaries.isEmpty() })
      val firstChunk = unconnectedChunks.first { it.faces == firstBundle.faces }
      val secondChunk = unconnectedChunks
              .first { it.faces == setOf(bottomFaceOfSecondBundle, topFaceOfSecondBundle) }
      val thirdChunk = unconnectedChunks
              .first { it.faces == setOf(bottomFaceOfThirdBundle, topFaceOfThirdBundle) }
      //top chunk has just one boundary
      assertThat(firstChunk.interBundleBoundaries.size, `is`(1))
      //inner chunk has two boundaries
      assertThat(secondChunk.interBundleBoundaries.size, `is`(2))
      //bottom chunk has one boundary
      assertThat(thirdChunk.interBundleBoundaries.size, `is`(1))
      val boundaryFromFirstChunkToSecond = firstChunk.interBundleBoundaries.first()
      val boundaryFromThirdChunkToSecond = thirdChunk.interBundleBoundaries.first()
      val boundariesOfSecondChun = secondChunk.interBundleBoundaries
      assertEquals(boundariesOfSecondChun, setOf(boundaryFromFirstChunkToSecond, boundaryFromThirdChunkToSecond) )
   }
   fun testsItIsASingleChunkFigure(figure: Figure){
      val chunkMaker = UnconnectedChunkMaker(figure)
      val unconnectedChunks = chunkMaker.makeUnconnectedChunks()
      assertThat(unconnectedChunks.size, `is`(1))
      val unconnectedChunk = unconnectedChunks.first()
      val facesOfUnconnectedChunk = unconnectedChunk.faces
      assertThat(facesOfUnconnectedChunk, `is`(figure.faces))
      assertTrue(unconnectedChunk.interBundleBoundaries.isEmpty())
      assertTrue(unconnectedChunk.internalBoundaries.isEmpty())
   }
}