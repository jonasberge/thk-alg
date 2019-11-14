package de.thkoeln.inf.agelb.livecoding

import de.thkoeln.inf.agelb.main.mst.MST
import de.thkoeln.inf.agelb.main.adt.graph.Edge
import de.thkoeln.inf.agelb.main.adt.graph.Graph
import de.thkoeln.inf.agelb.main.adt.queue.MinPriorityQueue
import de.thkoeln.inf.agelb.main.adt.queue.Queue
import de.thkoeln.inf.agelb.main.adt.unionfind.UnionFind


/**
 * [KruskalMST] repräsentiert eine konkrete Strategie
 * für das Erzeugen eines MST mit dem Algorithmus von Kruskal.
 *
 * Der Algorithmus von Kruskal arbeitet kantenbasiert.
 * Die Kanten des Graphen werden aufsteigend nach dem Gewicht behandelt.
 * Bei jedem Schritt werden die Kanten hinzugefügt, die keinen Zyklus verursachen.
 * Bis der minimale Spannbaum komplett ist.
 *
 * Um die Kanten in der gewünschten Reihenfolge zu durchlaufen
 * werden sie zu einer [MinPriorityQueue] hinzugefügt.
 *
 * Zyklen lassen sich mit einer [UnionFind] Datenstruktur ermitteln.
 * [UnionFind] erzeugt beim Initialisieren für jeden Knoten eine eigene Komponente.
 * Bei jedem Hinzufügen einer Kante werden die Komponenten beider Knoten vereint.
 * Um zu überprüfen ob eine Kante einen Zyklus verursacht
 * wird geschaut ob die beiden Knoten schon zur selben Komponente gehören.
 *
 * Die Kanten des MST werden in einer [Queue] (einfach verkettete Liste) gesammelt.
 *
 * @param sourceGraph der Ausgangsgraph
 * @property edges die für den MST ausgewählten Kanten
 * @property weight die Summe der Kantengewichte des MST
 * @property complete sagt aus ob MST vollständig ist
 */
class KruskalMST(sourceGraph: Graph) : MST {
    // hier kommen alle Kanten des MST rein
    private val queue = Queue<Edge>()

    // hier das Gesammtgewicht des MST
    override var weight: Double = 0.0
        private set

    // hier wird gespeichert ob der MST vollständig ist
    override var complete: Boolean = false
        private set

    // schnittstelle um von Außen auf Kanten zuzugreifen
    override var edges = queue
        private set

    // Anzahl der Knoten
    private val verticesSize = sourceGraph.verticesSize

    init {
        val priorityQueue = MinPriorityQueue<Edge>()
        val unionFind = UnionFind(verticesSize)

        // TODO: Implementiere den Algorithmus von Kruskal
        // Schau dir zu Hilfe unsere Wiki und Dokumentation an
    }
}