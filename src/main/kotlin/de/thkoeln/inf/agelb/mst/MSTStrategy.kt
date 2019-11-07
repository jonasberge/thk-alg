package de.thkoeln.inf.agelb.mst

import de.thkoeln.inf.agelb.adt.graph.Graph

/*
 *  Abstract Strategy for MST Algorithm that solves in steps
 */
interface StepwiseMST {
    /*
     * Returns edges of MST (after having determined MST with steps())
     */
    fun edges(): Iterable<Graph.Edge>

    /*
     * Returns weight of MST (after having determined MST with steps())
     */
    val weight: Double

    /*
     * Returns status of MST
     * If graph isn't coherent the value will remain false after having run steps()
     */
    val complete: Boolean

    /*
     * Returns all steps of the algorithm
     */
    fun steps(): Sequence<MSTStep>
}

interface MSTStep