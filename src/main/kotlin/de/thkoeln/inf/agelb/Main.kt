package de.thkoeln.inf.agelb

import de.thkoeln.inf.agelb.graph.Graph
import de.thkoeln.inf.agelb.graph.mst.MSTKruskal

fun main()
{
    val graph = Graph(2, 2)

    graph.addUndirectedEdge(1, 2, 2.3)
    graph.addUndirectedEdge(2, 3, 1.2)
    graph.addUndirectedEdge(3, 4, 5.2)
    graph.addUndirectedEdge(4, 2, 5.2)
    graph.addUndirectedEdge(4, 1, 6.2)
    graph.addUndirectedEdge(5, 1, 7.2)
    graph.addUndirectedEdge(5, 2, 7.2)
    graph.addUndirectedEdge(5, 3, 10.2)
    graph.addUndirectedEdge(5, 4, 1.2)

    MSTKruskal(graph).mst()?.edges?.forEach {
        println("Vertices: ${it.vertices}, Weight: ${it.weight}")
    }
}
