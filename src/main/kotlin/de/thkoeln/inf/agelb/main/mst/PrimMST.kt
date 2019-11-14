package de.thkoeln.inf.agelb.main.mst

import de.thkoeln.inf.agelb.main.adt.graph.Edge
import de.thkoeln.inf.agelb.main.adt.graph.Graph
import de.thkoeln.inf.agelb.main.adt.queue.IndexMinPriorityQueue
import de.thkoeln.inf.agelb.main.adt.queue.Queue

class PrimMST(sourceGraph: Graph) : MST {
    override var weight: Double = 0.0
        private set

    override var complete: Boolean = false
        private set

    override var edges = Queue<Edge>()
        private set

    private val verticesSize = sourceGraph.verticesSize

    private val edgeTo = Array<Edge?>(verticesSize) { null }
    private val distTo = DoubleArray(verticesSize) { Double.POSITIVE_INFINITY }
    private val added = BooleanArray(verticesSize) { false }
    private val priorityQueue = IndexMinPriorityQueue<Double>(verticesSize)

    init {
        for (vertex in 0 until verticesSize) {
            if(!added[vertex]) prim(sourceGraph, vertex)
        }

        if(edges.size == verticesSize - 1)
            complete = true
    }

    fun prim(graph: Graph, start: Int) {
        distTo[start] = 0.0
        priorityQueue.insert(start, distTo[start])
        while (priorityQueue.isNotEmpty()) {
            val vertex = priorityQueue.extractMin()

            if(vertex != start) {
                val edge = edgeTo[vertex]!!
                edges.enqueue(edge)
                weight += edge.weight
            }

            scan(graph, vertex)
        }
    }

    fun scan(graph: Graph, vertex: Int) {
        added[vertex] = true
        for (edge in graph.edgesOf(vertex)) {
            val neighbor = edge.other(vertex)
            if(added[neighbor]) continue
            if(edge.weight < distTo[neighbor]) {
                distTo[neighbor] = edge.weight
                edgeTo[neighbor] = edge

                if(priorityQueue.contains(neighbor))
                    priorityQueue.decreaseKey(neighbor, distTo[neighbor])
                else
                    priorityQueue.insert(neighbor, distTo[neighbor])
            }
        }
    }
}
