package de.thkoeln.inf.agelb.main.mst

import de.thkoeln.inf.agelb.main.adt.graph.Edge

/**
 * [MST] repräsentiert eine abstrakte Strategie
 * für das Erzeugen minimaler Spannbäume.
 *
 * @property edges die für den MST ausgewählten Kanten
 * @property weight die Summe der Kantengewichte des MST
 * @property complete sagt aus ob MST vollständig ist
 */
interface MST {
    val edges: Collection<Edge>
    val weight: Double
    val complete: Boolean // false bei nicht zusammenhängenden Graphen
}