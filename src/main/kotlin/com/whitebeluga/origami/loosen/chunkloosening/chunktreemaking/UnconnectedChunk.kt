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

package com.whitebeluga.origami.loosen.chunkloosening.chunktreemaking

import com.whitebeluga.origami.figure.Bundle
import com.whitebeluga.origami.figure.component.Face
import com.whitebeluga.origami.loosen.chunkloosening.ChunkBoundary

/**
 * This class contains the information of a chunk: faces and boundaries.
 *
 * It only lacks the connections to other chunks and it's the previous step to calculate the full chunk
 * tree node structure.
 */
internal class UnconnectedChunk(
        val bundle: Bundle,
        val faces: Set<Face>,
        /**
         * Boundaries with chunks of the same bundle
         * The side parameter is the side occupied by this chunk (and, thus, not the side of the chunk
         * this is connected to)
         */
        val internalBoundaries: Set<Pair<Bundle.Side, ChunkBoundary>> = setOf(),
        /**
         * boundaries with chunks that belong to other bundle
         */
        val interBundleBoundaries: Set<ChunkBoundary> = setOf() )