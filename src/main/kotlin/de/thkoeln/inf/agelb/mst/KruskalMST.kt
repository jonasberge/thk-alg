package de.thkoeln.inf.agelb.mst

import de.thkoeln.inf.agelb.adt.graph.Graph
import de.thkoeln.inf.agelb.adt.unionfind.EfficientUnionFind

class KruskalMST(private val sourceGraph: Graph): MST {
    override var weight = 0.0
        private set

    private val vertices = sourceGraph.vertices
    private val sortedEdges = sourceGraph.edges.sortedBy { it.weight }

    private val unionFind = EfficientUnionFind(vertices)
    private val edges = hashSetOf<Graph.Edge>()

    override var complete: Boolean = false
        private set

    override fun edges() = edges.toSet()

    override fun solve() {
        // go through all edges sorted by minimum weight
        for (edge in sortedEdges) {
            // skip edges that would create a cycle
            if(unionFind.connected(edge.from, edge.to)) continue

            // unite vertices in unionFind
            unionFind.union(edge.from, edge.to)

            // add edges weight to MST total weight
            weight += edge.weight

            // add edge to new graph
            edges.add(edge)

            // stop early if MST is already done
            if(unionFind.allInOneSet) break
        }

        // check if MST actually includes all nodes
        if(unionFind.allInOneSet)
            complete = true
    }
}