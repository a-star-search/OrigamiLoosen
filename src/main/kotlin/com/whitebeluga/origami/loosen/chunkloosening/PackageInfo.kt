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

/**

 The figure is first decomposed into "chunks" or "flaps". Then we traverse the tree structure (width or depth first,
 does not make a difference) and, for each chunk, we rotate it so that the connecting boundary
 of the parent flap already opened matches the boundary in the new flap to open.

 Note that this loosening algorithm, unlike others, yield a correct "Figure" object, since the faces are not deformed
 and there are still bundles.

 For this reason it is also the first algorithm to be applied to the figure when loosening it.

 */