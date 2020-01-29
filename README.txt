This project contains a complex algorithm that transforms an ideal, perfectly flat figure into a more realistic version
of it in 3D, where those polygons that have some degree of movement are moved, shifter or slightly rotated to a
different plane with the aim to achieving a realistic aspect of the figure in the GUI.

- Algorithm:

As we know, a figure is a collection of joined bundles. A bundle is joined to another through a segment, where several
edges articulate a pair of faces, one per bundle.

The problem of opening up a figure can be broken up in opening up bundles and "sewing" them back together.

The problem of opening up a bundle can be further broken up by dividing the bundle in what we call "chunks".

A chunk is, similarly to a bundle, a set of faces that are connected to the rest of the faces of the bundle through
edges articulating pairs of faces along one or more line segments.

The typical situation is that, when there are chunks, there is a part of the bundle connected to one or more chunks.
However it is technically possible for those chunks to be connected to further chunks and so on, so it is actually a
tree structure of chunks what a bundle is made of.


- Other approaches to opening up a figure:

  - Artificial Intelligence
It has to be done in a matter of less than a second by the backend, perhaps a few seconds, to be acceptable. It also
has to be reasonably accurate ie with little deformation.

If we require that the process is fast, then there are some methods, AI methods to be precise, that are probably out of
the question. Perhaps some AI methods would be workable by optimizing the code but it wouldn't make this a smart use of
my time for now, nor for the foreseeable future at the time of writing.

Algorithms like A* tree search could be valid options. For example some continuous variation of A*, since a branch in a
decision tree would be to fold along some segment but that would still leave open the question of the exact value of the angle.

So far I've been unable to find a suitable algorithm. I suspect it could be done with A* and small angles increments.

I know what is a valid, accurate unfolding and what is not (check distance between vertices).

But more interestingly, I also believe to have found a metric to maximize:

Note that maximizing or minimizing something is not immediate, since we want the figure slightly open, also not all
angles can be equal, so we cannot try to aim for the same value everywhere.

I believe one way to do it is by maximizing the "volume" of the figure and setting a maximum opening angle for any
segment. Or by using some relation between vertices movement and volume min(movement / volume)

I'm putting volume in quotation marks because it can be defined in many ways.

Here is a list of resources for search trees algorithms and similar:

- Artificial Intelligence A Modern Approach (3rd Edition)
- Artificial Intelligence For Games (2nd, 2009)

  - Other techniques
One of the easiest ways is, of course, deforming the figure by shifting the points along the depth of the bundle.

We can get away with this for small angles.

It may seem straightforward but it is not. The algorithm to stretch a bundle and make it look realistic is a somewhat
complex one.

 - Other possible algorithms to "open up" or loosen a flat figure:
Forward dynamics of closed-loop systems
Featherstone's "Rigid Body Dynamics" covers this topic in chapter 8

I'm not sure these would work to be honest. Just came up on a cursory search.