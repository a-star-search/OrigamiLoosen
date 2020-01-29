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
import com.moduleforge.libraries.geometry._3d.Plane.planeFromOrderedPoints
import com.moduleforge.libraries.geometry._3d.Point
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.OrigamiBase.*
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import com.whitebeluga.origami.loosen.chunkloosening.chunktreemaking.UnconnectedChunkMaker.InternalChunkBoundaryFinder
import com.whitebeluga.origami.loosen.chunkloosening.chunktreemaking.UnconnectedChunkMaker.InternalChunkBoundaryFinder.Companion.fromMonoBundleFigure
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnconnectedChunkMaker_InternalChunkBoundaryFinderTest {
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
   fun testFindIntraBundleBoundaries_WhenSingleFaceFigure_ShouldReturnEmptySet() {
      val square = SQUARE.make(1)
      val boundaryFinder = fromMonoBundleFigure(square)
      val boundaries = boundaryFinder.findInternalBoundaries()
      assertTrue(boundaries.isEmpty())
   }
   @Test
   fun testFindIntraBundleBoundaries_WhenBlintzBase_ShouldReturnFourBoundaries() {
      val blintz = BLINTZ.make(1)
      val boundaryFinder = fromMonoBundleFigure(blintz)
      val boundaries = boundaryFinder.findInternalBoundaries()
      assertThat(boundaries.size, `is`(4))
   }
   @Test
   fun testFindIntraBundleBoundaries_WhenKiteBase_ShouldReturnTwoBoundaries() {
      val kite = KITE.make(1)
      val boundaryFinder = fromMonoBundleFigure(kite)
      val boundaries = boundaryFinder.findInternalBoundaries()
      assertThat(boundaries.size, `is`(2))
   }
   @Test
   fun testFindIntraBundleBoundaries_WhenWaterbombBase_ShouldReturnNoBoundaries() {
      val waterbomb = WATERBOMB.make(1)
      val boundaryFinder = fromMonoBundleFigure(waterbomb)
      val boundaries = boundaryFinder.findInternalBoundaries()
      assertTrue(boundaries.isEmpty())
   }
   @Test
   fun testFindIntraBundleBoundaries_WhenFishBase_ShouldReturnNoBoundaries() {
      val fish = FISH.make(1)
      val boundaryFinder = fromMonoBundleFigure(fish)
      val boundaries = boundaryFinder.findInternalBoundaries()
      assertTrue(boundaries.isEmpty())
   }
   @Test
   fun testFindIntraBundleBoundaries_WhenBirdBase_ShouldReturnNoBoundaries() {
      val bird = BIRD.make(1)
      val boundaryFinder = fromMonoBundleFigure(bird)
      val boundaries = boundaryFinder.findInternalBoundaries()
      assertTrue(boundaries.isEmpty())
   }
   @Test
   fun testFindIntraBundleBoundaries_WhenBoatBase_ShouldReturnNoBoundaries() {
      val boat = BOAT.make(1)
      val boundaryFinder = fromMonoBundleFigure(boat)
      val boundaries = boundaryFinder.findInternalBoundaries()
      assertTrue(boundaries.isEmpty())
   }
   @Test
   fun testFindIntraBundleBoundaries_WhenBirdBlintzBase_ShouldReturnNoBoundaries() {
      val birdBlintz = BIRD_BLINTZ.make(1)
      val boundaryFinder = fromMonoBundleFigure(birdBlintz)
      val boundaries = boundaryFinder.findInternalBoundaries()
      assertTrue(boundaries.isEmpty())
   }
   @Test
   fun testFindIntraBundleBoundaries_WhenDiamondBase_ShouldReturnNoBoundaries() {
      val diamond = DIAMOND.make(1)
      val boundaryFinder = fromMonoBundleFigure(diamond)
      val boundaries = boundaryFinder.findInternalBoundaries()
      assertThat(boundaries.size, `is`(2))
      //both boundaries are comprised of two edges
      assertTrue(boundaries.all { it.edges.size == 2 } )
   }
   /**
    * This is a rectangle, with two edge-to-edge parallel folds, making an accordion
    * of three faces.
    *
    * Each fold is, obviously, a boundary.
    *
    *
    * Unfolded:
    *
    * v1----v2----v3----v4
    * |      |     |     |
    * |      |     |     |
    * v5----v6----v7----v8
    *
    */
   @Test
   fun testFindIntraBundleBoundaries_WhenThreeFaceAccordion_ShouldReturnTwoBoundaries() {
      val v1 = Vertex(0.0, 1.0, 0.0)
      val v2 = Vertex(1.0, 1.0, 0.0)
      val v3 = Vertex(0.0, 1.0, 0.0)
      val v4 = Vertex(1.0, 1.0, 0.0)

      val v5 = Vertex(0.0, 0.0, 0.0)
      val v6 = Vertex(1.0, 0.0, 0.0)
      val v7 = Vertex(0.0, 0.0, 0.0)
      val v8 = Vertex(1.0, 0.0, 0.0)
      val topFace = Face(v1, v2, v6, v5)
      val innerFace = Face(v2, v3, v7, v6)
      val bottomFace = Face(v3, v4, v8, v7)
      val bundle = Bundle(xyPlane_NormalTowardsZPositive, setOf(topFace, innerFace, bottomFace),
                 mapOf(bottomFace to setOf(innerFace, topFace), innerFace to setOf(topFace) ) )
      val accordion = Figure(bundle)
      val boundaryFinder = fromMonoBundleFigure(accordion)
      val boundaries = boundaryFinder.findInternalBoundaries()
      assertThat(boundaries.size, `is`(2))
   }
   /**
    * In this case, each chunk is made of two faces:
    * The paper is first folded by the middle, then folded in a three part accordion as the other
    * figure.
    *
    * Should find two boundaries where each has two edges
    */
   @Test
   fun testFindIntraBundleBoundaries_WhenThreeChunkAccordion_ShouldReturnTwoBoundaries() {
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
      val boundaryFinder = fromMonoBundleFigure(accordion)
      val boundaries = boundaryFinder.findInternalBoundaries()
      assertThat(boundaries.size, `is`(2))
      //both boundaries are comprised of two edges
      assertTrue(boundaries.all { it.edges.size == 2 } )
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
   fun testFindIntraBundleBoundaries_WhenThreeChunkFigure_ShouldReturnNoInternalBoundaries() {
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

      val topFaceOfFirstChunk = Face(v1, v2, v6, v5)
      val bottomFaceOfFirstChunk = Face(v1, v2, v10, v9)

      val topFaceOfSecondChunk = Face(v2, v10, v11, v3)
      val bottomFaceOfSecondChunk = Face(v2, v6, v7, v3)

      val topFaceOfThirdChunk = Face(v3, v4, v8, v7)
      val bottomFaceOfThirdChunk = Face(v3, v4, v12, v11)

      val firstBundle = Bundle(xyPlane_NormalTowardsZPositive,
              setOf(topFaceOfFirstChunk, bottomFaceOfFirstChunk),
              mapOf(bottomFaceOfFirstChunk to setOf(topFaceOfFirstChunk)))
      val secondBundle = Bundle(yzPlaneTowardsXPos,
              setOf(bottomFaceOfSecondChunk, topFaceOfSecondChunk),
              mapOf(bottomFaceOfSecondChunk to setOf(topFaceOfSecondChunk)))
      val thirdBundle = Bundle(xyPlane_NormalTowardsZPositive,
              setOf(bottomFaceOfThirdChunk, topFaceOfThirdChunk),
              mapOf(bottomFaceOfThirdChunk to setOf(topFaceOfThirdChunk)))

      val figure = Figure(setOf(firstBundle, secondBundle, thirdBundle))
      val boundaryFinderFirstBundle = InternalChunkBoundaryFinder(figure, firstBundle)
      val boundariesOfFirstBundle = boundaryFinderFirstBundle.findInternalBoundaries()
      assertTrue(boundariesOfFirstBundle.isEmpty())

   }
}