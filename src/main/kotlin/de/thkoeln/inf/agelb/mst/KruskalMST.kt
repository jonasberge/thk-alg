package de.thkoeln.inf.agelb.mst

import de.thkoeln.inf.agelb.adt.graph.Graph
import de.thkoeln.inf.agelb.adt.unionfind.UnionFind

class KruskalMST(private val sourceGraph: Graph): MST {
    override var weight = 0.0
        private set

    private val vertices = sourceGraph.vertices
    private val sortedEdges = sourceGraph.edges.sortedBy { it.weight }

    private val unionFind = UnionFind(vertices)
    private val edges = hashSetOf<Graph.Edge>()

    override fun edges() = edges.toSet()

    override fun solve() {
        for (edge in sortedEdges) {
            // skip edges that would create a cycle
            if(unionFind.connected(edge.from, edge.to)) continue

            // unite vertices in unionFind
            unionFind.union(edge.from, edge.to)

            weight += edge.weight

            // add edge to new graph
            edges.add(edge)

            // stop early if mst is already done
            if(unionFind.biggestSetSize == unionFind.size) break
        }
    }
}