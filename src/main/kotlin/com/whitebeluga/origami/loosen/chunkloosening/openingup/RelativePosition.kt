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

/**
 * This enumeration is used in the algorithm that ensures the same relative position between two flaps after the flaps
 * (or, more generally, chunks) have been opened up.
 *
 * For instance, a flap 'A' that is on top of a flap 'B' in the flat figure, should remain on top after opening up.
 *
 * In this case above and below are relative to the opening direction vector, and have no relationship with the
 * bundle normal or the concepts of 'top' and 'bottom' of the bundle, which are both arbitrary.
 */
enum class RelativePosition {
   ABOVE, BELOW
}