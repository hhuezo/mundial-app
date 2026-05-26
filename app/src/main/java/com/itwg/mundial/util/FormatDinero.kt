package com.itwg.mundial.util

fun formatDinero(value: Double): String {
    val amount = if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        "%.2f".format(value).trimEnd('0').trimEnd('.')
    }
    return "$$amount"
}
