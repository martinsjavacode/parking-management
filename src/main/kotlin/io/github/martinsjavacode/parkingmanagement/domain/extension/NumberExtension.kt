package io.github.martinsjavacode.parkingmanagement.domain.extension

fun Int.percentOf(value: Int): Int = ((this / value) * 100)
