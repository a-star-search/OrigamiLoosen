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

import com.moduleforge.libraries.geometry._3d.LineSegment
import com.moduleforge.libraries.geometry._3d.Point
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.loosen.chunkloosening.openingup.LinePlaneIntersectionCalculator.findLineSegmentAndPlaneIntersection

/**
 * This class works in an approximate way: it is designed to be used by the loosening of chunks algorithm
 * to avoid opening up a chunk so much that it collides with other chunk, hence it has rendering purposes and
 * needs little precision.
 *
 * Throughout the project most operations aim to provide a few orders of magnitude over double precision, and when
 * not it is clearly specified. Rendering algorithms (and this class is part of one) are an exception to this rule.
 */
internal object CollisionResolver {
   /**
    * Each set of faces must be coplanar.
    *
    * It may be the faces of a bundle or the faces of some other data structure used in an intermediate step of an
    * algorithm such as the chunk loosening algorithm.
    *
    */
   fun isThereCollision(a: Set<Face>, b: Set<Face>): Boolean =
      a.any { face1 -> b.any { face2 -> isThereCollision(face1, face2) } }
   /**
    * Returns true if both faces collide, false otherwise.
    */
   fun isThereCollision(a: Face, b: Face): Boolean {
      /*
      Collision of a with b is the same as collision of b with a. Only one case needs to be studied.
      The choice is arbitrary
       */
      val segments: Set<LineSegment> = a.edges
      return isThereCollision(b, segments)
   }
   private fun isThereCollision(f: Face, segments: Set<LineSegment>): Boolean =
      segments.any { isThereCollision(f, it) }
   private fun isThereCollision(f: Face, segment: LineSegment): Boolean {
      val plane = f.polygonPlane
      val intersection = findLineSegmentAndPlaneIntersection(segment, plane) ?: return false
      val segmentIntersectsFace = isPointInsideFace(f, intersection)
      return segmentIntersectsFace
   }
   /**
    If the point is *inside* the face, then return true,
    however if the point is anywhere near an edge or a vertex of a face and by near I mean float precision (6 decimals)
    then it will return false.

    This algorithm is part of a rendering algorithm. We want to be sure that two faces that share a vertex
    (a pretty common scenario) can be both opened, so we increase the error margin to ensure that happens.
    If that causes two opened faces to collide for that minute distance, then it is not a big deal for the application
    user.
    */
   fun isPointInsideFace(f: Face, p: Point): Boolean {
      val contained = f.contains(p)
      if(!contained)
         return false
      val vertices = f.vertices
      val epsilon = 10e-5
      val nextToAVertex = vertices.any { it.distance(p) < epsilon }
      if(nextToAVertex)
         return false
      return f.edges.none { it.distanceFrom(p) < epsilon }
   }
}