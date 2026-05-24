package com.itwg.mundial.util

fun formatPuntos(value: Double): String {
    if (value % 1.0 == 0.0) return value.toLong().toString()
    return "%.2f".format(value).trimEnd('0').trimEnd('.')
}
