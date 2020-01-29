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

import com.moduleforge.libraries.geometry.Geometry.almostZero
import com.moduleforge.libraries.geometry._3d.Plane
import com.moduleforge.libraries.geometry._3d.Point
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * This class is devoted to the part of the loosening algorithm in charge of maintaining the relative positions
 * of chunks after opening them.
 *
 * For example, for two folded faces that overlap and open in the same direction, if they are open the same angle,
 * the bigger one will end up on top of the smaller, regardless of their relative position when flat.
 *
 * The algorithm aims to prevent that by either opening the big one at a smaller angle or opening the small one
 * at a bigger angle.
 */
class ChunkLoosener_ChunkPositionConflictTest {
   private lateinit var xyPlane_NormalTowardsZPositive: Plane

   @Before
   fun setUp(){
      val antiClockwisePointsAsSeenFromZPositive =
              listOf(Point(0, 0, 0), Point(1, 0, 0), Point(1, 1, 0))
      xyPlane_NormalTowardsZPositive = Plane.planeFromOrderedPoints(antiClockwisePointsAsSeenFromZPositive)
   }

   /**
    * The figure is like this:
    *
    *         1 ________2
    *        /\         \
    *      /  \         \
    *    /  8 \_________\ 3
    * 6 /_____\        /
    *  \   \  7      /
    *  \   \       /
    *  \   \     /
    *  \   \   /
    *  \   \ /
    *  \___/ 4
    * 5
    *
    *
    * I won't bother naming the test, just compare to the figure in this javadoc
    */
   @Test
   fun conflict1_Calculated() {
      val v1 = Vertex(2.0, 10.0, 0.0)
      val v2 = Vertex(10.0, 10.0, 0.0)
      val v3 = Vertex(10.0, 9.0, 0.0)
      val v4 = Vertex(1.5, 0.0, 0.0)
      val v5 = Vertex(0.0, 0.0, 0.0)
      val v6 = Vertex(0.0, 8.0, 0.0)
      val v7 = Vertex(2.0, 8.0, 0.0)
      val v8 = Vertex(1.5, 9.0, 0.0)
      val bottomFace = Face(v1, v2, v3, v4, v5, v6)
      val middleFace = Face(v3, v8, v4)
      val topFace = Face(v1, v6, v7)
      val bundle = Bundle(xyPlane_NormalTowardsZPositive,
              setOf(topFace, middleFace, bottomFace),
              mapOf(bottomFace to setOf(middleFace, topFace),
                      middleFace to setOf(topFace) ) )
      val figure = Figure(bundle)
      val loosener = ChunkLoosener(figure)
      val loosenedFigure = loosener.loosenedFigure
      val loosenedBundles = loosenedFigure.bundles
      //Assertions:
      //three bundles
      assertThat(loosenedBundles.size, `is`(3))
      //one face per bundle
      loosenedBundles.forEach {
         assertThat(it.faces.size, `is`(1))
      }
      val loosenedFaces = loosenedBundles.map { it.faces.first() }.toSet()
      val bottomLoosenedFace = loosenedFaces.first{ it.vertices.size == 6}

      val zOfBottomLoosenedFace = bottomLoosenedFace.vertices.map { it.z }
      assertTrue(zOfBottomLoosenedFace.all { almostZero(it) } )

      val middleAndTopLoosenedFace = loosenedFaces.filter{ it.vertices.size == 3}.toSet()
      val middleLoosenedFace = middleAndTopLoosenedFace.maxBy { it.area() }!!

      //I use the largest z, remember the bundle is stacked towards z pos
      val zOfMiddleLoosenedFace = middleLoosenedFace.vertices.map { it.z }
      val largestZOfMiddleLoosenedFace = zOfMiddleLoosenedFace.max()!!

      val topLoosenedFace = middleAndTopLoosenedFace.minBy { it.area() }!!

      val zOfTopLoosenedFace = topLoosenedFace.vertices.map { it.z }
      val largestZOfTopLoosenedFace = zOfTopLoosenedFace.max()!!

      assertTrue(largestZOfTopLoosenedFace > largestZOfMiddleLoosenedFace)
   }
}