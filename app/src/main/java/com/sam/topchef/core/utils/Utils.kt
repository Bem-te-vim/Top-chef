package com.sam.topchef.core.utils

object Utils {
    fun <T> MutableList<T>.swap(index1: Int, index2: Int){
        val element = this[index1]
        this.removeAt(index1)
        this.add(index2, element)
    }
}