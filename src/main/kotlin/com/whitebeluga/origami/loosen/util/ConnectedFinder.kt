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

package com.whitebeluga.origami.loosen.util

internal object ConnectedFinder {
   fun <T> findConnectedThrough(t: T, connectedTo: (t: T) -> Set<T>): Set<T> =
           connectedThrough_Recursive(setOf(), setOf(t), connectedTo)
   private fun <T> connectedThrough_Recursive(connectedAndExplored: Set<T>, connectedAndUnexplored: Set<T>,
                                              connectedTo: (t: T) -> Set<T>): Set<T> {
      if(connectedAndUnexplored.isEmpty())
         return connectedAndExplored
      val toExplore = connectedAndUnexplored.first()
      val newConnectedAndExplored = connectedAndExplored + toExplore
      val newConnections: MutableSet<T> = connectedTo(toExplore).toMutableSet()
      newConnections.removeAll(connectedAndExplored)
      val newConnectedAndUnexplored = (connectedAndUnexplored + newConnections) - toExplore
      return connectedThrough_Recursive(newConnectedAndExplored, newConnectedAndUnexplored, connectedTo)
   }
}