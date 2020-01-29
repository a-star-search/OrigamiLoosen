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

package com.whitebeluga.origami.loosen

import java.lang.Math.PI

object Constants {
   const val DEFAULT_OPEN_UP_ANGLE = PI / 30
   /**
    * Instead of using an angle, we can have the ratio of
    * length of a segment to the distance one of the ends of
    * the edges should move to, perpendicular to the segment
    *
    * This is faster to calculate, easier to code and used for
    * algorithms that do approximations
    * where we don't need the precision of trigonometry
    *
    * This is ratio of the depth to that average dimension
    */
   //I'm using the formula of the sine approximation for small angles (which is the angle itself)
   // that way we can 1) use const 2) depend on the angle constant 3) more efficient than trigonometric functions (ok, that would be only one use)
   const val DEFAULT_OPEN_UP_SHIFT_RATIO = DEFAULT_OPEN_UP_ANGLE
   /**
    * works together with the shift ratio,
    * after a point "shifts", it should "travel back" along the segment
    * this reduces the deformation than if we just shift boundary to create depth in a layer
    *
    * the value is an approximation of the cosine of the open up angle for small values
    * of an angle
    */
   //const val DEFAULT_OPEN_UP_SHORTENING_RATIO = 1 - ((DEFAULT_OPEN_UP_ANGLE * DEFAULT_OPEN_UP_ANGLE) / 2.0)
   const val CORNER_OPEN_UP_ANGLE = DEFAULT_OPEN_UP_ANGLE / 4.5
}