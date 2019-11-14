package de.thkoeln.inf.agelb.visualisierung.adt.unionfind

class UnionFind(elements: Set<Int>) {
    private val parentMap = hashMapOf<Int, Int>()
    private  val sizeMap = hashMapOf<Int, Int>()
    var size: Int = 0
        private  set

    // used to check if a mst is complete
    val allInOneSet: Boolean get() {
        val elements = parentMap.keys
        val elementsSize = elements.size
        return elements.any { size(it) == elementsSize }
    }

    operator fun get(root: Int): Set<Int> {
        val set = mutableSetOf<Int>()
        for ((element, _) in parentMap) {
            val elementRoot = find(element)
            if(root == elementRoot) set.add(element)
        }
        return set.toSet()
    }

    val sets: Map<Int, Set<Int>> get() {
        val sets = mutableMapOf<Int, HashSet<Int>>()
        for ((element, _) in parentMap) {
            val root = find(element)
            sets.computeIfAbsent(root) { hashSetOf() }.add(element)
        }
        return sets
    }

    init {
        // Create own set for each element
        elements.forEach(::addElement)
    }

    fun addElement(element: Int) {
        // Create own set for element
        if(!parentMap.containsKey(element)) {
            parentMap[element] = element
            sizeMap[element] = 1
            size++
        }
    }

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

    fun size(element: Int) = sizeMap[find(element)]
    fun connected(element1: Int, element2: Int) = find(element1) == find(element2)

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

        size--
    }
}