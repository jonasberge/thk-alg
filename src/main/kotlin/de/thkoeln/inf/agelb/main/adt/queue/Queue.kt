package de.thkoeln.inf.agelb.main.adt.queue

/**
 * [Queue] repräsentiert eine FIFO(First-In, First-Out) queue mit generisch Elementen.
 */
class Queue<Item> : Collection<Item> {
    private var first: Node<Item>? = null   // beginning of queue
    private var last: Node<Item>? = null    // end of queue

    /**
     * Liefert die Anzahl der Elemente in der Queue.
     *
     * @return die Anzahl der Elemente
     */
    override var size: Int = 0  // number of elements on queue
        private set


    override fun isEmpty(): Boolean = first == null

    // Hilfsklasse für verkettete Liste
    private class Node<Item>(val item: Item, var next: Node<Item>? = null)

    /**
     * Liefert das zuletzt hinzugefügte Element.
     *
     * @return das zuletzt hinzugefügte Element
     * @throws NoSuchElementException wenn Queue leer ist
     */
    fun peek(): Item = first?.item ?: throw NoSuchElementException("Queue underflow")

    /**
     * Fügt der Queue ein neues Element hinzu.
     *
     * @param  item das Element, das hinzugefügt werden soll
     */
    fun enqueue(item: Item) {
        val oldlast = last
        last = Node(item)
        if (isEmpty()) first = last
        else oldlast!!.next = last
        size++
    }

    /**
     * Entfernt das zuletzt hinzugefügte Element und gibt es zurück.
     *
     * @return das zuletzt hinzugefügte Element der Queue
     * @throws NoSuchElementException wenn Queue leer ist
     */
    fun dequeue(): Item {
        val item = first?.item ?: throw NoSuchElementException("Queue underflow")
        first = first!!.next
        size--
        if (isEmpty()) last = null
        return item
    }

    override fun toString() = "Queue(items=${iterator().toString()})"

    /**
     * Liefert einen Iterator, der die Schlüssel nach FIFO-Prinzip durchläuft.
     *
     * @return ein Iterator, der die Schlüssel nach FIFO-Prinzip durchläuft
     */
    override fun iterator(): Iterator<Item> {
        return ListIterator(first)
    }

    override fun contains(element: Item): Boolean {
        for (obj in this) {
            if (obj == element) return true
        }
        return false
    }

    override fun containsAll(elements: Collection<Item>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    private inner class ListIterator(var current: Node<Item>?) : Iterator<Item> {

        override fun hasNext(): Boolean = current != null

        override fun next(): Item {
            val item = current?.item ?: throw NoSuchElementException()
            current = current!!.next
            return item
        }
    }
}
