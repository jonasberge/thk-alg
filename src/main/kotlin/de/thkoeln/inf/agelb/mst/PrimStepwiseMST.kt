package de.thkoeln.inf.agelb.mst

import de.thkoeln.inf.agelb.adt.graph.Graph
import de.thkoeln.inf.agelb.adt.queue.IndexedPriorityQueue

class PrimStepwiseMST(private val sourceGraph: Graph): StepwiseMST {
    override var weight: Double = 0.0
        private set

    var edges: HashSet<Graph.Edge> = hashSetOf()

    private val vertices = sourceGraph.vertices.toIntArray()
    private val verticesSize = vertices.size

    private val parent = hashMapOf<Int, Int?>()
    private val distTo = hashMapOf<Int, Double>()

    private val priorityQueue: IndexedPriorityQueue<Double> = IndexedPriorityQueue(verticesSize)

    override fun edges() = edges.toSet()

    override fun steps() = sequence<MSTStep> {
        val root = vertices.random()
        yield(Step(StepType.SELECT_ROOT, root))

        priorityQueue.insert(root, 0.0)
        distTo[root] = 0.0
        yield(Step(StepType.INIT, root, null, null, null, priorityQueue.indices, parent.keys.toSet(), edges.toSet()))
        parent[root] = root

        while (priorityQueue.isNotEmpty()) {
            val node = priorityQueue.poll().first
            yield(Step(StepType.NEXT_NODE, root, node, null, null, priorityQueue.indices, parent.keys.toSet(), edges.toSet()))

            val parentNode = parent[node]!!
            if(parentNode != node) {
                val edge = sourceGraph.getEdge(parentNode, node)!!
                edges.add(edge)
                weight += edge.weight

                yield(Step(StepType.ADD_EDGE, root, parentNode, node, edge.weight, priorityQueue.indices, parent.keys.toSet(), edges.toSet()))
            }

            yield(Step(StepType.SCAN_NEIGHBORS, root, node, null, null, priorityQueue.indices, parent.keys.toSet(), edges.toSet()))

            for (edge in sourceGraph.adjacentEdges(node)) {
                val neighbor = edge.other(node)
                val distance = distTo[neighbor] ?: Double.POSITIVE_INFINITY

                yield(Step(StepType.NEXT_NEIGHBOR, root, node, neighbor, distance, priorityQueue.indices, parent.keys.toSet(), edges.toSet()))

                if(priorityQueue.contains(neighbor) && edge.weight < distance) {
                    distTo[neighbor] = edge.weight
                    parent[neighbor] = node
                    priorityQueue.decreaseKey(neighbor, edge.weight)
                    yield(Step(StepType.QUEUE_UPDATE, root, node, neighbor, distance, priorityQueue.indices, parent.keys.toSet(), edges.toSet()))
                } else if(parent[neighbor] == null) {
                    distTo[neighbor] = edge.weight
                    parent[neighbor] = node
                    priorityQueue.insert(neighbor, edge.weight)
                    yield(Step(StepType.QUEUE_INSERT, root, node, neighbor, distance, priorityQueue.indices, parent.keys.toSet(), edges.toSet()))
                }
            }

            if(edges.size == verticesSize - 1) {
                yield(Step(StepType.MST_COMPLETE, root, null, null, null, priorityQueue.indices, parent.keys.toSet(), edges.toSet()))
            } else {
                yield(Step(StepType.MST_INCOMPLETE, root, null, null, null, priorityQueue.indices, parent.keys.toSet(), edges.toSet()))
            }
        }
    }

    enum class StepType { SELECT_ROOT, INIT, NEXT_NODE, ADD_EDGE, SCAN_NEIGHBORS, NEXT_NEIGHBOR, QUEUE_INSERT, QUEUE_UPDATE, MST_COMPLETE, MST_INCOMPLETE }

    data class Step(
        val type: StepType,
        val root: Int,
        val primaryNode: Int? = null,
        val secondaryNode: Int? = null,
        val distance: Double? = null,
        val queue: Set<Int> = setOf(),
        val visitedNodes: Set<Int> = setOf(),
        val edges: Set<Graph.Edge> = setOf()
    ): MSTStep

}