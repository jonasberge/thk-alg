package de.thkoeln.inf.agelb.graph

import java.lang.Exception
import java.util.Stack

private const val DEFAULT_WEIGHT = 0.0

// TODO: use pairs next to first/second and from/to parameters.
// NOTE: this is just for syntactic sugar, e.g.: graph.connect(1 to 2)

/**
 * Represents a (mixed) graph (abstract data type).
 * @param vertexCapacity the amount of vertices the graph should be able to hold.
 * @param incrementSteps the reallocation size of the internal adjacency matrix.
 */
class Graph(vertexCapacity: Int = 0, private val incrementSteps: Int = 1)
{
    /**
     * Represents an edge of a graph. An instance of this class always
     * belongs to the the graph it was created in throughout its life time.
     */
    inner class Edge(var weight: Double = DEFAULT_WEIGHT): Comparable<Edge>
    {
        val vertices : Pair<Int, Int>
            get() = volatile { edgeMapping[this]!! }

        val from: Int get() = vertices.first
        val to: Int get() = vertices.second

        val isDirected: Boolean
            get() {
                val u = volatile { vertexIndex(from)!! }
                val v = volatile { vertexIndex(to)!! }
                return connection[u][v] != connection[v][u]
            }

        /**
         * Should be used for the execution of code that might cause an
         * exception after the current edge has been removed from the graph.
         * @param f lambda containing the volatile code
         */
        private fun <T> volatile(f: () -> T) =
            try { f() }
            catch (e: Exception) {
                throw InvalidEdgeException("The edge was removed from the graph")
            }

        override fun compareTo(other: Edge): Int = weight.compareTo(other.weight)
    }

    /**
     * The adjacency matrix, storing a "connection":
     * An instance of Edge meaning there is a connection, and
     * null meaning there is no connection.
     */
    private val connection = mutableListOf<MutableList<Edge?>>()

    /** Maps an edge to its starting and ending vertex. */
    private val edgeMapping = hashMapOf<Edge, Pair<Int, Int>>()

    /** Maps a vertex to an index in the adjacency matrix. */
    private val vertexMapping = hashMapOf<Int, Int>()

    /** Stores all unused indices of the adjacency matrix. */
    private val vertexIndices = Stack<Int>()

    init {
        require(vertexCapacity >= 0) { "Capacity cannot be negative" }
        ensureCapacity(vertexCapacity)
    }

    /** The vertices contained in this graph. */
    val vertices : Set<Int>
        get() = vertexMapping.keys

    /** The edges contained in this graph. */
    val edges : Set<Edge>
        get() = edgeMapping.keys

    /**
     * Adds a vertex to the graph.
     * @param id the id of the vertex.
     */
    fun addVertex(id: Int)
    {
        if (hasVertex(id))
            return

        if (vertexIndices.isEmpty())
            growMatrix(incrementSteps)

        vertexMapping[id] = vertexIndices.pop()
    }

    /**
     * Removes a vertex from the graph.
     * @param id the id of the vertex.
     */
    fun removeVertex(id: Int)
    {
        if (!hasVertex(id))
            return

        val index = vertexIndex(id)!!
        for (k in 0 until connection.size) {
            edgeMapping.remove(connection[index][k])
            edgeMapping.remove(connection[k][index])
            connection[index][k] = null
            connection[k][index] = null
        }

        vertexMapping.remove(id)
        vertexIndices.push(index)
    }

    /**
     * Checks if the graph contains a vertex.
     * @param id the id of the vertex.
     * @return true if the graph contains the vertex.
     */
    fun hasVertex(id: Int) = id in vertexMapping

    /**
     * Connects two vertices through a directed edge.
     * @param from the id of the vertex the edge starts at.
     * @param to the id of the vertex the edge ends at.
     */
    fun addEdge(from: Int, to: Int, weight: Double = DEFAULT_WEIGHT) : Edge
    {
        val u = assureVertex(from)
        val v = assureVertex(to)

        if (connection[u][v] == null)
            connection[u][v] = Edge()

        val edge = connection[u][v]!!
        edge.weight = weight

        edgeMapping[edge] = Pair(from, to)

        return edge
    }

    /**
     * Connects two vertices through a directed edge.
     * @see addEdge
     */
    fun addDirectedEdge(from: Int, to: Int, weight: Double = DEFAULT_WEIGHT)
            = addEdge(from, to, weight)

    /**
     * Connects two vertices through an undirected edge.
     * @param first the id of the first vertex.
     * @param second the id of the second vertex.
     * @param weight the weight of the edge.
     */
    fun addUndirectedEdge(first: Int, second: Int,
                          weight: Double = DEFAULT_WEIGHT) : Edge
    {
        val u = assureVertex(first)
        val v = assureVertex(second)

        // Take one of the existing edges or make a new one.
        val edge = connection[u][v] ?: connection[v][u] ?: Edge()
        edge.weight = weight

        edgeMapping[edge] = Pair(first, second)

        // Since the adjacency matrix can only store directed edges we use
        // a single instance stored in two places, effectively making two
        // directed edges a single undirected edge.
        connection[u][v] = edge
        connection[v][u] = edge

        return edge
    }

