package de.thkoeln.inf.agelb.util

/** Removes the last n elements from the list. */
fun <T> MutableList<T>.removeLast(n: Int)
{
    if (n >= this.size)
        return this.clear()

    repeat(n) { this.removeAt(this.size - 1) }
}

/** Retains the first n elements of a list and removes the other ones. */
fun <T> MutableList<T>.retainFirst(n: Int)
        = this.removeLast(this.size - n)
