package de.thkoeln.inf.agelb.main.adt.queue

/**
 * [IndexMinPriorityQueue] repräsentiert eine indizierte Priority Queue mit generischen Schlüsseln.
 *
 * Sie wurde als binärer Heap umgesetzt.
 *
 * Sie unterstützt das Einfügen und Löschen [insert] & [delete],
 * das Extrahieren des Index des kleinsten Schlüssel [minExtract]
 * sowie sich den kleinsten Schlüssel und Index
 * nur anzuschauen [minIndex] & [minKey].
 *
 * Außerdem lässt sich der Schlüssel mit [changeKey] ändern.
 *
 * Mit [increaseKey] und [decreaseKey] lässt sich der Schlüssel für den gegebenen Index anpassen.
 *
 * Das Einfügen, Löschen, Extrahieren, Ändern des Schlüssels u. Anpassen des Schlüssels für gegebenen Index
 * geschieht in logarithmischen Laufzeit.
 *
 * Die Operationen [minIndex], [minKey], [size], [contains], [isEmpty] und [keyOf] erfolgen in konstanter Laufzeit.
 *
 * @property maxSize maximale Größe der Priority Queue
 * @property size Anzahl der Elemente in Priority Queue
 *
 * @param Key generische Typ der Schlüssel der Priority Queue
 *
 */