    /**
     * Connects two vertices through an undirected edge.
     * @see addUndirectedEdge
     */
    fun connect(first: Int, second: Int, weight: Double = DEFAULT_WEIGHT)
            = addUndirectedEdge(first, second, weight)

    /**
     * Removes an edge between two vertices.
     * @param from the id of the vertex the edge starts at.
     * @param to the id of the vertex the edge ends at.
     */
    fun removeEdge(from: Int, to: Int) : Edge?
    {
        if (!hasVertex(from) || !hasVertex(to))
            return null

        val u = vertexIndex(from)!!
        val v = vertexIndex(to)!!

        val edge = connection[u][v]
        edgeMapping.remove(edge)
        connection[u][v] = null

        // In this case we have an undirected edge that was created in
        // the connect() method above: read comments there for more details.
        if (connection[v][u] == edge) {
            edgeMapping.remove(connection[v][u])
            connection[v][u] = null
        }

        return edge
    }

    /**
     * Removes an edge of the graph.
     * @param edge the edge to remove.
     */
    fun removeEdge(edge: Edge)
    {
        if (edge in edgeMapping) {
            val pair = edgeMapping[edge]!!
            removeEdge(pair.first, pair.second)
        }
    }

    /**
     * Retrieves the vertices that are connected to this vertex through an edge.
     * @param vertex the id of the vertex.
     * @return a set of vertices (ids).
     */
    fun neighborsOf(vertex: Int) : Set<Int>
    {
        if (!hasVertex(vertex))
            return setOf()

        val index = vertexIndex(vertex)!!
        val result = mutableSetOf<Int>()
        for ((other, k) in vertexMapping)
            if (connection[index][k] != null)
                result.add(other)

        return result
    }

    /**
     * Retrieves the vertices that are connected to this vertex through an edge.
     * @see neighborsOf
     */
    fun neighboursOf(vertex: Int)
            = neighborsOf(vertex)

    /**
     * Returns the edge connecting two vertices.
     * @param from the id of the vertex the edge starts at.
     * @param to the id of the vertex the edge ends at.
     * @return the connecting edge or null.
     */
    fun getEdge(from: Int, to: Int) : Edge?
    {
        if (!hasVertex(from) || !hasVertex(to))
            return null

        val u = vertexIndex(from)!!
        val v = vertexIndex(to)!!

        return connection[u][v]
    }

    /**
     * Returns the edge between two vertices.
     * @see getEdge
     */
    fun getEdge(between: Pair<Int, Int>)
            = getEdge(between.first, between.second)

    /**
     * Checks if two vertices are adjacent i.e. connected with an edge.
     * @param from the id of the vertex the edge starts at.
     * @param to the id of the vertex the edge ends at.
     * @return true if the vertices are adjacent.
     */
    fun isAdjacent(from: Int, to: Int) : Boolean
            = getEdge(from, to) != null

    /**
     * Ensure that the graph can hold that many vertices.
     * @param size the amount of vertices.
     */
    fun ensureCapacity(size: Int)
    {
        if (size > connection.size)
            growMatrix(size - connection.size)
    }

    /**
     * Get the index into the adjacency matrix of a vertex.
     * @param id the id of the vertex.
     * @return the index into the adjacency matrix of a vertex or null.
     */
    private fun vertexIndex(id: Int) = vertexMapping[id]

    /**
     * Assures that a vertex exists.
     * @param id the id of the vertex.
     * @return the index into the adjacency matrix.
     */
    private fun assureVertex(id: Int) : Int
    {
        addVertex(id) // Only adds the vertex if it doesn't exist yet.
        return vertexMapping[id]!!
    }

    /**
     * Increases the size of the adjacency matrix.
     * @param amount the amount to increase.
     */
    private fun growMatrix(amount: Int)
    {
        require(amount > 0) { "An increment must be greater than 0" }

        for (row in connection)
            row.addAll(List(amount) { null })

        val previous = connection.size
        val size = previous + amount
        repeat(amount) {
            val row = MutableList<Edge?>(size) { null }
            connection.add(row)
        }

        vertexIndices.reverse()
        vertexIndices.ensureCapacity(size)
        for (k in previous until size)
            vertexIndices.push(k)

        vertexIndices.reverse()
    }

    /**
     * Reduces the size of the adjacency matrix to a minimum.
     */
    private fun shrinkMatrix()
    {
        val amountVertices = vertexMapping.size

        connection.retainFirst(amountVertices)
        for (row in connection)
            row.retainFirst(amountVertices)

        vertexIndices.clear()
    }
}
