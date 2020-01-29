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

import com.moduleforge.libraries.geometry._3d.Plane
import com.moduleforge.libraries.geometry._3d.Point
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.OrigamiBase.*
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class ChunkLoosenerTest {
   private lateinit var xyPlane_NormalTowardsZPositive: Plane

   @Before
   fun setUp(){
      val antiClockwisePointsAsSeenFromZPositive =
              listOf(Point(0, 0, 0), Point(1, 0, 0), Point(1, 1, 0))
      xyPlane_NormalTowardsZPositive = Plane.planeFromOrderedPoints(antiClockwisePointsAsSeenFromZPositive)
   }


   /**
    * In a single face figure there is nothing to loosen.
    *
    * Furthermore, we expect the same exact Figure object. There is no reason to change it in any way
    */
   @Test
   fun whenLooseningSingleFaceFigure_ShouldReturnSameFigure() {
      val square = SQUARE.make(1.0)
      val loosener = ChunkLoosener(square)
      val loosenedFigure = loosener.loosenedFigure
      assertThat(loosenedFigure, `is`(square))
   }
   /**
    * In this case we have a more complicated figure, but since there are "chunks" either,
    * there is also no reason to return anything other than the same Figure object.
    */
   @Test
   fun whenLooseningWaterbombBase_ShouldReturnSameFigure() {
      val waterbomb = WATERBOMB.make(1.0)
      val loosener = ChunkLoosener(waterbomb)
      val loosenedFigure = loosener.loosenedFigure
      assertThat(loosenedFigure, `is`(waterbomb))
   }
   /**
    * In this case a blintz base has four flaps and the figure should end up as
    * five bundles
    */
   @Test
   fun whenLooseningBlintzBase_ShouldReturnLoosenedFigureWithFiveBundles() {
      val blintz = BLINTZ.make(1.0)
      val loosener = ChunkLoosener(blintz)
      val loosenedFigure = loosener.loosenedFigure
      val loosenedBundles = loosenedFigure.bundles
      assertThat(loosenedBundles.size, `is`(5))
   }
   /**
    * The diamond base has two flaps
    */
   @Test
   fun whenLooseningDiamondBase_ShouldReturnLoosenedFigureWithThreeBundles() {
      val diamond = DIAMOND.make(1.0)
      val loosener = ChunkLoosener(diamond)
      val loosenedFigure = loosener.loosenedFigure
      val loosenedBundles = loosenedFigure.bundles
      assertThat(loosenedBundles.size, `is`(3))
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
    * The particularity about this test is that there are children of children (or, more specifically,
    * a child of a child).
    *
    */
   @Test
   fun whenLooseningAccordionOfThreeFaces_InOneBundle_AndTopFaceHasLargestArea_Calculated() {
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
      val loosener = ChunkLoosener(accordion)
      val loosenedFigure = loosener.loosenedFigure
      val loosenedBundles = loosenedFigure.bundles
      assertThat(loosenedBundles.size, `is`(3))
      val allVerticesOfLoosenedBundle = loosenedBundles.flatMap { it.vertices }.toSet()
      assertThat(allVerticesOfLoosenedBundle.size, `is`(8)) //no duplicated vertices
   }
}