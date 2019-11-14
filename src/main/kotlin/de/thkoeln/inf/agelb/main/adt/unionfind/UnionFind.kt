package de.thkoeln.inf.agelb.main.adt.unionfind

/**
 * [UnionFind] repräsentiert den ADT UnionFind.
 *
 * Zu Beginn wird für jedes Element eine Komponente erstellt.
 *
 * Mit [find] lässt sich herausfinden
 * zur welchen Komponente ein Element gehört.
 * Mit [union] können zwei Komponenten vereint werden.
 * [connected] überprüft ob zwei Elemente zur selben Komponente gehören.
 * [size] liefert die Anzahl der Komponenten.
 *
 * @param initSize die Anzahl der Elemente
 * @property size die Anzahl der Komponenten
 */
class UnionFind(initSize: Int) {

    // Referenz zum übergeordneten Element (Anfangs: zu sich selbst)
    private val parent = IntArray(initSize) { it }
    private val rank = ByteArray(initSize) { 0 }
    var size: Int = initSize
        private set

    init {
        require(initSize >= 0)
    }

    /**
     * Liefert zum [index] die Wurzel der Komponente.
     *
     * @param index ein Element im Union Find
     * @return Wurzel der Komponente
     */
    fun find(index: Int): Int {
        require(index in parent.indices)
            { "index $index is not between 0 and ${parent.size - 1}" }

        var root = index
        while (root != parent[root]) {
            parent[root] = parent[parent[root]] // path compression
            root = parent[root]
        }

        return root
    }

    /**
     * Überprüft ob zwei Elemente zur selben Komponente gehören.
     *
     * @param p ein beliebiges Element
     * @param q ein beliebiges Element
     *
     * @return ob zwei Elemente zur selben Komponente gehören
     */
    fun connected(p: Int, q: Int) = find(p) == find(q)


    /**
     * Vereint zwei Komponenten miteinander.
     */
    fun union(p: Int, q: Int) {
        val rootP = find(p)
        val rootQ = find(q)
        if(rootP == rootQ) return

        when {
            rank[rootP] < rank[rootQ] -> parent[rootP] = rootQ
            rank[rootP] > rank[rootQ] -> parent[rootQ] = rootP
            else -> {
                parent[rootQ] = rootP
                rank[rootP]++
            }
        }

        size--
    }
}