class IndexMinPriorityQueue<Key: Comparable<Key>>(
    private val maxSize: Int // maximale Größe der pq
): Collection<Int> {
    /**
     * Liefert Anzahl der Elemente in Priority Queue
     *
     * @return Anzahl der Elemente in Prioirty Queue
     */
    override var size: Int = 0
        private set

    // binärer Heap mit 1-based indexing
    private val pq = IntArray(maxSize + 1)
    // inversertierte pq - qp[pq[i]] = pq[qp[i]] = i
    private val qp = IntArray(maxSize + 1) { -1 }

    // Schlüssel
    private val keys: Array<Key?> = Array<Comparable<Key>?>(maxSize + 1) { null } as Array<Key?>

    init {
        require(maxSize >= 0)
    }

    /**
     * Liefert [true] wenn die Priority Queue leer ist.
     *
     * @return ob Priority Queue leer ist
     */
    override fun isEmpty(): Boolean = size == 0


    override fun contains(index: Int): Boolean {
        require(index in 0 until maxSize)
        return qp[index] != -1
    }

    override fun containsAll(indices: Collection<Int>): Boolean {
        return indices.all { contains(it) }
    }


    /**
    * Assoziiert Schlüssel mit Index.
     *
     * @param index ein Index
     * @param key der Schlüssel der mit [index] assoiziiert werden soll
     *
     * @throws IllegalArgumentException index nicht in 0..maxSize
     * @throws IllegalArgumentException wenn schon ein Element mit Index assoziiert ist
    */
    fun insert(index: Int, key: Key) {
        require(index in 0 until maxSize)
        require(!contains(index)) { "Index is already in priority queue" }
        size++
        qp[index] = size
        pq[size] = index
        keys[index] = key
        swim(size)
    }

    /**
     * Liefert den Index, der zum kleinsten Schlüssel der Priority Queue gehört.
     *
     * @return Index des kleinsten Schlüssels der Priority Queue
     * @throws NoSuchElementException wenn Priority Queue leer ist
     */
    fun minIndex() = if(isEmpty()) throw NoSuchElementException("Priority queue underflow") else pq[1]

    /**
     * Liefert den kleinsten Schlüssel der Priority Queue.
     *
     * @return der kleinste Schlüssels der Priority Queue
     * @throws NoSuchElementException wenn Priority Queue leer ist
     */
    fun minKey() = keys[minIndex()]


    /**
     * Entfernt den kleinsten Schlüssel aus der Priority Queue und gibt Index zurück.
     *
     * @return den Index des kleinsten Schlüssel der Priority Queue
     * @throws NoSuchElementException wenn Priority Queue leer ist
     */
    fun extractMin(): Int {
        val min = minIndex()
        exchange(1, size--)
        sink(1)
        assert(min == pq[size + 1])
        qp[min] = -1
        keys[min] = null
        pq[size + 1] = -1
        return min
    }

    /**
     * Liefert Schlüssel der mit Index [index] assoiziiert ist.
     *
     * @param index ein Index
     *
     * @return mit [index] assoziierter Schlüssel
     */
    fun keyOf(index: Int): Key = when {
        index in 0 until maxSize -> throw IllegalArgumentException()
        !contains(index) -> throw NoSuchElementException()
        else -> keys[index]!!
    }

    /**
     * Ändert mit [index] assozierten Schlüssel durch neuen Schlüssel.
     *
     * @param index ein Index
     * @param key der neue Schlüssel der mit [index] assoziiert werden soll.
     */
    fun changeKey(index: Int, key: Key) {
        checkIndex(index)
        keys[index] = key
        swim(qp[index])
        sink(qp[index])
    }

    /**
     * Den Schlüssel der mit [index] assoziiert ist verringern.
     *
     * @param index der Index des Schlüssels der verringert werden soll
     * @param key neuer kleinerer Schlüssek
     */
    fun decreaseKey(index: Int, key: Key) {
        checkIndex(index)
        require(keys[index]!! > key)
            { "Calling decreaseKey() with given argument would not strictly decrease the key" }
        keys[index] = key
        swim(qp[index])
    }

    /**
     * Den Schlüssel der mit [index] assoziiert ist vergrößern.
     *
     * @param index der Index des Schlüssels der vergrößert werden soll
     * @param key neuer größerer Schlüssel
     */
    fun increaseKey(index: Int, key: Key) {
        checkIndex(index)
        require(keys[index]!! < key)
            { "Calling increaseKey() with given argument would not strictly increase the key" }
        keys[index] = key
        sink(qp[index])
    }

    /**
     * Lösche Schlüssel mit gegebenen Index.
     */
    fun delete(index: Int) {
        checkIndex(index)
        val qpIndex = qp[index]
        exchange(qpIndex, size--)
        swim(qpIndex)
        sink(qpIndex)
        keys[qpIndex] = null
        qp[qpIndex] = -1
    }

    /*
     * Hilfsfunktionen
     */

    private fun checkIndex(index: Int) {
        require(index in 0 until maxSize)
        if(!contains(index)) throw NoSuchElementException("index is not in the priority queue")
    }

    private fun greater(i: Int, y: Int) = keys[pq[i]]!! > keys[pq[y]]!!

    private fun exchange(i: Int, y: Int) {
        val swap = pq[i]
        pq[i] = pq[y]
        pq[y] = swap
        qp[pq[i]] = i
        qp[pq[y]] = y
    }

    /*
     * Hilfsfunktionen für Heap
     */

    private fun swim(key: Int) {
        var k = key

        while (k > 1 && greater(k / 2, k)) {
            exchange(k, k / 2)
            k /= 2
        }
    }

    private fun sink(key: Int) {
        var k = key
        while (2 * k <= size) {
            var y = 2 * k
            if(y < size && greater(y, y + 1)) y++
            if(!greater(k, y)) break
            exchange(k, y)
            k = y
        }
    }

    /*
     * Iterator
     */

    fun copy() = IndexMinPriorityQueue<Key>(pq.size - 1).let { copy ->
        for (i in 1..size) {
            copy.insert(pq[i], keys[pq[i]]!!)
        }
        copy
    }

    override fun iterator(): Iterator<Int> = HeapIterator(copy())

    private class HeapIterator<Key: Comparable<Key>>(val copy: IndexMinPriorityQueue<Key>): Iterator<Int> {
        override fun hasNext(): Boolean = copy.isNotEmpty()
        override fun next(): Int = copy.extractMin()
    }
}