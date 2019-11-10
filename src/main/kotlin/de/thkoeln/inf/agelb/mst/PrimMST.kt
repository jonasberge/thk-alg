package de.thkoeln.inf.agelb.mst

import de.thkoeln.inf.agelb.adt.graph.Graph
import de.thkoeln.inf.agelb.adt.queue.IndexedPriorityQueue

class PrimMST(
    private val sourceGraph: Graph,
    val root: Int = sourceGraph.vertices.random()
): MST {
    // total weight of the mst
    override var weight: Double = 0.0
        private set

    // internal mst edges
    private var edges: HashSet<Graph.Edge> = hashSetOf()

    // array of graphs nodes + size
    private val vertices = sourceGraph.vertices.toIntArray()
    private val verticesSize = vertices.size

    private val vertexMapping = mutableMapOf<Int, Int>()

    // node's parent: default value null
    private val parent = hashMapOf<Int, Int?>()

    // distance from parent to node: default value +Infinity
    private val distTo = hashMapOf<Int, Double>()

    // internal priorityQueue that sorts nodes by min distance
    private val priorityQueue: IndexedPriorityQueue<Double> = IndexedPriorityQueue(verticesSize)

    // public representation of priorityQueue
    val queue: Set<Int> get() = priorityQueue.indices.map { value(it) }.toSet()

    val queueEdges: List<Pair<Int, Int>>
        get() {
            queue.map { node ->
                parent[node]
            }
            return listOf()
        }

    // queue representation as edges
    val edgeQueue: List<Graph.Edge> get() = priorityQueue.indices.mapNotNull {
        value(it).let { node ->
            sourceGraph.getEdge(parent[node]!!, node)
        }
    }

    // public mst edges
    override fun edges() = edges.toSet()

    override var complete: Boolean = false
        private set

    init {
        vertices.forEachIndexed { index, i -> vertexMapping[i] = index }
    }

    private fun index(id: Int) = vertexMapping[id]!!
    private fun value(index: Int) = vertices[index]

    override fun solve() {
        // insert root node to priority queue and set the distance to parent (itself) to 0.0
        priorityQueue.insert(index(root), 0.0)
        distTo[root] = 0.0
        parent[root] = root

        // work off priority queue
        while (priorityQueue.isNotEmpty()) {
            // extract node with minimum distance from priority queue
            val inspectedNode = value(priorityQueue.poll().first)

            val parentNode = parent[inspectedNode]!!

            // if inspected node isn't root node add edge to MST
            if(parentNode != inspectedNode) {
                val edge = sourceGraph.getEdge(parentNode, inspectedNode)!!
                edges.add(edge)
                weight += edge.weight
            }

            // look for nodes neighbor's
            for (edge in sourceGraph.adjacentEdges(inspectedNode)) {
                val neighbor = edge.other(inspectedNode)
                val neighborIndex = index(neighbor)
                val distance = distTo[neighbor] ?: Double.POSITIVE_INFINITY

                if(priorityQueue.contains(neighborIndex) && edge.weight < distance) {
                    // if neighbor is in queue but with a greater distance to its parent:
                    // update distance and parent and also decrease distance in pq
                    distTo[neighbor] = edge.weight
                    parent[neighbor] = inspectedNode
                    priorityQueue.decreaseKey(neighborIndex, edge.weight)
                } else if(parent[neighbor] == null) {
                    // if neighbor isn't visited yet: set distance and parent and add to queue
                    distTo[neighbor] = edge.weight
                    parent[neighbor] = inspectedNode
                    priorityQueue.insert(neighborIndex, edge.weight)
                }
            }
        }

        if(edges.size == verticesSize - 1)
            complete = true
    }
}