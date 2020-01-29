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

import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.figure.component.Vertex
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class CollisionResolverTest {
   @Test
   fun whenTwoFacesThatDoNotCollide_ShouldReturnFalse() {
      val v1 = Vertex(0.0, 0.0, 0.0)
      val v2 = Vertex(1.0, 0.0, 0.0)
      val v3 = Vertex(1.0, 1.0, 0.0)
      val face1 = Face(v1, v2, v3)

      val v4 = Vertex(2.0, 0.0, 0.0)
      val v5 = Vertex(3.0, 0.0, 0.0)
      val v6 = Vertex(3.0, 1.0, 0.0)
      val face2 = Face(v4, v5, v6)

      assertFalse(CollisionResolver.isThereCollision(face1, face2))
   }
   @Test
   fun whenTwoFacesInDifferentPlanesThatCollide_ShouldReturnTrue() {
      val v1 = Vertex(0.0, 0.0, 0.0)
      val v2 = Vertex(1.0, 0.0, 0.0)
      val v3 = Vertex(1.0, 1.0, 0.0)
      val v4 = Vertex(0.0, 1.0, 0.0)
      val face1 = Face(listOf(v1, v2, v3, v4))

      val v5 = Vertex(0.5, 0.5, 1.0)
      val v6 = Vertex(0.5, 0.5, -1.0)
      val v7 = Vertex(2.0, 0.5, 0.0)
      val face2 = Face(v5, v6, v7)

      assertTrue(CollisionResolver.isThereCollision(face1, face2))
   }
   @Test
   fun whenTwoFacesShareOnlyAVertex_ShouldReturnFalse() {
      val v1 = Vertex(0.0, 0.0, 0.0)
      val v2 = Vertex(1.0, 0.0, 0.0)
      val v3 = Vertex(1.0, 1.0, 0.0)
      val face1 = Face(v1, v2, v3)

      val v4 = Vertex(1.0, 0.0, 0.0)
      val v5 = Vertex(1.0, 0.0, 1.0)
      val v6 = Vertex(2.0, 0.0, 0.0)
      val face2 = Face(v4, v5, v6)

      assertFalse(CollisionResolver.isThereCollision(face1, face2))
   }
}