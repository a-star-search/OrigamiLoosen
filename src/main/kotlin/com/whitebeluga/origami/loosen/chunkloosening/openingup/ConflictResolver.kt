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

package com.whitebeluga.origami.loosen.chunkloosening.openingup

import com.moduleforge.libraries.geometry._3d.Point
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.loosen.chunkloosening.Chunk
import com.whitebeluga.origami.loosen.chunkloosening.openingup.LinePlaneIntersectionCalculator.findLineAndPlaneIntersection
import com.whitebeluga.origami.loosen.chunkloosening.openingup.RelativePosition.ABOVE
import com.whitebeluga.origami.loosen.chunkloosening.openingup.RelativePosition.BELOW

/**
 * This class encapsulates the algorithm of determining if a chunk that we are trying to rotate, for a given angle,
 * conflicts with some other already rotated chunks.
 *
 * A conflicts can mean two things: the chunk we are trying to rotates "collides" with another rotated chunk.
 * Or it doesn't collide but their relative positions are wrong: ie in the flat figure 'a' is below 'b' and
 * after rotation 'b' is below 'a'.
 *
 * ---
 *
 * What this algorithm does is it returns a set of relative positions with which the rotated chunk conflicts (if any).
 *
 * This is the meaning: if 'above' is returned, it means that there is conflict with at least one already rotated
 * chunk that should be above but it's not.
 *
 * In other words, if above is returned, the chunk we are trying to rotate is above, or colliding with a
 */
