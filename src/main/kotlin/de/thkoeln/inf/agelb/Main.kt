package de.thkoeln.inf.agelb

import de.thkoeln.inf.agelb.graph.Graph

fun main()
{
    var graph = Graph(2, 2)

    val vertices = graph.vertices

    graph.addVertex(1)
    graph.addVertex(2)
    graph.addVertex(3)
    graph.removeVertex(1)
    graph.addVertex(4)

    val a = graph.addUndirectedEdge(2, 3, 2.3)
    val b = graph.addEdge(2, 4, 1.2)

    val edges = graph.edges

    graph.removeEdge(b)

    val edge = b
    val _isDirected = edge.isDirected
    val _from = edge.from
    val _to = edge.to

    val x = 1

    /*graph.addVertex(1, 2, 3, 4, 5, 6)

    graph.addEdge(1, 2, 6.7)
    graph.addEdge(1, 3, 5.2)
    graph.addEdge(1, 4, 2.8)
    graph.addEdge(1, 5, 5.6)
    graph.addEdge(1, 6, 3.6)
    graph.addEdge(2, 3, 5.7)
    graph.addEdge(2, 4, 7.3)
    graph.addEdge(2, 5, 5.1)
    graph.addEdge(2, 6, 3.2)
    graph.addEdge(3, 4, 3.4)
    graph.addEdge(3, 5, 8.5)
    graph.addEdge(3, 6, 4.0)
    graph.addEdge(4, 5, 8.0)
    graph.addEdge(4, 6, 4.4)
    graph.addEdge(5, 6, 4.6)*/

    // prim

    ;



}
