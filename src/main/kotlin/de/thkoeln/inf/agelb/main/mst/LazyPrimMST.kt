package de.thkoeln.inf.agelb.main.mst

import de.thkoeln.inf.agelb.main.adt.graph.Edge
import de.thkoeln.inf.agelb.main.adt.graph.Graph
import de.thkoeln.inf.agelb.main.adt.queue.MinPriorityQueue
import de.thkoeln.inf.agelb.main.adt.queue.Queue
import kotlin.math.abs

/**
 * [LazyPrimMST] repräsentiert eine konkrete Strategy
 * für das Erzeugen eines MST durch die lazy Variante
 * des Algorithmus von Prim.
 *
 * Intern wird ein binary heap verwendet.
 *
 * Wenn der Graph nicht zusammenhängend ist wird ein
 * minimaler Wald erzeugt, welche eine Menge von
 * Minimalen Spannbäumen für jede Komponente des Graphen ist.
 *
 * @param sourceGraph der Ausgangsgraph
 * @property edges die für den MST ausgewählten Kanten
 * @property weight die Summe der Kantengewichte des MST
 * @property complete sagt aus ob MST vollständig ist
 */
class LazyPrimMST(sourceGraph: Graph) : MST {
    override var weight: Double = 0.0
        private set

    override var complete: Boolean = false
        private set

    override val edges = Queue<Edge>()

    // Anzahl an Knoten
    private val verticesSize = sourceGraph.verticesSize
    // BooleanArray zum Kennzeichnen der Knoten, die im MST vorhanden sind
    private val added = BooleanArray(verticesSize) { false }
    // priority queue
    private val priorityQueue = MinPriorityQueue<Edge>()

    init {
        for (vertex in 0 until verticesSize) {
            if(!added[vertex]) prim(sourceGraph, vertex)
        }

        if(edges.size == verticesSize - 1) complete = true
    }

    private fun prim(graph: Graph, start: Int) {
        scan(graph, start)

        while (priorityQueue.isNotEmpty()) {
            val edge = priorityQueue.extractMin()

            val either = edge.either()
            val other = edge.other(either)

            assert(added[either] || added[other])

            if(added[either] && added[other]) continue

            edges.enqueue(edge)
            weight += edge.weight

            if(!added[either]) scan(graph, either)
            if(!added[other]) scan(graph, other)
        }
    }

    private fun scan(graph: Graph, vertex: Int) {
        assert(!added[vertex])
        added[vertex] = true
        for (edge in graph.edgesOf(vertex))
            if(!added[edge.other(vertex)]) priorityQueue.insert(edge)
    }
}