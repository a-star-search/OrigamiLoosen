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

import com.whitebeluga.origami.loosen.datastructures.TreeNode
import com.whitebeluga.origami.loosen.util.ConnectedFinderTest.A.Companion.makeLeaf
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class ConnectedFinderTest {
   private val allDescendents = fun (node: A) = node.children
   private val evenValueDescendents = fun (node: A) = node.children.filter { it.value % 2 == 0 }.toSet()
   @Test
   fun singleNode_Calculated() {
      val node = makeLeaf(0)
      val connected = ConnectedFinder.findConnectedThrough(node, allDescendents)
      val expectedConnected = setOf(node)
      assertThat(connected, `is`(expectedConnected))
   }
   @Test
   fun rootAndTwoChildren_Calculated() {
      val child1 = makeLeaf(1)
      val child2 = makeLeaf(2)
      val parent = A(setOf(child1, child2), 0)
      val connected = ConnectedFinder.findConnectedThrough(parent, allDescendents)
      val expectedConnected = setOf(child1, child2, parent)
      assertThat(connected, `is`(expectedConnected))
   }
   @Test
   fun whenComplexTree_AndEvenValueDescendentsFunction_ShouldBeCorrectlyCalculated() {
      val leaf1 = makeLeaf(1)
      val leaf2 = makeLeaf(4)
      val leaf3 = makeLeaf(6)
      val leaf4 = makeLeaf(1)
      val firstLevel1 = A(setOf(leaf1, leaf2), 2)
      val firstLevel2 = A(setOf(leaf3, leaf4), 3)
      val parent  = A(setOf(firstLevel1, firstLevel2), 13)
      val connected = ConnectedFinder.findConnectedThrough(parent, evenValueDescendents)
      val expectedConnected = setOf(parent, firstLevel1, leaf2)
      assertThat(connected, `is`(expectedConnected))
   }
   private class A(override val children: Set<A>, val value: Int) : TreeNode<A> {
      companion object {
         fun makeLeaf(value: Int) = A(setOf(), value)
      }
   }
}