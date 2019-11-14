package de.thkoeln.inf.agelb.main.adt.queue

/**
 * [MinPriorityQueue] repräsentiert eine Priority Queue mit generischen Schlüsseln.
 *
 * Sie wurde als binärer Heap umgesetzt.
 *
 * Sie unterstützt das Einfügen [insert],
 * das Extrahieren des kleinsten Schlüssel [minExtract]
 * sowie sich den kleinsten Schlüssel nur anzuschauen [min].
 *
 * Das Einfügen und Extrahieren des kleinsten Schlüssels geschieht in amortisierter Laufzeit.
 *
 * Die Operationen [min], [size] und [isEmpty] erfolgt in konstanter Laufzeit.
 *
 * @param initialSize die Anfangsgröße der Priority Queue (default 1)
 * @param comparator optionaler Comparator
 *
 * @property size Aktuelle Größe der Priority Queue
 *
 */
class MinPriorityQueue<Key>(
    capacity: Int = 1, // Anfangskapazität der Priority Queue
    private val comparator: Comparator<Key>? = null // Optionaler comparator
): Collection<Key> {
    // Speichert Schlüssel von 1 bis size
    private var priorityQueue = Array<Comparable<Key>?>(capacity + 1) { null } as Array<Key?>

    /**
     * Liefert die Anzahl der Elemente in der Priority Queue.
     *
     * @return die Anzahl der Elemente
     */
    override var size: Int = 0
        private set

    /**
     * Liefert [true] wenn die Priority Queue leer ist.
     *
     * @return ob Priority Queue leer ist
     */
    override fun isEmpty(): Boolean = size == 0

    /**
     * Liefert den kleinsten Schlüssel der Priority Queue.
     *
     * @return kleinster Schlüssel der Priority Queue
     * @throws NoSuchElementException wenn Priority Queue leer ist
     */
    fun min(): Key {
        return priorityQueue[1] ?: throw NoSuchElementException("Priority queue underflow")
    }

    // Hilfsfunktion um Priority Queue dynamisch zu vergrößern
    private fun resize(newSize: Int) {
        assert(newSize > size)
        val temp = Array<Comparable<Key>?>(newSize) { null } as Array<Key?>
        for (i in 1..size) {
            temp[i] = priorityQueue[i]
        }
        priorityQueue = temp
    }

    /**
     * Fügt der Priority Queue einen neuen Schlüssel hinzu.
     *
     * @param key der Schlüssel der hinzugefügt werden soll
     */
    fun insert(key: Key) {
        // Wenn nötig die Größe der PQ verdoppeln
        if(size == priorityQueue.size - 1) resize(2 * priorityQueue.size)

        // Füge Schlüssel hinzu und verschiebene es nach oben um Heapordnung beizubehalten
        priorityQueue[++size] = key
        swim(size)
        assert(isMinHeap)
    }

    /**
     * Entfernt den kleinsten Schlüssel aus der Priority Queue und gibt ihn zurück.
     *
     * @return den kleinsten Schlüssel der Priority Queue
     * @throws NoSuchElementException wenn Priority Queue leer ist
     */
    fun extractMin(): Key {
        val min = min()
        exchange(1, size--)
        sink(1)
        priorityQueue[size + 1] = null
        if ((size > 0) && (size == (priorityQueue.size - 1) / 4)) resize(priorityQueue.size / 2)
        assert(isMinHeap)
        return min
    }

    /*
     * Hilfsfunktionen für Heapordnung
     */

    // Upheap: Nach oben verscheiben
    private fun swim(key: Int) {
        var _key = key

        while (_key > 1 && greater(_key / 2, _key) > 0) {
            exchange(_key, _key / 2)
            _key /= 2
        }
    }

    // Downheap: nach unten verschieben
    private fun sink(key: Int) {
        var _key = key

        while (2 * _key <= size) {
            var y = 2 * _key
            if(y < size && greater(y, y + 1) > 0) y++
            if(greater(_key, y) < 1) break
            exchange(_key, y)
            _key = y
        }
    }

    /*
     * Hilfsfunktionen zum Vergleichen und Vertauschen
     */

    private fun greater(i: Int, y: Int) = when(comparator) {
        null -> (priorityQueue[i]!! as Comparable<Key>).compareTo(priorityQueue[y]!!)
        else -> comparator.compare(priorityQueue[i]!!, priorityQueue[y]!!)
    }

    private fun exchange(i: Int, y: Int) {
        val temp = priorityQueue[i]!!
        priorityQueue[i] = priorityQueue[y]
        priorityQueue[y] = temp
    }

    private val isMinHeap: Boolean get() {
        for (i in 1..size) {
            if(priorityQueue[i] == null) return false
        }

        for (i in size + 1 until priorityQueue.size) {
            if(priorityQueue[i] != null) return false
        }
        if(priorityQueue[0] != null) return false
        return isMinHeapOrdered(1)
    }

    private fun isMinHeapOrdered(key: Int): Boolean {
        if(key > size) return true

        val left = 2 * key
        val right = 2 * key  + 1

        if(left <= size && greater(key, left) > 0) return false
        if(right <= size && greater(key, right) > 0) return false
        return isMinHeapOrdered(left) && isMinHeapOrdered(right)
    }

    // erzeugt eine deep-copy des objekts, gebraucht für iterator
    private fun copy(): MinPriorityQueue<Key> = MinPriorityQueue(size, comparator).let { copy ->
        for (i in 1..size)
            copy.insert(priorityQueue[i]!!)
        copy
    }

    override fun contains(element: Key): Boolean {
        for (obj in this) {
            if (obj == element) return true
        }
        return false
    }

    override fun containsAll(elements: Collection<Key>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    /**
     * Liefert einen Iterator um aufsteigend über die Schlüssel zu iterieren.
     *
     * @return ein Iterator um aufsteigend über Schlüssel zu iterieren
     */
    override fun iterator(): Iterator<Key> = HeapIterator(copy())

    private class HeapIterator<Key>(private val copy: MinPriorityQueue<Key>): Iterator<Key> {
        override fun hasNext(): Boolean = !copy.isEmpty()
        override fun next(): Key = copy.extractMin() ?: throw NoSuchElementException()
    }
}