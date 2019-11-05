package de.thkoeln.inf.agelb.mst

import de.thkoeln.inf.agelb.adt.graph.Graph

interface MST {
    fun edges(): Iterable<Graph.Edge>
    val weight: Double
    fun solve()
}

interface StepwiseMST {
    fun edges(): Iterable<Graph.Edge>
    val weight: Double
    fun steps(): Sequence<MSTStep>
}

interface MSTStep