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

 This package is dedicated to the algorithm of opening up the chunks or flaps by rotating them
 given a chunk tree.

 This algorithm is somewhat complicated by trying to keep the position of the chunks in the same order than
 in the flat figure: a chunk on top of another one must remain on top when opened.

 The exact way to maintain positions is surprisingly complicated (although certainly doable).

 We are using an algorithm that, while not 100% exact, produces a decent result. Simply put, for every new chunk to be
 opened, if the default opening angle yields it out of order, correct the angle in one  way or another until the order
 is restored.

 */