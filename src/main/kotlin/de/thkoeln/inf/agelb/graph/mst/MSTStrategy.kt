package de.thkoeln.inf.agelb.graph.mst

import de.thkoeln.inf.agelb.graph.Graph

abstract class MSTStrategy(val sourceGraph: Graph) {
    abstract fun mst(): Graph?
}