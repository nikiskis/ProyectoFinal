package com.example.proyectofinal.utils

import android.content.Context

class PrinterPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("PrinterConfig", Context.MODE_PRIVATE)

    fun guardarImpresoraZona(zonaId: Int, macAddress: String) {
        prefs.edit().putString("PRINTER_ZONE_$zonaId", macAddress).apply()
    }

    fun obtenerImpresoraZona(zonaId: Int): String? {
        return prefs.getString("PRINTER_ZONE_$zonaId", null)
    }

    fun guardarImpresoraCaja(macAddress: String) {
        guardarImpresoraZona(0, macAddress)
    }

    fun obtenerImpresoraCaja(): String? {
        return obtenerImpresoraZona(0)
    }
}