package de.thkoeln.inf.agelb.graph.mst

import de.thkoeln.inf.agelb.graph.Graph

class MSTKruskal(sourceGraph: Graph): MSTStrategy(sourceGraph) {

    internal class UnionFind(elements: Set<Int>) {
        private val parentMap = hashMapOf<Int, Int>()
        private val sizeMap = hashMapOf<Int, Int>()
        private var count = 0

        val numberOfSets get() = count
        val size get() = parentMap.size
        val biggestSetSize get() = sizeMap.maxBy { it.value }?.key?.let { size(it) }

        init {
            for (element in elements) {
                parentMap[element] = element
                sizeMap[element] = 1
            }
            count = elements.size
        }

        fun addElement(element: Int) {
            if(!parentMap.containsKey(element)) {
                parentMap[element] = element
                sizeMap[element] = 1
                count++
            }
        }

        fun connected(element1: Int, element2: Int) = find(element1) == find(element2)
        fun size(element: Int) = sizeMap[find(element)]

        fun find(element: Int): Int {
            require(parentMap.containsKey(element)) { "Element not found" }

            var current = element

            // find root
            while (current != parentMap[current]) current = parentMap[current]!!
            val root = current

            // path compression
            current = element
            while (current != root) {
                val parent = parentMap[current]!!
                parentMap[current] = root
                current = parent
            }

            return root
        }

        fun union(element1: Int, element2: Int) {
            val parent1 = find(element1)
            val parent2 = find(element2)

            if(parent1 == parent2) return

            val size1 = sizeMap[parent1]!!
            val size2 = sizeMap[parent2]!!

            if(size1 < size2) {
                sizeMap[parent2] = size2 + size1
                parentMap[parent1] = parent2
            } else {
                sizeMap[parent1] = size1 + size2
                parentMap[parent2] = parent1
            }

            count--
        }
    }

    override fun mst(): Graph? {
        val sortedEdges = sourceGraph.edges.sortedBy { it.weight }
        val unionFind = UnionFind(sourceGraph.vertices)
        val newGraph = Graph()

        for (edge in sortedEdges) {
            // skip cycle building edges
            if(unionFind.connected(edge.from, edge.to)) continue

            unionFind.union(edge.from, edge.to)
            newGraph.addUndirectedEdge(edge.from, edge.to, edge.weight)

            // stop early if mst is already done
            if(unionFind.biggestSetSize == unionFind.size) break
        }

        // check if mst includes all nodes
        if(unionFind.biggestSetSize != unionFind.size) return null

        return newGraph
    }
}