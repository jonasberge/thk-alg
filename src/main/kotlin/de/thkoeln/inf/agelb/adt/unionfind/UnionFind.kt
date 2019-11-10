package de.thkoeln.inf.agelb.adt.unionfind

class UnionFind(elements: Set<Int>) {
    private val parentMap = hashMapOf<Int, Int>()
    private val sizeMap = hashMapOf<Int, Int>()
    private var count = 0

    /*
     * Number of elements in this union find
     */
    val size get() = parentMap.size

    /*
     * Number of elements in the biggest set (used to check
     */
    val biggestSetSize get() = sizeMap.maxBy { it.value }?.key?.let { size(it) }

    init {
        for (element in elements) {
            parentMap[element] = element
            sizeMap[element] = 1
        }
        count = elements.size
    }

    fun addElement(element: Int) {
        if(!parentMap.containsKey(element)) {
            parentMap[element] = element
            sizeMap[element] = 1
            count++
        }
    }

    fun connected(element1: Int, element2: Int) = find(element1) == find(element2)
    fun size(element: Int) = sizeMap[find(element)]

    fun find(element: Int): Int {
        require(parentMap.containsKey(element)) { "Element not found" }

        var current = element

        // find root
        while (current != parentMap[current]) current = parentMap[current]!!
        val root = current

        // path compression
        current = element
        while (current != root) {
            val parent = parentMap[current]!!
            parentMap[current] = root
            current = parent
        }

        return root
    }

    fun union(element1: Int, element2: Int) {
        val parent1 = find(element1)
        val parent2 = find(element2)

        if(parent1 == parent2) return

        val size1 = sizeMap[parent1]!!
        val size2 = sizeMap[parent2]!!


        if(size1 < size2) {
            sizeMap[parent2] = size2 + size1
            parentMap[parent1] = parent2
        } else {
            sizeMap[parent1] = size1 + size2
            parentMap[parent2] = parent1
        }

        count--
    }

    val sets: Map<Int, Set<Int>> get() {
        val sets = mutableMapOf<Int, HashSet<Int>>()
        for ((element, _) in parentMap) {
            val root = find(element)
            sets.computeIfAbsent(root) { hashSetOf() }.add(element)
        }
        return sets
    }
}