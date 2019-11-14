package de.thkoeln.inf.agelb.main.adt.graph

/**
 * [Edge] repräsentiert eine gewichtete Kante.
 *
 * @property endpoints die zwei zu verbindenen Knoten als [Pair]
 * @property weight das Gewicht der Kante
 * @throws IllegalArgumentException wenn der Index von einem der beiden Knoten negativ ist
 */

class Edge(
    val endpoints: Pair<Int, Int>,
    val weight: Double
): Comparable<Edge> {

    init {
        require(endpoints.first >= 0 && endpoints.second >= 0)
            { "Vertex index must be a non-negative integer" }
    }

    /**
     * Gibt den Index [Int] eines beliebigen Endpunkts zurück.
     * @return den Index eines beliebigen Endpunkts
     */
    fun either(): Int = endpoints.first

    /**
     * Gibt den Index [Int] des anderen Endpunkts zurück.
     * @param other den Index eines beliebigen Endpunkts
     * @return den Index des anderen Endpunkts
     */
    fun other(other: Int) = when (other) {
        endpoints.first -> endpoints.second
        endpoints.second -> endpoints.first
        else -> throw IllegalArgumentException("Illegal endpoint")
    }

    /**
     * Vergleicht das Gewicht zweier Kanten.
     * @param other die zu vergleichende Kante
     */
    override fun compareTo(other: Edge): Int = weight.compareTo(other.weight)

    /**
     * Gibt diese Kante [Edge] als [String] repräsentiert zurück.
     *
     * @return diese Kante als [String] repräsentiert
     */
    override fun toString(): String ="Edge(endpoints=$endpoints, weight=$weight)"
}