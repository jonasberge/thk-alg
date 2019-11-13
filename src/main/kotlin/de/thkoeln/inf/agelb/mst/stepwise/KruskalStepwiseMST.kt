package de.thkoeln.inf.agelb.mst.stepwise

import de.thkoeln.inf.agelb.adt.graph.Graph
import de.thkoeln.inf.agelb.adt.unionfind.EfficientUnionFind
import de.thkoeln.inf.agelb.mst.MSTStep
import de.thkoeln.inf.agelb.mst.StepwiseMST

class KruskalStepwiseMST(private val sourceGraph: Graph): StepwiseMST {
    override var weight = 0.0
        private set

    private val vertices = sourceGraph.vertices
    private val sortedEdges = sourceGraph.edges.sortedBy { it.weight }

    private val unionFind = EfficientUnionFind(vertices)
    private val edges = hashSetOf<Graph.Edge>()

    override var complete: Boolean = false
        private set

    override fun edges() = edges.toSet()

    override fun steps() = sequence<MSTStep> {
        // go through all edges sorted by minimum weight
        for (edge in sortedEdges) {
            yield(
                Step(
                    StepType.EDGE_INSPECT,
                    edge
                )
            )

            // skip edges that would create a cycle
            if(unionFind.connected(edge.from, edge.to)) {
                val set = unionFind[unionFind.find(edge.from)]
                val edgeSet = edges.filter { set.contains(it.from) || set.contains(it.to) }
                yield(
                    Step(
                        StepType.EDGE_CYCLES,
                        edge,
                        edgeSet
                    )
                )
                continue
            }

            // unite vertices in unionFind
            unionFind.union(edge.from, edge.to)

            // add edges weight to MST total weight
            weight += edge.weight

            // add edge to new graph
            edges.add(edge)

            yield(
                Step(
                    StepType.EDGE_SELECT,
                    edge
                )
            )

            // stop early if MST is already done
            if(unionFind.allInOneSet) break
        }

        // check if MST actually includes all nodes
        if(unionFind.allInOneSet)
            complete = true
    }

    enum class StepType { EDGE_CYCLES, EDGE_INSPECT, EDGE_SELECT }

    data class Step(
        val type: StepType,
        val edge: Graph.Edge,
        val edgeSet: Collection<Graph.Edge>? = null
    ): MSTStep
}