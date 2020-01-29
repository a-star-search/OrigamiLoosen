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

package com.whitebeluga.origami.loosen

import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.loosen.chunkloosening.openingup.ChunkLoosener

/**
 * A loosened or opened up figure is different from a "Figure" class mainly in that it has fewer restrictions.
 *
 * It's little more than a bunch of faces to be rendered.
 *
 * For example, if a figure has several faces together and some of those inner faces would never be seen by an
 * observer from any angle, then we might remove them.
 *
 * This might be impossible in a Figure object, removing faces might mean breaking the structural integrity of
 * the figure in some way.
 *
 * It doesn't have bundles either, no two overlapping faces occupy the same plane. They don't have to. If they are
 * folded in a way that they should be tightly together, having one on top of the other separated by the minimum
 * possible distance is always more realistic.
 *
 * It doesn't even necessarily have proper polygons as faces, as faces of more than three vertices may not share the
 * same plane, being somewhat deformed.
 *
 * Admittedly this last point is a leakage from both the library used in the front end (babylon js, in which it's
 * trivial to draw deformed non planar polygons) and the stretching algorithm that may easily create deformed faces.
 *
 * Such a convenient coincidence shouldn't be overlooked. Besides, it's not like the actual faces of an origami figure
 * aren't always slightly deformed too.
 */
object FigureLoosener {
   fun loosen(figure: Figure): Figure = loosenChunks(figure)

   private fun loosenChunks(figure: Figure): Figure {
      val loosener = ChunkLoosener(figure)
      return loosener.loosenedFigure
   }
}
