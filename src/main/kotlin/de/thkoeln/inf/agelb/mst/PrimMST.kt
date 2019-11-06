package de.thkoeln.inf.agelb.mst

import de.thkoeln.inf.agelb.adt.graph.Graph
import de.thkoeln.inf.agelb.adt.queue.IndexedPriorityQueue

class PrimMST(private val sourceGraph: Graph): MST {
    override var weight: Double = 0.0
        private set

    var edges: HashSet<Graph.Edge> = hashSetOf()

    private val vertices = sourceGraph.vertices.toIntArray()
    private val verticesSize = vertices.size

    private val parent = hashMapOf<Int, Int?>()
    private val distTo = hashMapOf<Int, Double>()

    private val priorityQueue: IndexedPriorityQueue<Double> = IndexedPriorityQueue(verticesSize)

    override fun edges() = edges.toSet()

    override fun solve() {
        val root = vertices.random()
        println("Add random node to queue: $root")

        priorityQueue.insert(root, 0.0)
        distTo[root] = 0.0
        parent[root] = root

        while (priorityQueue.isNotEmpty()) {
            val node = priorityQueue.poll().first
            println("Next node: $node")

            val parentNode = parent[node]!!
            if(parentNode != node) {
                println("Add edge $parentNode -> $node")
                edges.add(sourceGraph.getEdge(parentNode, node)!!)
                weight += distTo[node]!!
            }


            println("Check Neighbors")

            for (edge in sourceGraph.adjacentEdges(node)) {
                val neighbor = edge.other(node)
                val distance = distTo[neighbor] ?: Double.POSITIVE_INFINITY
                println("Next neighbor: $neighbor (distance: ${edge.weight})")

                if(priorityQueue.contains(neighbor) && edge.weight < distance) {
                    println("In queue, but changing parent and distance ${edge.weight}")
                    distTo[neighbor] = edge.weight
                    parent[neighbor] = node
                    priorityQueue.decreaseKey(neighbor, edge.weight)
                } else if(parent[neighbor] == null) {
                    println("Not visited yet, add to queue ${edge.weight}")
                    distTo[neighbor] = edge.weight
                    parent[neighbor] = node
                    priorityQueue.insert(neighbor, edge.weight)
                }
            }
        }
    }
}