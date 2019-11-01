package de.thkoeln.inf.agelb

import de.thkoeln.inf.agelb.graph.Graph
import de.thkoeln.inf.agelb.graph.mst.MSTKruskal
import de.thkoeln.inf.agelb.graph.mst.MSTPrim

fun main()
{
    val graph = Graph(2, 2)

    graph.addUndirectedEdge(0, 4, 15.0)
    graph.addUndirectedEdge(4, 1, 14.0)
    graph.addUndirectedEdge(1, 5, 11.0)
    graph.addUndirectedEdge(5, 2, 7.0)
    graph.addUndirectedEdge(2, 6, 8.0)
    graph.addUndirectedEdge(6, 3, 17.0)
    graph.addUndirectedEdge(3, 7, 16.0)
    graph.addUndirectedEdge(7, 0, 18.0)
    graph.addUndirectedEdge(0, 1, 5.0)
    graph.addUndirectedEdge(1, 2, 9.0)
    graph.addUndirectedEdge(2, 3, 12.0)
    graph.addUndirectedEdge(3, 0, 6.0)
    graph.addUndirectedEdge(4, 5, 3.0)
    graph.addUndirectedEdge(5, 6, 2.0)
    graph.addUndirectedEdge(6, 7, 4.0)
    graph.addUndirectedEdge(7, 4, 1.0)
    graph.addUndirectedEdge(1, 6, 10.0)
    graph.addUndirectedEdge(1, 7, 13.0)

    MSTKruskal(graph).mst()?.edges?.forEach {
        println("Vertices: ${it.vertices}, Weight: ${it.weight}")
    }

    println("Prim")

    MSTPrim(graph).mst()?.edges?.forEach {
        println("Vertices: ${it.vertices}, Weight: ${it.weight}")
    }
}
