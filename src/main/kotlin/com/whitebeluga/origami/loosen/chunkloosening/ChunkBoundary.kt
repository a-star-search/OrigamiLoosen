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

package com.whitebeluga.origami.loosen.chunkloosening

import com.whitebeluga.origami.figure.component.Edge

/**
 * Boundary between "chunks". Either flaps of the same bundle or to a flap of another bundle.
 */
internal class ChunkBoundary(val edges: Set<Edge>) {
   private constructor(edge: Edge): this(setOf(edge))
   override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false
      other as ChunkBoundary
      return edges == other.edges
   }
   override fun hashCode(): Int = edges.hashCode()
   companion object {
      fun chunkBoundaryFromEdge(edge: Edge) = ChunkBoundary(edge)
   }
}