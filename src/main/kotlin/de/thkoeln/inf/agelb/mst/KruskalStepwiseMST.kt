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

    override fun edges() = edges.toSet()

    override fun steps() = sequence<MSTStep> {
        yield(Step(StepType.INIT))
        for (edge in sortedEdges) {
            yield(Step(StepType.NEXT_EDGE, edge, weight, edges(), unionFind.sets))

            // skip edges that would create a cycle
            if(unionFind.connected(edge.from, edge.to)) {
                yield(Step(StepType.SKIP_EDGE, edge, weight, edges(), unionFind.sets))
                continue
            }

            // unite vertices in unionFind
            unionFind.union(edge.from, edge.to)

            weight += edge.weight

            // add edge to new graph
            edges.add(edge)

            yield(Step(StepType.UNION_MERGE, edge, weight, edges(), unionFind.sets))

            // stop early if mst is already done
            if(unionFind.biggestSetSize == unionFind.size) break
        }

        // check if mst includes all nodes
        if(unionFind.biggestSetSize != unionFind.size) {
            yield(Step(StepType.MST_INCOMPLETE, null, weight, edges(), unionFind.sets))
        } else {
            yield(Step(StepType.MST_COMPLETE, null, weight, edges(), unionFind.sets))
        }
    }

    enum class StepType { INIT, NEXT_EDGE, SKIP_EDGE, UNION_MERGE, MST_COMPLETE, MST_INCOMPLETE }

    data class Step(
        val type: StepType,
        val edge: Graph.Edge? = null,
        val weight: Double = 0.0,
        val edges: Set<Graph.Edge> = setOf(),
        val unionSets: Map<Int, Set<Int>> = mapOf()
    ): MSTStep
}