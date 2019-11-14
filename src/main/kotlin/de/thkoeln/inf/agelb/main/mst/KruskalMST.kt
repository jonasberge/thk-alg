package de.thkoeln.inf.agelb.main.mst

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
 * @param sourceGraph der Ausgangsgraph
 * @property edges die für den MST ausgewählten Kanten
 * @property weight die Summe der Kantengewichte des MST
 * @property complete sagt aus ob MST vollständig ist
 */
class KruskalMST(sourceGraph: Graph) : MST {
    private val queue = Queue<Edge>()

    override var weight: Double = 0.0
        private set

    override var complete: Boolean = false
        private set

    override var edges = queue
        private set

    private val verticesSize = sourceGraph.verticesSize

    init {
        val priorityQueue = MinPriorityQueue<Edge>()

        // Fülle Priority Queue mit allen Kanten
        for (edge in sourceGraph.edges)
            priorityQueue.insert(edge)

        // Erzeuge Union Find zur Ermittlung von Zyklen
        val unionFind = UnionFind(verticesSize)

        // Solange PQ Elemente hat und MST noch nicht komplett ist
        while (priorityQueue.isNotEmpty() && edges.size < verticesSize - 1) {

            // Nimm die Kante mit dem kleinsten Gewicht aus der PQ
            val edge = priorityQueue.extractMin()

            // Erzeuge Variablen für beide Enpunkte/Knoten der Kante
            val either = edge.either()
            val other = edge.other(either)

            // Wenn Kante keinen Zyklus verursacht
            if(!unionFind.connected(either, other)) {
                // Vereine die Komponenten beider Knoten im Union Find
                unionFind.union(either, other)

                queue.enqueue(edge)     // Füge die Kante zu MST hinzu
                weight += edge.weight   // Addiere Gewicht zur Gewichtsumme des MST
            }
        }

        if(edges.size == verticesSize - 1)
            complete = true
    }
}