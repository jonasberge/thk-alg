import de.thkoeln.inf.agelb.livecoding.KruskalMST
import de.thkoeln.inf.agelb.main.adt.graph.Edge
import de.thkoeln.inf.agelb.main.adt.graph.Graph
import de.thkoeln.inf.agelb.main.adt.unionfind.UnionFind
import org.junit.Test

import kotlin.math.abs

class LiveAufgabeTest {
    @Test
    fun `Summe des MST ist korrekt`() {
        val mst = KruskalMST(sampleGraph())
        assert(abs(mst.edges.sumByDouble { it.weight } - mst.weight) <= Double.MIN_VALUE)
    }

    @Test
    fun `MST enthält keine Zyklen`() {
        val graph = sampleGraph()
        val mst = KruskalMST(graph)
        val unionFind = UnionFind(graph.verticesSize)
        for (edge in mst.edges) {
            val one = edge.either()
            val other = edge.other(one)
            assert(!unionFind.connected(one, other))
            unionFind.union(one, other)
        }
    }

    @Test
    fun `MST ist zusammenhängend`() {
        val graph = sampleGraph()
        val mst = KruskalMST(graph)
        val unionFind = UnionFind(graph.verticesSize)

        for (edge in mst.edges) {
            val one = edge.either()
            val other = edge.other(one)
            assert(!unionFind.connected(one, other))
            unionFind.union(one, other)
        }

        for (edge in graph.edges) {
            val one = edge.either()
            val other = edge.other(one)
            assert(unionFind.connected(one, other))
        }
    }

    @Test
    fun `MST ist minimal`() {
        val graph = sampleGraph()
        val mst = KruskalMST(graph)

        for (edge in mst.edges) {
            val unionFind = UnionFind(graph.verticesSize)

            for (otherEdge in mst.edges.minus(edge)) {
                val one = otherEdge.either()
                val other = otherEdge.other(one)
                unionFind.union(one, other)
            }

            for (otherEdge in graph.edges) {
                val one = otherEdge.either()
                val other = otherEdge.other(one)
                if(!unionFind.connected(one, other)) {
                    assert(otherEdge.weight >= edge.weight)
                }
            }
        }
    }

    fun sampleGraph() = Graph(6).run {
        addEdge(Edge(Pair(2, 4), 33.0))
        addEdge(Edge(Pair(3, 5), 2.0))
        addEdge(Edge(Pair(3, 4), 20.0))
        addEdge(Edge(Pair(4, 5), 1.0))
        addEdge(Edge(Pair(2, 3), 20.0))
        addEdge(Edge(Pair(1, 4), 10.0))
        addEdge(Edge(Pair(1, 3), 50.0))
        addEdge(Edge(Pair(0, 2), 20.0))
        addEdge(Edge(Pair(0, 1), 10.0))

        this
    }
}