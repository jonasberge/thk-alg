package de.thkoeln.inf.agelb.graph.mst

import de.thkoeln.inf.agelb.graph.Graph
import java.util.*

class MSTPrim(sourceGraph: Graph): MSTStrategy(sourceGraph) {
    private val visited = hashSetOf<Int>()
    private val priorityQueue = PriorityQueue<Graph.Edge>()

    private fun visited(vertex: Int) = visited.contains(vertex)

    private fun addEdges(vertex: Int) {
        visited.add(vertex)
        val edges = sourceGraph.edgesFrom(vertex)

        for (edge in edges) {
            if(!visited(edge.to))
                priorityQueue.add(edge)
        }

    }

    override fun mst(): Graph? {
        val vertices = sourceGraph.vertices
        val expectedEdges = vertices.size - 1

        var edgeCount = 0
        var mstCost = 0.0

        val root = vertices.random()
        val mstGraph = Graph(expectedEdges, 1)

        addEdges(root)

        while (priorityQueue.isNotEmpty() && edgeCount != expectedEdges) {
            val edge = priorityQueue.poll()
            val nodeIndex = edge.to

            if(visited(nodeIndex)) continue

            mstGraph.addUndirectedEdge(edge.from, edge.to, edge.weight)
            addEdges(nodeIndex)

            mstCost += edge.weight
            edgeCount++
        }

        if(edgeCount != expectedEdges) return null

        return mstGraph
    }
}