internal class ConflictResolver(private val preRotation: PreRotation,
                                private val chunk: Chunk,
                                private val rotationsOfAncestors: List<Rotation>,
                                private val angle: Double) {
   private val openedUpFacesThatShouldBeAbove = preRotation.openedUpFacesThatShouldBeAbove
   private val openedUpFacesThatShouldBeBelow = preRotation.openedUpFacesThatShouldBeBelow
   private val facesOfRotatedBundle: Set<Face>

   init {
      facesOfRotatedBundle = makeFacesOfRotatedBundle()
   }
   private fun makeFacesOfRotatedBundle(): Set<Face> {
      val rotation = Rotation(preRotation.line, preRotation.rotationDirection, angle)
      val rotations = rotationsOfAncestors + rotation
      val rotatedBundle = ChunkLoosener.rotate(chunk, rotations).first
      return rotatedBundle.faces
   }
   fun conflicts(): Set<RelativePosition> {
      val isThereConflictWithFacesThatShouldBeAbove =
              isThereConflictWithAlreadyRotatedFaces(facesOfRotatedBundle, openedUpFacesThatShouldBeAbove, ABOVE)
      val isThereConflictWithFacesThatShouldBeBelow =
              isThereConflictWithAlreadyRotatedFaces(facesOfRotatedBundle, openedUpFacesThatShouldBeBelow, BELOW)
      val noConflicts = !isThereConflictWithFacesThatShouldBeAbove && !isThereConflictWithFacesThatShouldBeBelow
      if(noConflicts)
         return emptySet()
      if(isThereConflictWithFacesThatShouldBeAbove && isThereConflictWithFacesThatShouldBeBelow)
         return setOf(BELOW, ABOVE)
      if(isThereConflictWithFacesThatShouldBeAbove)
         return setOf(ABOVE)
      return setOf(BELOW)
   }
   private fun isThereConflictWithAlreadyRotatedFaces(rotated: Set<Face>,
                                                      alreadyRotated: Set<Face>,
                                                      positionOfAlreadyRotated: RelativePosition): Boolean =
      rotated.any { rotatedFace ->
         alreadyRotated.any { isThereConflictWithAlreadyRotatedFace(rotatedFace, it, positionOfAlreadyRotated) }
      }
   private fun isThereConflictWithAlreadyRotatedFace(rotated: Face, alreadyRotated: Face, positionOfAlreadyRotated: RelativePosition): Boolean {
      val isThereCollision = CollisionResolver.isThereCollision(rotated, alreadyRotated)
      if(isThereCollision)
         return true
      val areFacesOutOfOrder = areFacesOutOfOrder(rotated, alreadyRotated, positionOfAlreadyRotated)
      return areFacesOutOfOrder
   }
   private fun areFacesOutOfOrder(rotated: Face, alreadyRotated: Face, positionOfAlreadyRotated: RelativePosition): Boolean {
      val rotatedVertexIsOutOfOrder = aRotatedVertexIsOutOfOrder(rotated, alreadyRotated, positionOfAlreadyRotated)
      if(rotatedVertexIsOutOfOrder)
         return true
      val alreadyRotatedVertexIsOutOfOrder = anAlreadyRotatedVertexIsOutOfOrder(rotated, alreadyRotated, positionOfAlreadyRotated)
      return alreadyRotatedVertexIsOutOfOrder
   }
   private fun aRotatedVertexIsOutOfOrder(rotated: Face, alreadyRotated: Face, positionOfAlreadyRotated: RelativePosition): Boolean =
      rotated.vertices.any { isVertexOutOfOrder(it, alreadyRotated, positionOfAlreadyRotated) }
   private fun anAlreadyRotatedVertexIsOutOfOrder(rotated: Face, alreadyRotated: Face, positionOfAlreadyRotated: RelativePosition): Boolean =
      alreadyRotated.vertices.any { isVertexOutOfOrder(rotated, it, positionOfAlreadyRotated) }

   private fun isVertexOutOfOrder(vertexOfRotated: Point, alreadyRotated: Face, positionOfAlreadyRotated: RelativePosition): Boolean {
      val intersectionOfPointWithFace = intersectionOfPointWithFace(vertexOfRotated, alreadyRotated)
              ?: return false // If there is no intersection, there is no problem. ie, the point is not 'out of order'
      val relativePositionOfPointOfRotatedFace =
              relativePositionOfPoint(vertexOfRotated, intersectionOfPointWithFace)
      val inTheCorrectPosition = positionOfAlreadyRotated != relativePositionOfPointOfRotatedFace
      return !inTheCorrectPosition
   }

   private fun isVertexOutOfOrder(rotated: Face, vertexOfAlreadyRotated: Point, positionOfAlreadyRotated: RelativePosition): Boolean {
      val intersectionOfPointWithFace = intersectionOfPointWithFace(vertexOfAlreadyRotated, rotated)
              ?: return false // If there is no intersection, there is no problem. ie, the point is not 'out of order'
      val relativePositionOfPointOfAlreadyRotatedFace =
              relativePositionOfPoint(vertexOfAlreadyRotated, intersectionOfPointWithFace)
      val inTheCorrectPosition = positionOfAlreadyRotated == relativePositionOfPointOfAlreadyRotatedFace
      return !inTheCorrectPosition
   }
   /**
    * Intersection following the opening up vector direction find out the intersection with the face (if there is an
    * intersection at all)
    */
   private fun intersectionOfPointWithFace(point: Point, face: Face): Point? {
      val direction = preRotation.rotationDirection
      val point2 = point.translate(direction)
      val plane = face.polygonPlane
      val intersection = findLineAndPlaneIntersection(point, point2, plane) ?: return null
      val isIntersectionInsideFace = CollisionResolver.isPointInsideFace(face, intersection)
      if(isIntersectionInsideFace)
         return intersection
      return null
   }
   /**
    * Using the pre-rotation opening vector as reference for direction,
    * it returns whether the point is above or below the plane.
    *
    * Below meaning "if the point travels in the vector direction it meets the plane" (because the plane is above)
    */
   private fun relativePositionOfPoint(point: Point, intersection: Point): RelativePosition {
      val direction = preRotation.rotationDirection
      val fromPointToPlane = point.vectorTo(intersection).normalize()
      val planeIsAbove = fromPointToPlane.dot(direction) > 0
      return if(planeIsAbove) BELOW else ABOVE
   }
}