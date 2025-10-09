package com.example.proyectofinal.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.android.Android

object SupabaseClient {

    private const val SUPABASE_URL = "https://pnmwdgtgkpoehrynuzcl.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBubXdkZ3Rna3BvZWhyeW51emNsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk5NzIxMjYsImV4cCI6MjA3NTU0ODEyNn0.gw4TfKrNpEZ_SwuUuiZyX29r7I3c8CgQJir2cWYpusw"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    )
    {
        install(Postgrest)
    }
}