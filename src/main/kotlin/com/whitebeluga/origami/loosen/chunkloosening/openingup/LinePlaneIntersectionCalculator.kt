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

import com.moduleforge.libraries.geometry.Geometry
import com.moduleforge.libraries.geometry._3d.LineSegment
import com.moduleforge.libraries.geometry._3d.Plane
import com.moduleforge.libraries.geometry._3d.Point

/**
 * I don't want these methods to be part of the geometry library:
 *
 * They are basically inaccurate and don't account for corner cases
 * such as the line being contained in the plane or parallel to it.
 *
 * (And I don't want to spend time refining it nor do I trust any library!)
 *
 * It will be used only as part of the loosening algorithm.
 */
object LinePlaneIntersectionCalculator {
   fun findLineSegmentAndPlaneIntersection(s: LineSegment, plane: Plane): Point? {
      val linePlaneIntersection = findLineAndPlaneIntersection(s.endsAsList[0], s.endsAsList[1], plane) ?: return null
      return if(s.contains(linePlaneIntersection)) linePlaneIntersection else null
   }
   /**
    * The line here is given by two points.
    * It founds the intersection with the line, not with the segment delimited by those two points!
    */
   fun findLineAndPlaneIntersection(p: Point, q: Point, plane: Plane): Point? {
      val equation = plane.equation
      val a = equation[0]
      val b = equation[1]
      val c = equation[2]
      val d = equation[3]
      val tDenom = a * (q.x - p.x) + b * (q.y - p.y) + c * (q.z - p.z)
      if (Geometry.almostZeroFloatPrecision(tDenom))
         return null
      val t = - ( a * p.x + b * p.y + c * p.z + d ) / tDenom
      return Point(p.x + t * (q.x - p.x), p.y + t * (q.y - p.y), p.z + t * (q.z - p.z))
   }
}