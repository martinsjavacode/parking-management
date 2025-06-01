package io.github.martinsjavacode.parkingmanagement.adapters.extension

fun Int.percentOf(value: Int): Int = ((this.toDouble() / value) * 100).toInt()
