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

import com.moduleforge.libraries.geometry._3d.Line.X_AXIS
import com.moduleforge.libraries.geometry._3d.Line.linePassingBy
import com.moduleforge.libraries.geometry._3d.Plane
import com.moduleforge.libraries.geometry._3d.Plane.planeFromOrderedPoints
import com.moduleforge.libraries.geometry._3d.Point
import com.moduleforge.libraries.geometry._3d.Vector
import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.Math.PI

/**
 * This class contains tests for the bundle rotation function of the chunk loosener
 */
class ChunkLoosener_BundleRotationsTest {
   private lateinit var xyPlane_NormalTowardsZPositive: Plane
   private lateinit var v1: Vertex
   private lateinit var v2: Vertex
   private lateinit var v3: Vertex
   private lateinit var faceInXYPlane: Face
   private lateinit var monoFaceBundle: Bundle
   @Before
   fun setUp() {
      val antiClockwisePointsAsSeenFromZPositive =
              listOf(Point(0, 0, 0), Point(1, 0, 0), Point(1, 1, 0))
      xyPlane_NormalTowardsZPositive = planeFromOrderedPoints(antiClockwisePointsAsSeenFromZPositive)
      v1 = Vertex(0.0, 0.0, 0.0)
      v2 = Vertex(1.0, 0.0, 0.0)
      v3 = Vertex(1.0, 1.0, 0.0)
      faceInXYPlane = Face(v1, v2, v3)
      monoFaceBundle = Bundle(xyPlane_NormalTowardsZPositive, faceInXYPlane)
   }
   /**
    * In this example we do a single rotation of a single face bundle.
    *
    * We check that the result is correct
    */
   @Test
   fun whenSingleRotation_OfHalfPIRadian_OfSingleFace_FaceShouldEndUpInXZPlane(){
      val towardsZPos = Vector(0.0, 0.0, 1.0)
      val straightAngle = PI / 2.0
      val straightAngleRotation = Rotation(X_AXIS, towardsZPos, straightAngle)
      val rotations = listOf(straightAngleRotation)
      val rotated = ChunkLoosener.doRotations(rotations, monoFaceBundle)
      val rotatedBundle = rotated.first
      assertThat(rotatedBundle.faces.size, `is`(1)) //just one face
      val rotatedFace = rotatedBundle.faces.first()
      val verticesOfRotatedFace = rotatedFace.vertices.toSet()
      val originalToRotated = rotated.second
      assertEquals(verticesOfRotatedFace, originalToRotated.values.toSet()) //vertices and map values match
      //ensure rotated point position is correctly calculated
      val expectedRotatedV1Position = Point(v1)
      val actualRotatedV1 = originalToRotated[v1]!!
      assertTrue(actualRotatedV1.epsilonEquals(expectedRotatedV1Position))
      val expectedRotatedV2Position = Point(v2)
      val actualRotatedV2 = originalToRotated[v2]!!
      assertTrue(actualRotatedV2.epsilonEquals(expectedRotatedV2Position))
      val expectedRotatedV3Position = Point(1.0, 0.0, 1.0)
      val actualRotatedV3 = originalToRotated[v3]!!
      assertTrue(actualRotatedV3.epsilonEquals(expectedRotatedV3Position))
   }
   @Test
   fun whenTwoRotations_OfHalfPIRadian_SameAxis_OfSingleFace_FaceShouldEndUpRotatedPIRadians(){
      val towardsZPos = Vector(0.0, 0.0, 1.0)
      val straightAngle = PI / 2.0
      val firstRotation = Rotation(X_AXIS, towardsZPos, straightAngle)
      val towardsYNeg = Vector(0.0, -1.0, 0.0)
      val secondRotation = Rotation(X_AXIS, towardsYNeg, straightAngle)
      val rotations = listOf(firstRotation, secondRotation)
      val rotated = ChunkLoosener.doRotations(rotations, monoFaceBundle)
      val originalToRotated = rotated.second
      //ensure rotated point position is correctly calculated
      val expectedRotatedV1Position = Point(v1)
      val actualRotatedV1 = originalToRotated[v1]!!
      assertTrue(actualRotatedV1.epsilonEquals(expectedRotatedV1Position))
      val expectedRotatedV2Position = Point(v2)
      val actualRotatedV2 = originalToRotated[v2]!!
      assertTrue(actualRotatedV2.epsilonEquals(expectedRotatedV2Position))
      val expectedRotatedV3Position = Point(1.0, -1.0, 0.0)
      val actualRotatedV3 = originalToRotated[v3]!!
      assertTrue(actualRotatedV3.epsilonEquals(expectedRotatedV3Position))
   }
   /**
    * Two rotations but in this case the rotations are not around the same axis.
    *
    * Single face bundle.
    */
   @Test
   fun rotationComplexCase1(){
      val towardsZPos = Vector(0.0, 0.0, 1.0)
      val straightAngle = PI / 2.0
      val firstRotation = Rotation(X_AXIS, towardsZPos, straightAngle)
      val secondRotationAxis = linePassingBy(Point(1.0, 0.0, 0.0), Point(1.0, 0.0, 1.0))
      val towardsYNeg = Vector(0.0, -1.0, 0.0)
      val secondRotation = Rotation(secondRotationAxis, towardsYNeg, straightAngle)
      val rotations = listOf(firstRotation, secondRotation)
      val rotated = ChunkLoosener.doRotations(rotations, monoFaceBundle)
      val originalToRotated = rotated.second
      //ensure rotated point position is correctly calculated
      val expectedRotatedV1Position = Point(1.0, -1.0, 0.0)
      val actualRotatedV1 = originalToRotated[v1]!!
      assertTrue(actualRotatedV1.epsilonEquals(expectedRotatedV1Position))
      val expectedRotatedV2Position = Point(v2)
      val actualRotatedV2 = originalToRotated[v2]!!
      assertTrue(actualRotatedV2.epsilonEquals(expectedRotatedV2Position))
      val expectedRotatedV3Position = Point(1.0, 0.0, 1.0)
      val actualRotatedV3 = originalToRotated[v3]!!
      assertTrue(actualRotatedV3.epsilonEquals(expectedRotatedV3Position))
   }
   /**
    * In this example we do a single rotation of a double face bundle.
    */
   @Test
   fun whenSingleRotation_OfHalfPIRadian_OfDoubleFaceBundle_Calculated(){
      val towardsZPos = Vector(0.0, 0.0, 1.0)
      val straightAngle = PI / 2.0
      val straightAngleRotation = Rotation(X_AXIS, towardsZPos, straightAngle)
      val rotations = listOf(straightAngleRotation)
      val v1BigFace = Vertex(0.0, 0.0, 0.0)
      val v2BigFace = Vertex(2.0, 0.0, 0.0)
      val v3BigFace = Vertex(1.0, 1.0, 0.0)
      val bigFace = Face(v1BigFace, v2BigFace, v3BigFace)
      val bundle = Bundle(xyPlane_NormalTowardsZPositive,
              setOf(faceInXYPlane, bigFace),
              mapOf(faceInXYPlane to setOf(bigFace)) ) //the big face is the one on top
      val rotated = ChunkLoosener.doRotations(rotations, bundle)
      val rotatedBundle = rotated.first
      assertThat(rotatedBundle.faces.size, `is`(2)) //still two faces
      /*
      test that the big face is the top face after rotation (the ordering of faces is not changed by a rotation)
       */
      assertThat(rotatedBundle.topFaces().size, `is`(1))
      val topFace = rotatedBundle.topFaces().first()
      val bottomFace = (rotatedBundle.faces - topFace).first()
      val areaOfTopFace = topFace.area()
      val areaOfBottomFace = bottomFace.area()
      assertTrue(areaOfTopFace > areaOfBottomFace)
   }
}