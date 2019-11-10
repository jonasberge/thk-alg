package de.thkoeln.inf.agelb.adt.unionfind

open class UnionFind(elements: Set<Int>) {
    protected val parentMap = hashMapOf<Int, Int>()
    protected val sizeMap = hashMapOf<Int, Int>()
    open var size: Int = 0
        protected set

    // used to check if a mst is complete
    val allInOneSet get() = parentMap.keys.any { size(it) == size }

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

    open fun find(element: Int): Int {
        val parent = parentMap[element] ?: throw IllegalArgumentException("Element not found")
        return if(parent != element) find(parent) else parent
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