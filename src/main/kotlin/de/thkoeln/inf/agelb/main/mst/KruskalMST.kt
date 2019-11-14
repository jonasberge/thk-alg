package de.thkoeln.inf.agelb.main.mst

import de.thkoeln.inf.agelb.main.adt.graph.Edge
import de.thkoeln.inf.agelb.main.adt.graph.Graph
import de.thkoeln.inf.agelb.main.adt.queue.MinPriorityQueue
import de.thkoeln.inf.agelb.main.adt.queue.Queue
import de.thkoeln.inf.agelb.main.adt.unionfind.UnionFind

class KruskalMST(sourceGraph: Graph) : MST {
    private val queue = Queue<Edge>()

    override var weight: Double = 0.0
        private set

    override var complete: Boolean = false
        private set

    override var edges = queue
        private set

    private val verticesSize = sourceGraph.verticesSize

    init {
        val priorityQueue = MinPriorityQueue<Edge>()
        for (edge in sourceGraph.edges)
            priorityQueue.insert(edge)

        val unionFind = UnionFind(verticesSize)

        while (priorityQueue.isNotEmpty() && edges.size < verticesSize - 1) {
            val edge = priorityQueue.extractMin()
            val either = edge.either()
            val other = edge.other(either)

            if(!unionFind.connected(either, other)) {
                unionFind.union(either, other)
                queue.enqueue(edge)
                weight += edge.weight
            }
        }
    }
}