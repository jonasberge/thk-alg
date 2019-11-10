package de.thkoeln.inf.agelb.adt.unionfind

/*
    Union Find implementation with path compression
 */
class EfficientUnionFind(elements: Set<Int>): UnionFind(elements) {
    override fun find(element: Int): Int {
        val root = super.find(element)
        compressPath(element, root)
        return root
    }

    private fun compressPath(element: Int, root: Int) {
        val parent = parentMap[element] ?: return
        if(element != root) {
            parentMap[element] = root
            compressPath(parent, root)
        }
    }
}