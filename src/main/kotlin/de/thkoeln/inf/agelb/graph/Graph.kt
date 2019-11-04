package de.thkoeln.inf.agelb.graph

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
    abstract class Edge : Comparable<Edge>
    {
        abstract val from: Int
        abstract val to: Int
        abstract var weight: Double
        val isDirected: Boolean
            get() = this is DirectedEdge

        /**
         * Compares this weight with the specified edges weight for order.
         * Returns zero if the value is equal to the specified other value, a negative number if it's less than other,
         * or a positive number if it's greater than other.
         * @param other another edge to compare with
         */
        override fun compareTo(other: Edge) = weight.compareTo(other.weight)
    }

    class DirectedEdge(override val from: Int, override val to: Int,
                       override var weight: Double = DEFAULT_WEIGHT)
        : Edge()

    class UndirectedEdge(first: DirectedEdge, second: DirectedEdge)
        : Edge()
    {
        private val edges = Pair(first, second)

        override val from = edges.first.from
        override val to = edges.first.to
        override var weight: Double
            get() = edges.first.weight
            set(value) = onEach { it.weight = value }

        private fun onEach(f: (DirectedEdge) -> Unit)
                = f(edges.first).run { f(edges.second) }
    }

    /**
     * The adjacency matrix, storing a "connection":
     * An instance of Edge meaning there is a connection, and
     * null meaning there is no connection.
     */
    private val connection = mutableListOf<MutableList<Edge?>>()

    /** Maps an edge to its starting and ending vertex. */
    // private val edgeMapping = hashMapOf<EdgeOld, Pair<Int, Int>>()

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

    private val _edges = mutableSetOf<Edge>()

    /** The edges contained in this graph. */
    val edges: Set<Edge>
        get() = _edges.toSet()

    // val edges : Set<Edge>
    //     get() = edgeMapping.keys

    /** True if the graph contains no vertices. */
    val isEmpty : Boolean
        get() = vertexMapping.size == 0

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
            _edges.remove(connection[index][k])
            _edges.remove(connection[k][index])
            connection[index][k] = null
            connection[k][index] = null
        }

        vertexMapping.remove(id)
        vertexIndices.push(index)
    }

    /**
     * Checks if the graph contains one or more vertices.
     * @param id the id of the vertex.
     * @return true if the graph contains all vertices.
     */
    fun hasVertex(vararg id: Int) = !id.any { it !in vertexMapping }

    /**
     * Connects two vertices through a directed edge.
     * @param from the id of the vertex the edge starts at.
     * @param to the id of the vertex the edge ends at.
     */
    fun addEdge(from: Int, to: Int, weight: Double = DEFAULT_WEIGHT) : Edge
    {
        removeEdge(from, to)

        val u = assureVertex(from)
        val v = assureVertex(to)

        return DirectedEdge(from, to, weight).also {
            connection[u][v] = it
            _edges.add(it)
        }
    }

    /** @see addEdge */
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
        removeEdge(first, second)
        removeEdge(second, first)

        val u = assureVertex(first)
        val v = assureVertex(second)

        return UndirectedEdge(
            DirectedEdge(first, second, weight),
            DirectedEdge(second, first, weight)
        ).also {
            _edges.add(it)
            connection[u][v] = it
            connection[v][u] = it
        }
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
    fun removeEdge(from: Int, to: Int)
    {
        if (!hasVertex(from) || !hasVertex(to))
            return

        val u = vertexIndex(from)!!
        val v = vertexIndex(to)!!

        connection[u][v]?.let { edge ->
            _edges.remove(edge)
            connection[u][v] = null
            if (!edge.isDirected)
                connection[v][u] = null
        }
    }

    /** @see removeEdge */
    fun removeEdge(between: Pair<Int, Int>)
            = removeEdge(between.first, between.second)

    /** @see removeEdge */
    fun removeEdge(edge: Edge) = removeEdge(edge.from, edge.to)

    /**
     * Returns the edge connecting two vertices.
     * @param from the id of the vertex the edge starts at.
     * @param to the id of the vertex the edge ends at.
     * @return the connecting edge or null.
     */
    fun getEdge(from: Int, to: Int) : Edge?
            = if (hasVertex(from, to)) getEdge_unsafe(from, to) else null

    /** @see getEdge */
    fun getEdge(between: Pair<Int, Int>)
            = getEdge(between.first, between.second)

    /**
     * Returns the edge connecting two vertices.
     * Assumes that both vertices exist.
     * @param from the id of the vertex the edge starts at.
     * @param to the id of the vertex the edge ends at.
     * @return the connecting edge or null.
     */
    private fun getEdge_unsafe(from: Int, to: Int) : Edge?
    {
        val u = vertexIndex(from)!!
        val v = vertexIndex(to)!!
        return connection[u][v]
    }

    /**
     * Checks if two vertices are adjacent i.e. connected with an edge.
     * @param from the id of the vertex the edge starts at.
     * @param to the id of the vertex the edge ends at.
     * @return true if the vertices are adjacent.
     */
    fun isAdjacent(from: Int, to: Int) = getEdge(from, to) != null

    /**
     * Retrieves the edges leaving this vertex.
     * @param vertex the id of the vertex.
     * @return a set of edges.
     */
    fun edgesFrom(vertex: Int) = neighborsOf(vertex)
        .mapNotNull { other -> getEdge_unsafe(vertex, other) }.toSet()

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

        return result.toSet()
    }

    /** @see neighborsOf */
    fun neighboursOf(vertex: Int)
            = neighborsOf(vertex)

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
