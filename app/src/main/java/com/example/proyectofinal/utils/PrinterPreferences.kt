package com.example.proyectofinal.utils

import android.content.Context

class PrinterPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("PrinterConfig", Context.MODE_PRIVATE)

    // Guardar la MAC de una impresora para una zona específica
    fun guardarImpresoraZona(zonaId: Int, macAddress: String) {
        prefs.edit().putString("PRINTER_ZONE_$zonaId", macAddress).apply()
    }

    // Obtener la MAC guardada para una zona
    fun obtenerImpresoraZona(zonaId: Int): String? {
        return prefs.getString("PRINTER_ZONE_$zonaId", null)
    }

    // Para la impresora de CLIENTE/CAJA (Usaremos ID 0 para identificarla)
    fun guardarImpresoraCaja(macAddress: String) {
        guardarImpresoraZona(0, macAddress)
    }

    fun obtenerImpresoraCaja(): String? {
        return obtenerImpresoraZona(0)
    }
}