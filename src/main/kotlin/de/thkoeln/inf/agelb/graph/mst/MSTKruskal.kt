package de.thkoeln.inf.agelb.graph.mst

import de.thkoeln.inf.agelb.graph.Graph

class MSTKruskal(sourceGraph: Graph): MSTStrategy(sourceGraph) {
    override fun mst(): Graph {
        // TODO: implement algorithm
        return sourceGraph
    }
}