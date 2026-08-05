package com.example.mobiledevsecops.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Fechas {

    private const val PATTERN_ISO = "yyyy-MM-dd'T'HH:mm:ss"
    private const val PATTERN_MOSTRAR = "dd/MM/yyyy HH:mm"
    private const val PATTERN_FECHA = "yyyy-MM-dd"
    private const val CLAVE_PREFIX = "V"
    private const val CLAVE_SUFFIX_LENGTH = 9
    private const val ISO_LENGTH = 19

    fun ahoraIso(): String = SimpleDateFormat(PATTERN_ISO, Locale.US).format(Date())

    fun generarClaveVenta(): String {
        val millis = System.currentTimeMillis().toString()
        val suffix = if (millis.length >= CLAVE_SUFFIX_LENGTH) {
            millis.takeLast(CLAVE_SUFFIX_LENGTH)
        } else {
            millis.padStart(CLAVE_SUFFIX_LENGTH, '0')
        }
        return CLAVE_PREFIX + suffix
    }

    fun formatearFechaHora(iso: String?): String {
        if (iso.isNullOrBlank()) return ""

        val valor = iso.trim()
        val limpio = if (valor.length >= ISO_LENGTH) valor.substring(0, ISO_LENGTH) else valor

        return try {
            val parsed = SimpleDateFormat(PATTERN_ISO, Locale.US).parse(limpio)
            if (parsed != null) {
                SimpleDateFormat(PATTERN_MOSTRAR, Locale.US).format(parsed)
            } else {
                valor
            }
        } catch (_: Exception) {
            valor
        }
    }

    fun isoFechaDesdeMillis(millis: Long): String {
        val formatter = SimpleDateFormat(PATTERN_FECHA, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return formatter.format(Date(millis))
    }

    fun formatearFechaDesdeMillis(millis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(millis))
    }
}