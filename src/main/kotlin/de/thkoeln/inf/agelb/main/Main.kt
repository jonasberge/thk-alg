package de.thkoeln.inf.agelb.main

import de.thkoeln.inf.agelb.main.adt.graph.Edge
import de.thkoeln.inf.agelb.main.adt.graph.Graph
import de.thkoeln.inf.agelb.main.mst.KruskalMST
import de.thkoeln.inf.agelb.main.mst.LazyPrimMST
import de.thkoeln.inf.agelb.main.mst.PrimMST

fun main() {
    println("Hello TH-Köln!")

    val prim = KruskalMST(primGraph())

    prim.edges.forEach {
        println(it)
    }

    println(prim.weight)

    println(prim.complete)
}

fun primGraph() = Graph(6).run {
    addEdge(Edge(Pair(2, 4), 33.0))
    addEdge(Edge(Pair(3, 5), 2.0))
    addEdge(Edge(Pair(3, 4), 20.0))
    addEdge(Edge(Pair(4, 5), 1.0))
    addEdge(Edge(Pair(2, 3), 20.0))
    addEdge(Edge(Pair(1, 4), 10.0))
    addEdge(Edge(Pair(1, 3), 50.0))
    addEdge(Edge(Pair(0, 2), 20.0))
    addEdge(Edge(Pair(0, 1), 10.0))

    this
}