package com.duo.nebula.util

import java.util.concurrent.TimeUnit

/**
 * Formata um timestamp (epoch millis) em texto relativo curto, em
 * português — ex.: "agora", "5 min", "3 h", "2 d". Solução simples e
 * sem dependências extras (evita trazer uma lib inteira só para isso).
 */
fun formatRelativeTime(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val diff = (nowMillis - timestampMillis).coerceAtLeast(0)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        minutes < 1 -> "agora"
        minutes < 60 -> "${minutes} min"
        hours < 24 -> "${hours} h"
        days < 7 -> "${days} d"
        days < 30 -> "${days / 7} sem"
        days < 365 -> "${days / 30} mês"
        else -> "${days / 365} anos"
    }
}
