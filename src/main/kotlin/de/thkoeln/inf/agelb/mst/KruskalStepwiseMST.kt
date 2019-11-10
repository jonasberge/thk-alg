package de.thkoeln.inf.agelb.mst

import de.thkoeln.inf.agelb.adt.graph.Graph
import de.thkoeln.inf.agelb.adt.unionfind.UnionFind

class KruskalStepwiseMST(private val sourceGraph: Graph): StepwiseMST {
    override var weight = 0.0
        private set

    private val vertices = sourceGraph.vertices
    private val sortedEdges = sourceGraph.edges.sortedBy { it.weight }

    private val unionFind = UnionFind(vertices)
    private val edges = hashSetOf<Graph.Edge>()

    override var complete: Boolean = false
        private set

    override fun edges() = edges.toSet()

    override fun steps() = sequence<MSTStep> {
        // go through all edges sorted by minimum weight
        for (edge in sortedEdges) {
            yield(Step(StepType.EDGE_INSPECT, edge))

            // skip edges that would create a cycle
            if(unionFind.connected(edge.from, edge.to)) {
                yield(Step(StepType.EDGE_CYCLES, edge))
                continue
            }

            // unite vertices in unionFind
            unionFind.union(edge.from, edge.to)

            // add edges weight to MST total weight
            weight += edge.weight

            // add edge to new graph
            edges.add(edge)

            yield(Step(StepType.EDGE_SELECT, edge))

            // stop early if MST is already done
            if(unionFind.biggestSetSize == unionFind.size) break
        }

        // check if MST actually includes all nodes
        if(unionFind.biggestSetSize == unionFind.size)
            complete = true
    }

    enum class StepType { EDGE_CYCLES, EDGE_INSPECT, EDGE_SELECT }

    data class Step(
        val type: StepType,
        val edge: Graph.Edge
    ): MSTStep
}