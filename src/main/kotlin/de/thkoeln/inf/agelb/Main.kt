package de.thkoeln.inf.agelb

import de.thkoeln.inf.agelb.ui.GraphApplet
import processing.core.PApplet
import processing.core.PConstants

fun main()
{
    val pa = PApplet()

    GraphApplet.run(
        GraphApplet.Config(
            width = 1920 / 2,
            height = 1080 / 2,
            isResizable = true,
            backgroundColor = pa.color(240),
            scrollDragTolerance = 8f,
            node = GraphApplet.Config.Node(
                radius = 25.0f,
                padding = 35.0f,
                paddingColor = pa.color(0, 3),
                fillColor = 200,
                selectedFillColor = pa.color(240, 150, 100),
                highlightedFillColor = pa.color(220, 240, 230),
                strokeColor = pa.color(0),
                strokeWeight = 2.0f
            )
        )
    )


    /*
    val graph = Graph()

    graph.connect(1, 2, 6.7)

    graph.addEdge(1, 2, 5.5)

    graph.removeVertex(2)

    graph.removeEdge(1, 2)

    graph.connect(1, 3, 5.2)
    graph.connect(1, 4, 2.8)
    graph.connect(1, 5, 5.6)
    graph.connect(1, 6, 3.6)
    graph.connect(2, 3, 5.7)
    graph.connect(2, 4, 7.3)
    graph.connect(2, 5, 5.1)
    graph.connect(2, 6, 3.2)
    graph.connect(3, 4, 3.4)
    graph.connect(3, 5, 8.5)
    graph.connect(3, 6, 4.0)
    graph.connect(4, 5, 8.0)
    graph.connect(4, 6, 4.4)
    graph.connect(5, 6, 4.6)

    val prim = MSTPrim(graph)
    val result = prim.mst()

    val x = 1
    */

    /*
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

    println("__Kruskal__")

    KruskalStepwiseMST(graph).steps().forEach {
        println(it)
    }

    println("\n__Prim__")

    PrimStepwiseMST(graph).steps().forEach {
        println(it)
    }
    */
}
