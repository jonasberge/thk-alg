package de.thkoeln.inf.agelb.mst

import de.thkoeln.inf.agelb.adt.graph.Graph
import de.thkoeln.inf.agelb.adt.queue.IndexedPriorityQueue

/*
 * Implementation of stepwise Prim algorithm using an indexed priority queue
 * @param sourceGraph the graph used to determine the MST from
 * @param root root node of the MST
 */
class PrimStepwiseMST(
    private val sourceGraph: Graph,
    val root: Int = sourceGraph.vertices.random()
): StepwiseMST {
    // total weight of the mst
    override var weight: Double = 0.0
        private set

    // internal mst edges
    private var edges: HashSet<Graph.Edge> = hashSetOf()

    // array of graphs nodes + size
    private val vertices = sourceGraph.vertices.toIntArray()
    private val verticesSize = vertices.size

    // node's parent: default value null
    private val parent = hashMapOf<Int, Int?>()

    // distance from parent to node: default value +Infinity
    private val distTo = hashMapOf<Int, Double>()

    // internal priorityQueue that sorts nodes by min distance
    private val priorityQueue: IndexedPriorityQueue<Double> = IndexedPriorityQueue(verticesSize)

    // public representation of priorityQueue
    val queue: Set<Int> get() = priorityQueue.indices

    // public mst edges
    override fun edges() = edges.toSet()

    override var complete: Boolean = false
        private set

    // returns all steps of the algorithm in a sequence
    override fun steps() = sequence<MSTStep> {

        // insert root node to priority queue and set the distance to parent (itself) to 0.0
        priorityQueue.insert(root, 0.0)
        distTo[root] = 0.0
        parent[root] = root

        // work off priority queue
        while (priorityQueue.isNotEmpty()) {
            // extract node with minimum distance from priority queue
            val inspectedNode = priorityQueue.poll().first

            yield(
                Step(
                    type = StepType.NODE_INSPECT,
                    node = inspectedNode
                )
            )

            val parentNode = parent[inspectedNode]!!

            // if inspected node isn't root node add edge to MST
            if(parentNode != inspectedNode) {
                val edge = sourceGraph.getEdge(parentNode, inspectedNode)!!
                edges.add(edge)
                weight += edge.weight

                yield(
                    Step(
                        type = StepType.NODE_SELECT,
                        node = inspectedNode,
                        parentNode = parentNode
                    )
                )
            }

            // look for nodes neighbor's
            for (edge in sourceGraph.adjacentEdges(inspectedNode)) {
                val neighbor = edge.other(inspectedNode)
                val distance = distTo[neighbor] ?: Double.POSITIVE_INFINITY

                yield(
                    Step(
                        type = StepType.NEIGHBOR_INSPECT,
                        parentNode = parent[neighbor], // current parent of neighbor, null if not visited yet
                        node = neighbor
                    )
                )

                if(priorityQueue.contains(neighbor) && edge.weight < distance) {
                    // if neighbor is in queue but with a greater distance to its parent:
                    // update distance and parent and also decrease distance in pq
                    distTo[neighbor] = edge.weight
                    parent[neighbor] = inspectedNode
                    priorityQueue.decreaseKey(neighbor, edge.weight)
                } else if(parent[neighbor] == null) {
                    // if neighbor isn't visited yet: set distance and parent and add to queue
                    distTo[neighbor] = edge.weight
                    parent[neighbor] = inspectedNode
                    priorityQueue.insert(neighbor, edge.weight)
                }
            }
        }

        if(edges.size == verticesSize - 1)
            complete = true
    }

    enum class StepType { NODE_INSPECT, NEIGHBOR_INSPECT, NODE_SELECT }

    data class Step(
        val type: StepType,
        val node: Int? = null,
        val parentNode: Int? = null
    ): MSTStep

}