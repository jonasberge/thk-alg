package de.thkoeln.inf.agelb.main.adt.graph

/**
 * [Graph] repräsentiert einen ungerichteten Graphen mit Kantengewichtung.
 *
 * Die Knoten erhalten Indexes von 0 bis [verticesSize] - 1.
 * Die ungerichteten, gewichteten Kanten werden von der Klasse [Edge] repräsentiert.
 *
 * Intern wird eine Adjäzenzliste genutzt.
 * Für jeden Knotenindex ist ein [Hashset] reserviert, das mit [Edge]objekten erweitert wird.
 * Da der Graph ungerichtet ist wird das [Edge]objekt beim Hinzufügen in beide [HashSet]s hinzugefügt.
 * Der Iterator [edges] liefert jedoch nur ein [Edge]objekt pro Verbindung.
 *
 * Alle Operationen erfolgen (im schlimmsten Fall) in konstanter Laufzeit.
 * Davon ausgenommen ist jedoch das iterieren der Kanten mit [edges].
 *
 * @property verticesSize die Anzahl an Knoten
 * @property vertices die Indexes der Knoten als [IntRange]
 * @property edgesSize die Anzahl an Kanten
 * @property edges die [Kanten][Edge] als [Iterable]
 * @property adjacencyList Adjäzenzliste in Form eines Arrays aus HashSet<Edge>
 */

class Graph(val verticesSize: Int) {
    private val adjacencyList = Array(verticesSize) { HashSet<Edge>() }
    val vertices: IntRange = 0..verticesSize
    var edgesSize: Int = 0

    /**
     * Liefert die [Kanten][Edge] als [Sequence] und ignoriert doppelte [Edge]objekte.
     */
    val edgeSequence get() = sequence {
        for (i in 0 until verticesSize) {
            var selfLoops = 0
            for (edge in adjacencyList[i]) {
                if(edge.other(i) > i) {
                    yield(edge)
                } else if(edge.other(i) == i) {
                    if(selfLoops % 2 == 0) yield(edge)
                    selfLoops++
                }
            }
        }
    }

    /**
     * Liefert die [Kanten][Edge] als [Iterable] und ignoriert doppelte [Edge]objekte.
     */
    val edges: Iterable<Edge> get() = edgeSequence.toList()

    /**
     * Überprüft ob der Knotenindex valide ist.
     * @throws IllegalArgumentException wenn der Knotenindex out of bounds ist
     */
    private fun validateVertex(index: Int) {
        require(index in 0 until verticesSize)
            { "vertex $index is not between 0 and ${verticesSize - 1}" }
    }

    /**
     * Fügt diesem kantengewichteten Graphen eine ungerichtete Kante [edge] hinzu.
     *
     * @param edge die Kante die hinzugefügt werden soll
     */
    fun addEdge(edge: Edge) {
        val either = edge.either()
        val other = edge.other(either)
        validateVertex(either)
        validateVertex(other)
        adjacencyList[either].add(edge)
        adjacencyList[other].add(edge)
        edgesSize++
    }

    /**
     * Liefert alle mit dem Knoten [vertexIndex] verbundenen Kanten.
     *
     * @param vertexIndex der Index des Knoten
     *
     * @return die mit gegebenen Knoten verbundene Kanten
     */
    fun edgesOf(vertexIndex: Int): Iterable<Edge> {
        validateVertex(vertexIndex)
        return adjacencyList[vertexIndex]
    }

    /**
     * Liefert alle Nachbarn vom Knoten [vertexIndex].
     *
     * @param vertexIndex der Index des Knoten
     *
     * @return die Nachbarn des gegebenen Knoten
     */
    fun neighborsOf(vertexIndex: Int) = edgesOf(vertexIndex).map { it.other(vertexIndex) }

    /**
     * Liefert den Grad des Knoten [vertexIndex].
     *
     * @param vertexIndex der Index des Knoten
     * @return den Grad des gegebenen Knoten
     */
    fun degree(vertexIndex: Int): Int {
        validateVertex(vertexIndex)
        return adjacencyList[vertexIndex].size
    }

    override fun toString(): String {
        return "Graph(verticesSize=$verticesSize, edgesSize=$edgesSize, edges=${edges.map { it }.toList()})"
    }

    /**
     * Gibt einen neuen, durch Deepcopy erzeugten Graphen zurück.
     * @return ein neues durch Deepcopy erzeugtes Objekt
     */
    fun copy() = Graph(verticesSize).let { graph ->
        edges.forEach { graph.addEdge(it) }
        graph
    }
}